package biz.paluch.testing.acceptance.selenium;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Meta;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.web.selenium.RemoteWebDriverProvider;
import org.jbehave.web.selenium.WebDriverProvider;

import com.google.common.io.Files;

/**
 * Captures HTML and a screenshot in a temporary directory (UUID) and assigns after that the captured data to a scenario/story.
 */
public class CaptureDebugOnFailure extends NullStoryReporter {

    public static final String DEFAULT_NAME = "failed-scenario";
    public static final String DIRECTORY = "{0}/failures/{1}/";
    public static final String PHASE_1_PATTERN = DIRECTORY + DEFAULT_NAME;
    public static final String PHASE_2_PATTERN = DIRECTORY + DEFAULT_NAME + "-{2}";

    protected final StoryReporterBuilder reporterBuilder;

    private ThreadLocal<String> step = new ThreadLocal<>();
    private ThreadLocal<String> scenario = new ThreadLocal<>();
    private Map<Throwable, String> uuids = new ConcurrentHashMap<>();
    private WebDriverProvider webDriverProvider;

    public CaptureDebugOnFailure(WebDriverProvider driverProvider, StoryReporterBuilder reporterBuilder) {
        this.webDriverProvider = driverProvider;
        this.reporterBuilder = reporterBuilder;

    }

    @AfterScenario(uponOutcome = AfterScenario.Outcome.FAILURE)
    public void afterScenarioFailure(UUIDExceptionWrapper uuidWrappedFailure) throws Exception {
        if (uuidWrappedFailure instanceof PendingStepFound) {
            return; // we don't take screen-shots for Pending Steps
        }

        capture(uuidWrappedFailure);
        uuids.put(uuidWrappedFailure.getCause(), uuidWrappedFailure.getUUID().toString());
    }

    private void capture(UUIDExceptionWrapper uuidWrappedFailure) {
        String screenshotPath = path(uuidWrappedFailure.getUUID()) + ".png";
        String sourcePath = path(uuidWrappedFailure.getUUID()) + ".html";
        String currentUrl = getCurrentUrl();

        boolean savedIt = false;
        try {
            File file = new File(sourcePath);
            file.getParentFile().mkdirs();

            savedIt = webDriverProvider.saveScreenshotTo(screenshotPath);
            Files.write(webDriverProvider.get().getPageSource(), file, Charset.defaultCharset());
        } catch (RemoteWebDriverProvider.SauceLabsJobHasEnded e) {
            System.err.println("Screenshot of page '" + currentUrl
                    + "' has **NOT** been saved. The SauceLabs job has ended, possibly timing out on their end.");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!savedIt) {
            System.err
                    .println("Screenshot of page '"
                            + currentUrl
                            + "' has **NOT** been saved. If there is no error, perhaps the WebDriver type you are using is not compatible with taking screenshots");
        }
    }

    private String getCurrentUrl() {
        String currentUrl = "[unknown page url]";
        try {
            currentUrl = webDriverProvider.get().getCurrentUrl();
        } catch (Exception e) {
        }
        return currentUrl;
    }

    protected String path(Object uuid) {
        return MessageFormat.format(PHASE_1_PATTERN, reporterBuilder.outputDirectory(), uuid);
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        scenario.set(scenarioTitle);
    }

    @Override
    public void scenarioMeta(Meta meta) {
    }

    @Override
    public void afterScenario() {
        scenario.remove();
    }

    @Override
    public void beforeStep(String step) {
        this.step.set(step);
    }

    @Override
    public void successful(String step) {
        this.step.remove();
    }

    @Override
    public void failed(String step, Throwable cause) {
        String uuid = uuids.get(cause.getCause());
        if (uuid != null) {
            copyAndClean(uuid, "png", scenario.get(), step);
            copyAndClean(uuid, "html", scenario.get(), step);
        }
    }

    public void copyAndClean(String uuid, String fileType, String scenario, String step) {
        String sourcePath = path(uuid) + "." + fileType;

        String targetPath = path(scenario, step) + "." + fileType;

        try {

            File sourceFile = new File(sourcePath);
            File sourceParent = sourceFile.getParentFile();
            File targetFile = new File(targetPath);
            File targetParent = targetFile.getParentFile();

            if (!sourceFile.exists()) {
                return;
            }

            if (!targetParent.exists()) {
                targetParent.mkdirs();
            }
            Files.copy(sourceFile, targetFile);

            sourceFile.delete();

            if (sourceParent.listFiles().length == 0) {
                sourceParent.delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String path(String scenario, String step) {

        return MessageFormat.format(PHASE_2_PATTERN, reporterBuilder.outputDirectory(), getFileName(scenario),
                getFileName(step));
    }

    private String getFileName(String input) {
        String result = "";
        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c) || ' ' == c) {
                result += c;
            } else {
                result += ' ';
            }
        }
        result = result.replaceAll("  ", " ");
        return result;
    }
}

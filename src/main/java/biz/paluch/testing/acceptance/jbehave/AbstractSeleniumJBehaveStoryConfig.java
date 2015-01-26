package biz.paluch.testing.acceptance.jbehave;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import biz.paluch.testing.ConfigurationUtil;
import biz.paluch.testing.acceptance.AcceptanceProperties;
import biz.paluch.testing.acceptance.debug.DebugSeleniumStepsModule;
import biz.paluch.testing.acceptance.selenium.*;
import biz.paluch.testing.acceptance.selenium.PerStoryWebDriverSteps;
import biz.paluch.testing.guice.IntegrationTestModule;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.thoughtworks.xstream.core.util.Fields;
import org.apache.commons.lang.StringUtils;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.web.selenium.*;
import org.junit.Test;

/**
 * Abstract, generic config for all the Stories/Steps which should be executed using JBehave.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public abstract class AbstractSeleniumJBehaveStoryConfig extends JUnitStories {

    private WebDriverProvider driverProvider;
    private SeleniumContext context = new SeleniumContext();
    private ContextView contextView = null;
    private boolean debugMode = false;
    private boolean initialized = false;

    /**
     * 
     */
    public AbstractSeleniumJBehaveStoryConfig() {

    }

    private void setutpDriverProvider(Properties properties) {
        String mode = properties.getProperty(AcceptanceProperties.SELENIUM_MODE, AcceptanceProperties.SELENIUM_MODE_LOCAL);
        System.out.println(mode);
        if (mode.equals(AcceptanceProperties.SELENIUM_MODE_LOCAL)) {
            driverProvider = new IntegrationPropertyWebDriverProvider(properties);
        } else {
            driverProvider = new IntegrationPropertyRemoteWebDriverProvider(properties);
        }

        driverProvider.initialize();
        driverProvider.get();
        driverProvider.end();
    }

    @Override
    @Test
    public void run() throws Throwable {
        if (!initialized) {
            Embedder embedder = configuredEmbedder();
            if (!(embedder.embedderControls() instanceof UnmodifiableEmbedderControls)) {
                embedder.embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(true)
                        .doIgnoreFailureInView(true).useThreads(2).useStoryTimeoutInSecs(60);
            }
            initialized = true;
        }

        Embedder embedder = configuredEmbedder();
        embedder.useExecutorService(MoreExecutors.sameThreadExecutor());

        List<String> storyPaths = storyPaths();
        embedder.mapStoriesAsPaths(storyPaths);
        try {
            embedder.runStoriesAsPaths(storyPaths());
        } finally {
            embedder.generateCrossReference();
        }
    }

    @Override
    public Embedder configuredEmbedder() {

        Field embedderField = Fields.find(ConfigurableEmbedder.class, "embedder");
        embedderField.setAccessible(true);
        Embedder embedder = (Embedder) Fields.read(embedderField, this);
        embedder.processSystemProperties();

        return super.configuredEmbedder();
    }

    @Override
    public Configuration configuration() {

        Properties properties = ConfigurationUtil.readConfiguration();

        setutpDriverProvider(properties);

        debugMode = Boolean.valueOf(System.getProperty(AcceptanceProperties.DEBUG_MODE, "false")).booleanValue();

        Class<? extends Embeddable> embeddableClass = this.getClass();
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters();
        // factory to allow parameter conversion and loading from external resources (used by StoryParser too)
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(
                embeddableClass), parameterConverters);

        // add custom converters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")), new ExamplesTableConverter(
                examplesTableFactory));

        contextView = createContextView();
        JBehaveState state = new JBehaveState(context);

        Configuration configuration = new SeleniumConfiguration()
                .useWebDriverProvider(driverProvider)
                .useSeleniumContext(context)
                .useFailureStrategy(new ExceptionStrategy())
                .useStepMonitor(new JBehaveStepMonitor(contextView, state, createStepMonitor()))
                .useStoryLoader(new LoadFromClasspath(embeddableClass))
                .useStoryParser(new RegexStoryParser(examplesTableFactory))
                .useStoryReporterBuilder(
                        new StoryReporterBuilder().withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                                .withFormats(Format.CONSOLE, Format.TXT, Format.HTML, Format.XML, Format.STATS)
                                .withFailureTrace(true).withFailureTraceCompression(true))
                .useParameterConverters(parameterConverters);

        CaptureDebugOnFailure capture = new CaptureDebugOnFailure(driverProvider, configuration.storyReporterBuilder());

        configuration.storyReporterBuilder().withReporters(capture);

        Set<Class<?>> classes = new HashSet<>(getClasses());

        PerStoryWebDriverSteps perStoryWebDriverSteps = new PerStoryWebDriverSteps(driverProvider);

        SeleniumStepsModule module;

        if (debugMode) {
            module = new DebugSeleniumStepsModule(state, perStoryWebDriverSteps, classes, driverProvider,
                    new PerStoriesContextView(contextView), context, perStoryWebDriverSteps, capture);
        } else {
            module = new SeleniumStepsModule(perStoryWebDriverSteps, classes, driverProvider, new PerStoriesContextView(
                    contextView), context, perStoryWebDriverSteps, capture);
        }
        Injector injector = Guice.createInjector(Modules.combine(new IntegrationTestModule(), module));
        useStepsFactory(new GuiceStepsFactory(configuration, injector));

        return configuration;
    }

    private PrintStreamStepMonitor createStepMonitor() {
        return new SilentStepMonitor();
    }

    private ContextView createContextView() {
        return new LocalFrameContextView().sized(500, 100);
    }

    protected abstract List<Class<?>> getClasses();

    @Override
    protected List<String> storyPaths() {
        String include = System.getProperty(AcceptanceProperties.STORIES_INCLUDE, "**/**.story");
        String exclude = System.getProperty(AcceptanceProperties.STORIES_EXCLUDE, "**/*excluded*.story");
        String filter = System.getProperty(AcceptanceProperties.STORIES_FILTER, "");
        List<String> stories = new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(this.getClass()), include,
                exclude);
        if (StringUtils.isNotEmpty(filter)) {
            return filterContains(filter, stories);
        }
        return stories;
    }

    private List<String> filterContains(String filter, List<String> stories) {
        List<String> result = new ArrayList<>();
        for (String story : stories) {
            if (story.toLowerCase().contains(filter.toLowerCase())) {
                result.add(story);
            }
        }
        return result;
    }

    public static class PerStoriesContextView {

        private final ContextView contextView;

        public PerStoriesContextView(ContextView contextView) {
            this.contextView = contextView;
        }

        @AfterStories
        public void afterStory() {
            contextView.close();
        }
    }

}

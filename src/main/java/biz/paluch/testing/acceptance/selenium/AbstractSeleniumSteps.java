package biz.paluch.testing.acceptance.selenium;

import javax.inject.Inject;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.web.selenium.SeleniumContext;
import org.jbehave.web.selenium.WebDriverProvider;
import org.openqa.selenium.WebDriver;

/**
 * Steps to set scenario name.
 */
public abstract class AbstractSeleniumSteps {

    @Inject
    private WebDriverProvider webDriverProvider;

    @Inject
    private SeleniumContext seleniumContext;

    @BeforeScenario
    public void beforeTheScenario() {
        seleniumContext.setCurrentScenario(getClass().getSimpleName());
    }

    protected WebDriver webDriver() {
        return webDriverProvider.get();
    }
}

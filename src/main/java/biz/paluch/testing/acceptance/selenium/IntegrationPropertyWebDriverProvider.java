package biz.paluch.testing.acceptance.selenium;

import static java.lang.Boolean.*;

import java.util.Locale;
import java.util.Properties;

import org.jbehave.web.selenium.DelegatingWebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import biz.paluch.testing.acceptance.AcceptanceProperties;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 14.08.13 07:56
 */
public class IntegrationPropertyWebDriverProvider extends DelegatingWebDriverProvider {

    private Properties properties;

    public enum Browser {
        CHROME("chrome"), FIREFOX("firefox"), HTMLUNIT("hmlunit"), IE("internet explorer"), SAFARI("safari");

        private String remoteId;

        Browser(String remoteId) {
            this.remoteId = remoteId;
        }

        public String getRemoteId() {
            return remoteId;
        }
    }

    public IntegrationPropertyWebDriverProvider(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize() {

        Browser browser = Browser.valueOf(Browser.class,
                properties.getProperty(AcceptanceProperties.SELENIUM_BROWSER, "firefox").toUpperCase(usingLocale()));
        try {
            delegate.set(createDriver(browser));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private WebDriver createDriver(Browser browser) {
        switch (browser) {
            case SAFARI:
                return createSafariDriver();
            case CHROME:
                return createChromeDriver();
            case FIREFOX:
                return createFirefoxDriver();
            case HTMLUNIT:
            default:
                return createHtmlUnitDriver();
            case IE:
                return createInternetExplorerDriver();
        }
    }

    protected WebDriver createChromeDriver() {
        return new ChromeDriver();
    }

    protected WebDriver createSafariDriver() {
        return new SafariDriver();
    }

    protected WebDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    protected WebDriver createHtmlUnitDriver() {
        HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME);
        boolean javascriptEnabled = parseBoolean(properties.getProperty("webdriver.htmlunit.javascriptEnabled", "true"));
        driver.setJavascriptEnabled(javascriptEnabled);
        return driver;
    }

    protected WebDriver createInternetExplorerDriver() {
        return new InternetExplorerDriver();
    }

    protected Locale usingLocale() {
        return Locale.getDefault();
    }
}

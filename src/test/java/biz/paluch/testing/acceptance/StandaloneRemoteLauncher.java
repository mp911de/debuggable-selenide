package biz.paluch.testing.acceptance;

import org.junit.Test;

import biz.paluch.testing.acceptance.selenium.IntegrationPropertyWebDriverProvider;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class StandaloneRemoteLauncher {

    @Test
    public void run() throws Throwable {

        System.setProperty(AcceptanceProperties.DEBUG_MODE, "false");
        System.setProperty(AcceptanceProperties.SELENIUM_MODE, AcceptanceProperties.SELENIUM_MODE_REMOTE);
        System.setProperty(AcceptanceProperties.SELENIUM_BROWSER, IntegrationPropertyWebDriverProvider.Browser.IE.getRemoteId());

        new SeleniumJBehaveStories().run();
    }
}

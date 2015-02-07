package biz.paluch.testing.acceptance;

import org.junit.Test;

import biz.paluch.testing.acceptance.selenium.Browser;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class StandaloneRemoteLauncher {

    public static void main(String[] args) throws Throwable {
        new StandaloneRemoteLauncher().run();
    }

    @Test
    public void run() throws Throwable {

        System.setProperty(AcceptanceProperties.DEBUG_MODE, "false");
        System.setProperty(AcceptanceProperties.SELENIUM_MODE, AcceptanceProperties.SELENIUM_MODE_REMOTE);

        new SeleniumJBehaveStories().run();
    }
}

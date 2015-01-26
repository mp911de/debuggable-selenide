package biz.paluch.testing.acceptance;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 15.11.13 10:04
 */
public class StandaloneLauncher {

    @Test
    public void run() throws Throwable {

        if (SystemUtils.IS_OS_MAC_OSX) {
            File chromedriver = new File("/Applications/chromedriver");
            if (chromedriver.exists()) {
                System.setProperty("webdriver.chrome.driver", chromedriver.getCanonicalPath());
                System.setProperty(AcceptanceProperties.SELENIUM_BROWSER, "chrome");
            }
        }

        System.setProperty(AcceptanceProperties.DEBUG_MODE, "true");
        System.setProperty(AcceptanceProperties.STORIES_INCLUDE, "**/**.story");

        new SeleniumJBehaveStories().run();
    }
}

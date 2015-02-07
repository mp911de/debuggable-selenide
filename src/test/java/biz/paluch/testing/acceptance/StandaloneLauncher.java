package biz.paluch.testing.acceptance;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 15.11.13 10:04
 */
public class StandaloneLauncher {

    public static void main(String[] args) throws Throwable {
        new StandaloneLauncher().run();
    }

    @Test
    public void run() throws Throwable {

        if (SystemUtils.IS_OS_MAC_OSX) {
            File chromedriver = new File("drivers/mac/chrome/chromedriver");
            if (chromedriver.exists()) {
                System.setProperty("webdriver.chrome.driver", chromedriver.getCanonicalPath());
            }
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            File chromedriver = new File("drivers/win/chrome32/chromedriver.exe");
            if (chromedriver.exists()) {
                System.setProperty("webdriver.chrome.driver", chromedriver.getCanonicalPath());
            }

            int bits = Integer.getInteger("sun.arch.data.model", 32);
            File iedriver = new File("drivers/win/ie" + bits + "/IEDriverServer.exe");
            if (iedriver.exists()) {
                System.setProperty("webdriver.ie.driver", iedriver.getCanonicalPath());
            }
        }

        if (SystemUtils.IS_OS_LINUX) {
            int bits = Integer.getInteger("sun.arch.data.model", 32);

            File chromedriver = new File("drivers/linux/chrome" + bits + "/chromedriver");
            if (chromedriver.exists()) {
                System.setProperty("webdriver.chrome.driver", chromedriver.getCanonicalPath());
            }
        }

        System.setProperty(AcceptanceProperties.DEBUG_MODE, "true");
        System.setProperty(AcceptanceProperties.STORIES_INCLUDE, "**/**.story");

        new SeleniumJBehaveStories().run();
    }
}

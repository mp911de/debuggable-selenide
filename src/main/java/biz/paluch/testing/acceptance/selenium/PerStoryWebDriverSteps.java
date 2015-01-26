package biz.paluch.testing.acceptance.selenium;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.web.selenium.DelegatingWebDriverProvider;
import org.jbehave.web.selenium.WebDriverProvider;
import org.jbehave.web.selenium.WebDriverSteps;

import com.codeborne.selenide.WebDriverRunner;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Selenide bootstrap and story scope.
 */
public class PerStoryWebDriverSteps extends WebDriverSteps implements Scope {

    private Map<Key, Provider> cache = new ConcurrentHashMap<>();

    public PerStoryWebDriverSteps(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    @BeforeStory
    public void beforeStory() throws Exception {
        try {
            driverProvider.initialize();
            com.codeborne.selenide.Configuration.selectorMode = com.codeborne.selenide.Configuration.SelectorMode.Sizzle;
            WebDriverRunner.setWebDriver(driverProvider.get());
            WebDriverRunner.clearBrowserCache();
        } catch (Error e) {
            throw e;
        }
    }

    @AfterStory
    public void afterStory() throws Exception {
        try {
            driverProvider.get();
            driverProvider.end();
        } catch (DelegatingWebDriverProvider.DelegateWebDriverNotFound e) {
        }

        cache.clear();
    }

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        cache.put(key, unscoped);

        return unscoped;
    }
}

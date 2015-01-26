package biz.paluch.testing.acceptance.selenium;

import java.util.Set;

import org.jbehave.web.selenium.WebDriverProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;

/**
 * Module providing singletons and objects as DI source.
 */
public class SeleniumStepsModule extends AbstractModule {

    private Object[] instances;
    private Set<Class<?>> classes;
    private Scope scope;

    public SeleniumStepsModule(Scope scope, Set<Class<?>> classes, Object... instances) {
        this.instances = instances;
        this.classes = classes;
        this.scope = scope;
    }

    @Override
    protected void configure() {
        for (Object instance : instances) {
            bind((Class) instance.getClass()).toInstance(instance);
            if (instance instanceof WebDriverProvider) {
                bind(WebDriverProvider.class).toInstance((WebDriverProvider) instance);
            }
        }

        for (Class theClass : classes) {
            bind(theClass).in(scope);
        }
    }
}

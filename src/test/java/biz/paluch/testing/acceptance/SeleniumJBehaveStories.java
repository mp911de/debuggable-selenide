package biz.paluch.testing.acceptance;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;

import biz.paluch.testing.acceptance.jbehave.AbstractSeleniumJBehaveStoryConfig;

/**
 * Discovery for Guice classes. Scans the class path and identifies classes which can be used together with guice.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class SeleniumJBehaveStories extends AbstractSeleniumJBehaveStoryConfig {

    @Override
    protected List<Class<?>> getClasses() {

        List<String> classResourceNames = new StoryFinder().findClassNames(
                CodeLocations.getPathFromURL(CodeLocations.codeLocationFromClass(SeleniumJBehaveStories.class)).toString(),
                asList("**/pages/**.class", "**/steps/**.class"), null);

        List<Class<?>> classes = new ArrayList<>();

        for (String classResourceName : classResourceNames) {
            String className = classResourceName.replace('/', '.').replace(".class", "");
            if (className.contains("$")) {
                continue;
            }
            try {
                Class<?> theClass = Class.forName(className);
                classes.add(theClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load Class " + className, e);
            }
        }

        return classes;
    }
}

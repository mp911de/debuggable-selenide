package biz.paluch.testing.guice;

import java.lang.reflect.Method;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.inject.Module;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.04.14 15:43
 */
public class IntegrationTestGuiceBerryRule implements TestRule {

    private final GuiceBerryEnvSelector guiceBerryEnvSelector;
    private final Object testInstance;

    public IntegrationTestGuiceBerryRule(Class<? extends Module> envClass, Object instance) {
        this.guiceBerryEnvSelector = DefaultEnvSelector.of(envClass);
        this.testInstance = instance;
    }

    private static TestDescription buildTestDescription(Object testCase, String methodName) {
        String testCaseName = testCase.getClass().getName();
        return new TestDescription(testCase, testCaseName + "." + methodName);
    }

    @Override
    public Statement apply(Statement base, Description description) {

        return new IntegratedStatement(base, description);
    }

    private class IntegratedStatement extends Statement {

        private final Statement base;
        private final Description description;

        public IntegratedStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {

            Method method = getMethod(description.getTestClass(), description.getMethodName());

            final GuiceBerry.GuiceBerryWrapper setupAndTearDown = GuiceBerry.INSTANCE.buildWrapper(
                    buildTestDescription(testInstance, description.getMethodName()), guiceBerryEnvSelector);

            try {
                setupAndTearDown.runBeforeTest();
                base.evaluate();
            } finally {
                setupAndTearDown.runAfterTest();
            }
        }

        private Method getMethod(Class<?> testClass, String methodName) {

            Method[] methods = testClass.getMethods();

            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            return null;
        }

    }
}

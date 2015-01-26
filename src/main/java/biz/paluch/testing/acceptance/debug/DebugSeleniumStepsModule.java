package biz.paluch.testing.acceptance.debug;

import java.util.Set;

import biz.paluch.testing.acceptance.jbehave.JBehaveState;
import biz.paluch.testing.acceptance.selenium.SeleniumStepsModule;

import com.google.inject.Scope;
import com.google.inject.matcher.Matchers;

/**
 * Debug interceptor module. Enables @DebugableInvocation annotation processing.
 */
public class DebugSeleniumStepsModule extends SeleniumStepsModule {

    private final JBehaveState state;

    public DebugSeleniumStepsModule(JBehaveState state, Scope scope, Set<Class<?>> classes, Object... instances) {
        super(scope, classes, instances);
        this.state = state;
    }

    @Override
    protected void configure() {
        bindInterceptor(Matchers.annotatedWith(DebugableInvocation.class), Matchers.any(), new DebugInvoationInterceptor(state));
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(DebugableInvocation.class), new DebugInvoationInterceptor(state));
        super.configure();
    }
}

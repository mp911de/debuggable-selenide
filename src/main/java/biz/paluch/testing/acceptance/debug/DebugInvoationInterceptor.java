package biz.paluch.testing.acceptance.debug;

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Layout;

import biz.paluch.testing.acceptance.jbehave.JBehaveState;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.UIAssertionError;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 15.11.13 11:47
 */
public class DebugInvoationInterceptor implements MethodInterceptor {

    private JBehaveState state;

    public DebugInvoationInterceptor(JBehaveState state) {
        this.state = state;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean retry = false;
        do {
            try {
                retry = false;
                return invocation.proceed();
            } catch (UIAssertionError | Exception throwable) {

                StringBuilder builder = new StringBuilder();
                builder.append("Scenario: ").append(state.getCurrentScenario()).append(Layout.LINE_SEP);
                builder.append("Step: ").append(state.getStep()).append(Layout.LINE_SEP);
                builder.append("Method: ")
                        .append(invocation.getMethod().getDeclaringClass().getName() + "#" + invocation.getMethod().getName())
                        .append(Layout.LINE_SEP);
                builder.append("Arguments: ").append(Arrays.asList(invocation.getArguments())).append(Layout.LINE_SEP)
                        .append(Layout.LINE_SEP);

                StoryExceptionDialog dialog = StoryExceptionDialog.open(builder.toString(), throwable,
                        WebDriverRunner.getWebDriver());

                if (dialog.isDoRetry()) {
                    retry = true;
                }

                if (dialog.isDoCancel()) {
                    System.exit(0);
                }
            }

        } while (retry);

        return null;
    }
}

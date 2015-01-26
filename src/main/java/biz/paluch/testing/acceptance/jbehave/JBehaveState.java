package biz.paluch.testing.acceptance.jbehave;

import org.jbehave.web.selenium.SeleniumContext;

/**
 * Threadlocal JBehave state storage.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JBehaveState {

    private final SeleniumContext context;
    private final ThreadLocal<String> step = new ThreadLocal<>();

    public JBehaveState(SeleniumContext context) {
        this.context = context;
    }

    public SeleniumContext getContext() {
        return context;
    }

    public void setStep(String step) {
        this.step.set(step);
    }

    public String getStep() {
        return step.get();
    }

    public String getCurrentScenario() {
        return context.getCurrentScenario();
    }
}

package biz.paluch.testing.acceptance.jbehave;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;
import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepType;
import org.jbehave.web.selenium.ContextView;

public class JBehaveStepMonitor extends DelegatingStepMonitor {

    private final ContextView contextView;
    private final JBehaveState state;

    public JBehaveStepMonitor(ContextView contextView, JBehaveState state, StepMonitor delegate) {
        super(delegate);
        this.contextView = contextView;
        this.state = state;
    }

    public void performing(String step, boolean dryRun) {
        String currentScenario = state.getCurrentScenario();
        state.setStep(step);
        contextView.show(currentScenario, step);
        super.performing(step, dryRun);
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
        super.usingTableParameterNameForParameter(name, position);
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
        super.convertedValueOfType(value, type, converted, converterClass);
    }

    @Override
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance) {
        super.stepMatchesType(stepAsString, previousAsString, matchesType, stepType, method, stepsInstance);
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method, Object stepsInstance) {
        super.stepMatchesPattern(step, matches, stepPattern, method, stepsInstance);
    }

    @Override
    public void foundParameter(String parameter, int position) {
        super.foundParameter(parameter, position);
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
        super.usingAnnotatedNameForParameter(name, position);
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
        super.usingNaturalOrderForParameter(position);
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
        super.usingParameterNameForParameter(name, position);
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
        super.usingTableAnnotatedNameForParameter(name, position);
    }
}
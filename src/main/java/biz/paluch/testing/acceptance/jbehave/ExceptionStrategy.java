package biz.paluch.testing.acceptance.jbehave;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.reflect.FieldUtils;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;

/**
 * JBehave Exception strategy for improved exceptions.
 */
public class ExceptionStrategy implements FailureStrategy {

    @Override
    public void handleFailure(Throwable throwable) throws Throwable {
        if (throwable instanceof UUIDExceptionWrapper) {
            handle((UUIDExceptionWrapper) throwable);
        }
        throw throwable;
    }

    private void handle(UUIDExceptionWrapper throwable) throws Throwable {

        if (throwable.getCause() instanceof NoSuchElementException) {

            NoSuchElementException toThrow = new NoSuchElementException(throwable.getMessage());
            filterStackTrace(throwable, toThrow);
            throw toThrow;
        }

        if (throwable.getCause() instanceof AssertionError) {
            AssertionError stopped = (AssertionError) throwable.getCause();
            if (stopped.getCause() instanceof NoSuchElementException) {

                NoSuchElementException toThrow = new NoSuchElementException(stopped.getMessage());
                filterStackTrace(stopped, toThrow);
                throw toThrow;
            }

            if (stopped.getCause() instanceof AssertionError) {
                filterStackTrace(stopped, stopped.getCause());
                throw stopped.getCause();
            }

            if (stopped.getCause() instanceof WebDriverException) {
                WebDriverException wde = (WebDriverException) stopped.getCause();

                Field field = FieldUtils.getField(Throwable.class, "detailMessage", true);
                String detailMessage = (String) field.get(wde);

                AssertionError toThrow = new AssertionError(throwable.getMessage() + ": " + detailMessage, null);
                filterStackTrace(stopped, toThrow);
                throw toThrow;
            }
        }

        throw throwable.getCause();
    }

    private void filterStackTrace(Throwable stopped, Throwable toThrow) {
        List<StackTraceElement> elements = new ArrayList(Arrays.asList(stopped.getStackTrace()));
        Iterator<StackTraceElement> it = elements.iterator();
        List<StackTraceElement> toRemove = new ArrayList<>();

        while (it.hasNext()) {
            StackTraceElement element = it.next();
            if (element.getClassName().startsWith("org.seleniumhq") || element.getClassName().startsWith("org.jbehave")
                    || element.getClassName().startsWith("com.google") || element.getClassName().startsWith("java.")) {
                toRemove.add(element);
            }
        }

        elements.removeAll(toRemove);

        StackTraceElement[] finalTrace = elements.toArray(new StackTraceElement[elements.size()]);

        toThrow.setStackTrace(finalTrace);
    }
}

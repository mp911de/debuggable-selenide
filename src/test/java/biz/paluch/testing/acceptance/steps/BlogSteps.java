package biz.paluch.testing.acceptance.steps;

import static org.assertj.core.api.Assertions.*;

import javax.inject.Inject;

import biz.paluch.testing.acceptance.debug.DebugableInvocation;
import biz.paluch.testing.acceptance.pages.BlogPage;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import biz.paluch.testing.acceptance.pages.Homepage;
import biz.paluch.testing.acceptance.selenium.AbstractSeleniumSteps;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.01.15 11:20
 */
@DebugableInvocation
public class BlogSteps extends AbstractSeleniumSteps {

    @Inject
    private Homepage homepage;

    @Inject
    private BlogPage blogPage;

    @Given("I'm on $url")
    public void givenImOnUrl(String url) {
        homepage.navigate(url);
    }

    @When("I click Blog")
    public void whenIClickBlog() {
        homepage.clickBlog();
    }

    @Then("the URL ends with $endsWith")
    public void thenTheUrlEndsWith(String endsWith) {
        assertThat(webDriver().getCurrentUrl()).endsWith(endsWith);
    }

    @When("I click entry no $entryNo")
    public void whenIClickEntry(int no) {
        blogPage.clickEntry(no);
    }

}

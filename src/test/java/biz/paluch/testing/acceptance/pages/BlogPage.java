package biz.paluch.testing.acceptance.pages;

import static com.codeborne.selenide.Selenide.*;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.01.15 11:16
 */
public class BlogPage {

    public void clickEntry(int no) {
        $$("article").get(no).find("h1 a").click();
    }
}

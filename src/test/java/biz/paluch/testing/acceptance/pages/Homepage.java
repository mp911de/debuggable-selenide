package biz.paluch.testing.acceptance.pages;

import static com.codeborne.selenide.Selenide.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.01.15 11:16
 */
public class Homepage {

    public void navigate(String url) {
        try {
            open(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clickBlog() {
        $("li.item108 a").click();
    }
}

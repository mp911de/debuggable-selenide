package biz.paluch.testing.acceptance.selenium;

/**
* @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
*/
public enum Browser
{
    CHROME("chrome"), FIREFOX("firefox"), HTMLUNIT("hmlunit"), IE("internet explorer"), SAFARI("safari");

    private String remoteId;

    Browser(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public static Browser find(String id){
        for (Browser browser : values())
        {
            if(browser.name().equalsIgnoreCase(id) || browser.getRemoteId().equalsIgnoreCase(id))
            {
                return browser;
            }
        }

        throw new IllegalArgumentException("No enum constant " + id);
    }
}

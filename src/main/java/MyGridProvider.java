import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static org.openqa.selenium.firefox.FirefoxDriver.PROFILE;

public class MyGridProvider implements WebDriverProvider {
    @Override
    public WebDriver createDriver(DesiredCapabilities capabilities) {
        FirefoxProfile firefoxProfile = new FirefoxProfile();

        firefoxProfile.setPreference("browser.fullscreen.autohide",true);
        firefoxProfile.setPreference("browser.fullscreen.animateUp",0);

        capabilities.setCapability(PROFILE, firefoxProfile);
        capabilities.setBrowserName("firefox");

        try {
            return new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

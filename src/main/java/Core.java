import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Created by User on 20.08.2017.
 */
public class Core {
    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.chrome.driver", "D:/code/Chromedriver/chromedriver.exe");
        System.setProperty("selenide.browser", "Chrome");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("chrome.switches", "--disable-vaapi-accelerated-video-encode");
        Configuration.browserSize = "1200x900"; //ширина 1200, чтобы до искомой сабменюшки пришлось скроллить

        open("https://market.yandex.ru/");

        locateSubMenuLink("Игрушки", "Детские товары");

        $(By.linkText("Конструкторы")).shouldBe(visible).click();

        //маркет автоматически применяет фильтры, можно не жать кнопку применить
        $(By.name("glf-pricefrom-var")).setValue("1500"); autocompleteWait();
        $(By.name("glf-priceto-var")).setValue("2500"); autocompleteWait();

        //star wars есть на первой странице, скукотень
        //взял рандомное с пятой, можешь заменить на любое другое и посмотреть как работает
        System.out.println(productFind("Снежное приключение Анны"));

        $(By.linkText("Конструкторы")).click();
        $(By.linkText("Перейти ко всем фильтрам")).click();

        String engStr = extendedSearch("ENGINO");
        String znStr = extendedSearch("Знаток");

        if(!engStr.equals(znStr))
            System.out.println("Количество отличается: " +
                    engStr.split(" ")[1] + " vs " + znStr.split(" ")[1]);
    }

    private static void locateSubMenuLink (String what, String where){
        ElementsCollection liElems = $$("li");

        SelenideElement desiredLi = null;
        for (int i = 0; i < liElems.size(); ++i) {
            SelenideElement currLi = liElems.get(i);
            if(currLi.getAttribute("data-department").equals(where))
                desiredLi = currLi;
        }

        //чек сабменюшек на visible/hidden
        SelenideElement tmSubwrap = desiredLi.$(By.className("topmenu__subwrap")).shouldBe(hidden);

        //можно было бы вбить в поиске конструкторы, да, но скрытые подменюшки штука постоянно встречающаяся
        desiredLi.hover();
        tmSubwrap.shouldBe(visible);

        ElementsCollection smLinks = tmSubwrap.$$("a");
        LinkedHashMap <String, String> smLinksLHM = new LinkedHashMap();

        for (int i = 0; i < smLinks.size(); ++i) {
            SelenideElement currLink = smLinks.get(i);
            smLinksLHM.put(currLink.getText(), currLink.getAttribute("style"));
        }

        //hidden элементы в мапу не попадают, жмакаем стрелку
        //можешь ширину окна сменить на 1600 и перезапустить, посмотреть как работает
        if(!smLinksLHM.containsKey(what))
            tmSubwrap.$(By.className("topmenu__arrow")).shouldBe(visible).click();
        tmSubwrap.$(By.linkText(what)).click();
    }
    private static void autocompleteWait(){
        $(By.className("spin2")).shouldBe(hidden);
        $(By.className("n-filter-panel-counter")).shouldBe(visible);
        $(By.className("n-filter-panel-counter")).shouldBe(hidden);
    }
    private static String productFind(String desiredProductName){
        SelenideElement desiredElement = null;
        boolean dstFound = false;

        while (!dstFound){
            ElementsCollection titles  = $$("h4");
            for (int i = 0; i < titles.size(); ++i) {
                if(titles.get(i).getText().contains(desiredProductName)) {
                    dstFound = true;
                    desiredElement = titles.get(i);
                }
                if(!dstFound && i == titles.size()-1){
                    $(By.className("n-pager__button-next")).click();
                    //если крутилка пропала, значит страничка прогрузилась
                    $(By.className("spin2")).shouldBe(hidden);
                    titles  = $$("h4");
                    i=0;
                }
            }
        }

        desiredElement.click();
        return $("h1").getText() + ": " + $(By.className("price")).getText();
    }
    private static String extendedSearch (String manufacturer){
        $(By.className("button_pseudo_yes")).click();
        $(By.className("button_pseudo_yes")).shouldHave(text("Свернуть"));
        //switch to великая вещь))
        switchTo().activeElement().sendKeys(manufacturer);
        //цепочка вызовов может быть сколь угодно длинной
        //не обязательно юзать setSelected, можно по лейблу попасть
        $(By.className("n-filter-block__list-items-wrap")).
                $(By.className("n-filter-block__list-items")).
                $(By.className("n-filter-block__item")).
                $(By.className("checkbox")).
                $(By.className("checkbox__label")).click();
        String result = $(By.className("n-filter-panel-counter")).shouldBe(visible).getText();
        $(By.className("n-filter-panel-counter")).shouldBe(hidden);

        $(By.className("button_action_n-filter-reset")).click();
        refresh();

        return result;
    }
}

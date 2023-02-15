import com.codeborne.selenide.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

public class RozetkaMainCheckTest {

    private static int DEFAULT_PAGE_LOAD = 20;
    private static final String HOME_URL = "https://rozetka.com.ua/ua/";
    private static final String LANGUAGE_UA = "UA";
    private static final String GOODS_FOR_HOUSE_CATEGORY = "Товари для дому";
    private static final String MATRICES_SUB_CATEGORY = "Матраци";
    private static final String LANGUAGE_UA_SEARCH_BUTTON_TEXT = "Знайти";
    private static final String BASKET_PATH = "//button[@rzopencart]";
    private static final By LANGUAGE_LOCATOR = By.xpath("//li[contains(@class,'lang-header')]");
    private static final By BASKET_ORDER = By.xpath(BASKET_PATH + "/rz-icon-badge");
    private static final By PRIVATE_OFFICE = By.xpath("//rz-user/a");
    private static final By SEARCH_BUTTON_LOCATOR = By.xpath("//form[contains(@class,'search-form')]/button[contains(@class,'search-form')]");
    private static final By CATALOG_BUTTON = By.xpath("//button[contains(@class,'icon menu')]");
    private static final By CATALOG_MENU = By.xpath("//ul[contains(@class,'menu-categories')]/li");
    private static final By SUB_CATALOG_MENU = By.xpath("//rz-list-tile//li/a");
    private static final By GOODS_ITEMS = By.xpath("//app-goods-tile-default");
    private static final By MENU_ITEM_PRICE = By.xpath(".//span[contains(@class,'price-value')]");
    private static final By MENU_ITEM_NAME = By.xpath(".//a[contains(@class,'goods-tile__heading')]");
    private static final By MENU_ITEM_BASKET_ICON = By.xpath(".//button[contains(@class,'buy-button')]");
    private static final By SECTION_TITLE = By.xpath("//h1");
    private static final By BASKET_POPUP = By.xpath("//rz-shopping-cart");
    private static final By BASKET_CARDS = By.xpath("//rz-cart-product");
    private static final By BASKET_CARD_NAME = By.xpath(".//a[@data-testid='title']");
    private static final By BASKET_CARD_PRICE = By.xpath(".//p[contains(@class,'product__price')]");
    private String goodName;
    private String goodPrice;

    @Test(description = "Check default page layout")
    public void verifyBasicPageView() {
        openHomePage();
        selectUkrainianLanguage();
        verifySearchButtonText(LANGUAGE_UA_SEARCH_BUTTON_TEXT);
        verifyBasketIsEmpty();
        verifyUserIsNotLogin();
    }

    @Test(description = "Check user can add any good to basket", priority = 1)
    public void verifyBasicFlow() {
        openHomePage();
        selectUkrainianLanguage();
        openCatalog();
        selectCategory(GOODS_FOR_HOUSE_CATEGORY);
        selectSubCategory(MATRICES_SUB_CATEGORY);
        addFirstGoodToBasket();
        openBasket();
        verifyCorrectGoodIsAdded();
    }

    private void openHomePage() {
        open(HOME_URL);
    }

    private void openCatalog() {
        $(CATALOG_BUTTON).shouldBe(Condition.visible).click();
        assertThat($(CATALOG_MENU).isDisplayed())
                .isTrue();
    }

    private void openBasket() {
        $(By.xpath(BASKET_PATH)).shouldBe(Condition.visible).click();
        assertThat($(BASKET_POPUP).shouldBe(Condition.visible).isDisplayed())
                .isTrue();
    }

    private void selectCategory(String category) {
        selectCategoryItem(CATALOG_MENU, category);
    }

    private void selectSubCategory(String subCategory) {
        selectCategoryItem(SUB_CATALOG_MENU, subCategory);
    }

    private void addFirstGoodToBasket() {
        SelenideElement firstGood = $$(GOODS_ITEMS).shouldBe(CollectionCondition.sizeGreaterThan(1)).first();
        goodName = firstGood.$(MENU_ITEM_NAME).getText();
        goodPrice = firstGood.$(MENU_ITEM_PRICE).getText();
        firstGood.$(MENU_ITEM_BASKET_ICON).click();
    }

    private void verifySearchButtonText(String text) {
        String searchButtonText = $(SEARCH_BUTTON_LOCATOR).shouldBe(Condition.visible).getText();
        assertThat(text).isEqualTo(searchButtonText);
    }

    private void verifyBasketIsEmpty() {
        assertThat($(BASKET_ORDER).getText())
                .isEmpty();
    }

    private void verifyUserIsNotLogin() {
        assertThat($(PRIVATE_OFFICE).exists())
                .isFalse();
    }

    private void verifyCorrectGoodIsAdded() {
        $$(BASKET_CARDS).shouldHave(CollectionCondition.size(1));
        SelenideElement firstCard = $$(BASKET_CARDS).first();
        String actualName = firstCard.$(BASKET_CARD_NAME).getText();
        String actualPrice = firstCard.$(BASKET_CARD_PRICE).getText();
        assertSoftly(soflty -> {
            soflty.assertThat(goodName)
                  .isEqualTo(actualName);
            soflty.assertThat(goodPrice)
                  .isEqualTo(actualPrice);
        });
    }

    private void selectUkrainianLanguage() {
        Collection<SelenideElement> languages = $$(LANGUAGE_LOCATOR).shouldBe(CollectionCondition.sizeGreaterThan(1));
        SelenideElement expectedLanguage = languages.stream()
                                                    .filter(language -> language.getText().equals(LANGUAGE_UA))
                                                    .findFirst()
                                                    .orElseThrow(() -> new RuntimeException(LANGUAGE_UA + " language wasn't found"));
        expectedLanguage.click();
        assertThat(Objects.requireNonNull(expectedLanguage.getAttribute("class")))
                .contains("state_active");
    }

    private void selectCategoryItem(By collection, String itemName) {
        $$(collection).stream()
                      .filter(categoryChoice -> categoryChoice.getText().equals(itemName))
                      .findFirst()
                      .orElseThrow(() -> new RuntimeException(String.format("Menu item - %s is not found ", itemName)))
                      .scrollIntoView("{block: 'center', inline: 'center'}")
                      .hover()
                      .click();
        checkSectionTitle(itemName);
    }

    private void checkSectionTitle(String titleName) {
        try {
            new FluentWait<SelenideElement>($(SECTION_TITLE))
                    .withTimeout(Duration.ofSeconds(DEFAULT_PAGE_LOAD))
                    .until(title -> title.getText().equals(titleName));
        } catch (Exception exception) {
            Logger.getLogger(this.getClass()).debug("wait for expected title");
        }
        assertThat($(SECTION_TITLE).getText())
                .isEqualTo(titleName);
    }
}
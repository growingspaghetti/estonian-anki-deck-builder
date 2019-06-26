package com.github.growingspaghetti.eesti;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EestiLoecsenSeleniumTest {
    @Test
    public void testSelenium() throws Exception {
        WebDriver driver = new RemoteWebDriver(
            new URL("http://dockerhost:4444/wd/hub"),
            DesiredCapabilities.chrome());

        List<String> pages = new ArrayList<String>();
        try {
            driver.get("https://www.loecsen.com/en/learn-estonian");
            WebElement   itemdiv = driver.findElement(By.id("list_t"));
            List<String> targets
                = itemdiv
                      .findElements(By.xpath("//div[@id='list_t']//li"))
                      .stream()
                      .map(WebElement::getText)
                      .map(String::trim)
                      .collect(Collectors.toList());

            for (String target : targets) {
                System.out.println("page: " + target);
                By         by = By.xpath("//div[@id='list_t']//li//h3[contains(text(), '" + target + "')]");
                WebElement we = itemdiv.findElement(by);
                // https://stackoverflow.com/questions/37879010/selenium-debugging-element-is-not-clickable-at-point-x-y
                // we.click();
                ((JavascriptExecutor)driver).executeScript("arguments[0].click();", we);

                WebDriverWait wait       = new WebDriverWait(driver, 10);
                WebElement    playButton = driver.findElement(By.id("btn_autoplay"));
                wait.until(ExpectedConditions.elementToBeClickable(playButton));

                WebElement js   = driver.findElement(By.xpath("//div[@id='container']//script"));
                String     code = (String)((JavascriptExecutor)driver)
                                  .executeScript("return jQuery(arguments[0]).text();", js);

                System.out.println(code);
                Assert.assertThat(code, StringContains.containsString("https://www.loecsen.com/OrizonFlash_V2"));
                pages.add(code);
            }
        } finally {
            Optional.ofNullable(driver).ifPresent(WebDriver::quit); // not close
        }
    }
}

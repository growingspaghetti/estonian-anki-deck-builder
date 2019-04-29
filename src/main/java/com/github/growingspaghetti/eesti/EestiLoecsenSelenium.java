package com.github.growingspaghetti.eesti;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.growingspaghetti.eesti.data.LoeesenEngEesti;
import com.github.growingspaghetti.eesti.util.ImagemagickUtils;
import com.github.growingspaghetti.eesti.util.OeesenEngEestiUtils;

class SeleniumUtils {
    static List<String> fetchPages() throws Exception {
        WebDriver driver = new RemoteWebDriver(
            new URL("http://127.0.0.1:4444/wd/hub"),
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
                pages.add(code);
            }
        } finally {
            Optional.ofNullable(driver).ifPresent(WebDriver::quit); // not close
        }
        return pages;
    }
}

/**
 * Sadly, I eventually found that Selenium does not help for this case
 * because javascript doesn't dynamically re-write the data section.
 * <pre>
 *  cd selenium
 *  docker-compose up
 *  # run this program
 *  docker-compose down
 * </pre>
 * If you want to see the browser, use
 * <pre>
 *  sudo snap install remmina
 *  snap run remmina
 *  localhost:5900 (VNC)
 *  User password: secret
 * </pre> 
 * @author ryoji
 * @since 2019/04/26
 */
public class EestiLoecsenSelenium {
    /**
     * @param sect
     * <pre>//data-id
     * exp_data[" 6594"]["img"] = "/OrizonFlash_V2/ressources/svg/LABLANG_V2_1_1.svg";
     * exp_data[" 6594"]["exp"] = "Tere p&#228;evast!";
     * exp_data[" 6594"]["var"] = "N";
     * exp_data[" 6594"]["phon"] = "";
     * exp_data[" 6594"]["quizz_disabled"] = "";
     * exp_data[" 6594"]["sound"] = "https://www.loecsen.com/OrizonFlash_V2/ressources/son/anglais-1-1.mp3";
     * exp_data[" 6594"]["exptra"] = "Hello";
     * exp_data[" 6594"]["phontra"] = "";
     * exp_data[" 6594"]["soundtra"] = "https://www.loecsen.com/OrizonFlash_V2/ressources/son/estonien-1-1.mp3";
     * </pre>
     * @return
     */
    private static LoeesenEngEesti convert(String sect) {
        String img    = StringUtils.substringBetween(sect, "[\"img\"] = \"", "\"");
        String ee     = StringUtils.substringBetween(sect, "[\"exp\"] = \"", "\"");
        String en     = StringUtils.substringBetween(sect, "[\"exptra\"] = \"", "\"");
        String mp3    = StringUtils.substringBetween(sect, "[\"soundtra\"] = \"", "\"");
        String dataId = StringUtils.substringBetween(sect, "exp_data[\"", "\"").trim();
        return new LoeesenEngEesti(
            StringEscapeUtils.unescapeHtml(en),
            StringEscapeUtils.unescapeHtml(ee),
            mp3,
            img,
            dataId);
    }

    private static void processPage(String page) throws Exception {
        List<LoeesenEngEesti> enees = new ArrayList<>();
        for (String section : page.split(Pattern.quote("] = {};"))) {
            if (!StringUtils.contains(section, "[\"exptra\"]")) {
                continue;
            }
            enees.add(convert(section));
        }

        List<String> tsv = new ArrayList<>();
        enees.forEach(enee -> {
            String en = StringEscapeUtils.escapeCsv(enee.getEng());
            String ee = StringEscapeUtils.escapeCsv(enee.getEesti());

            File mp3;
            try {
                mp3 = OeesenEngEestiUtils.downloadMp3(enee);
            } catch (Exception ex) {
                throw new IllegalStateException("exception at mp3 download", ex);
            }

            File png;
            try {
                File   svg      = OeesenEngEestiUtils.downloadSvg(enee);
                String basename = FilenameUtils.removeExtension(svg.getName());
                File   target   = new File("loeesen/" + basename + ".png");
                png             = ImagemagickUtils.convert(svg, target);
            } catch (Exception ex) {
                throw new IllegalStateException("exception at img processing", ex);
            }

            String l = String.format("%s\t<img src=\"%s\">\t%s\t[sound:%s]", en, png.getName(), ee, mp3.getName());
            tsv.add(l);
            System.out.println(en + "\n" + ee + "\n" + png.getName() + "\n" + mp3.getName());
        });
        FileUtils.writeLines(new File("loeesen/anki.txt"), "UTF-8", tsv, true);
    }

    public static void main(String args[]) throws Exception {
        new File("loeesen").mkdir();
        SeleniumUtils.fetchPages().stream().forEach(t -> {
            try {
                processPage(t);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}

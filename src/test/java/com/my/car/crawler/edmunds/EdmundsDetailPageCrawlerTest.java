package com.my.car.crawler.edmunds;

import com.my.car.entity.Car;
import org.junit.AfterClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EdmundsDetailPageCrawlerTest {
    private static WebDriver driver = new ChromeDriver();
    private static WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3).getSeconds());
    private static Pattern yearPattern = Pattern.compile("[0-9]{4}");

    @Test
    public void testCrawlCar_hasSpecModal_getSpecs() throws Exception {
        EdmundsDetailPageCrawler edmundsDetailPageCrawler = new EdmundsDetailPageCrawler(0, 0);
        driver.get("https://www.edmunds.com/mini/countryman/2018/vin/WMZYT3C39J3E00515/?radius=25");
        Thread.sleep(2000);
        Car car = edmundsDetailPageCrawler.crawlCar(driver, wait, yearPattern);
        String expectedSpec = "Bluetooth Connection, Dual Moonroof, Keyless Start, Light White";

        assertEquals(expectedSpec, car.getSpec());
    }

    @Test
    public void testCrawlCar_brokenDescription_getDescription() throws Exception {
        EdmundsDetailPageCrawler edmundsDetailPageCrawler = new EdmundsDetailPageCrawler(0, 0);
        driver.get("https://www.edmunds.com/chevrolet/trax/2019/vin/3GNCJPSB2KL396169/?radius=25");
        Thread.sleep(2000);
        Car car = edmundsDetailPageCrawler.crawlCar(driver, wait, yearPattern);
        assertTrue(car.getDescription().startsWith("!!!WOW..."));
        assertTrue(car.getDescription().endsWith("* Power train only, internally lubricated parts, over 10 years old " +
                "and 120,000 miles excluded."));
    }

    @Test
    public void testCrawlCar_noFeatureModal_getSpecs() throws Exception {
        EdmundsDetailPageCrawler edmundsDetailPageCrawler = new EdmundsDetailPageCrawler(0, 0);
        driver.get("https://www.edmunds.com/chevrolet/trax/2019/vin/3GNCJPSB2KL396169/?radius=25");
        Thread.sleep(2000);
        Car car = edmundsDetailPageCrawler.crawlCar(driver, wait, yearPattern);
        String expectedSpec = "Automatic ECOTEC 1.4L I4 SMPI DOHC Turbocharged VVT, Awd";

        assertEquals(expectedSpec, car.getSpec());
    }

    @AfterClass
    public static void clean() {
        driver.close();
    }
}

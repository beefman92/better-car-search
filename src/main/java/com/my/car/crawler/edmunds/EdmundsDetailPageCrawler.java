package com.my.car.crawler.edmunds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.my.car.crawler.common.Crawler;
import com.my.car.entity.Car;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class EdmundsDetailPageCrawler implements Crawler {
    private static final Logger logger = LogManager.getLogger(EdmundsDetailPageCrawler.class);
    private final String seedUrl = "https://www.edmunds.com/used-all/";
    private int startPage;
    private int endPage;
    private Gson gson;
    private Set<String> carVinSet;
    private List<Car> carList;

    public EdmundsDetailPageCrawler(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
        carVinSet = new HashSet<>();
        carList = new ArrayList<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void crawl() throws Exception {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3).getSeconds());
        Pattern yearPattern = Pattern.compile("[0-9]{4}");
        try {
            // create another tab to render details of cars
            String mainTabHandle = driver.getWindowHandle();
            ((JavascriptExecutor)driver).executeScript("window.open('about:blank','_blank');");
            String detailTabHandle = null;
            for (String handle: driver.getWindowHandles()) {
                if (!handle.equals(mainTabHandle)) {
                    detailTabHandle = handle;
                }
            }
            if (detailTabHandle == null) {
                throw new RuntimeException("Failed to create new tab");
            }

            driver.get("https://www.edmunds.com/used-all/");
            Thread.sleep(5000);

            for (int i = startPage; i <= endPage; i++) {
                driver.switchTo().window(mainTabHandle);
                driver.get(seedUrl + "?pagenumber=" + i);
                Thread.sleep(7000);
                List<WebElement> carCardList = driver.findElements(
                        By.xpath("//li[@class='d-flex mb-0_75 mb-md-1_5 col-12 col-md-6']"));
                for (WebElement carCard : carCardList) {
                    WebElement linkElement = carCard.findElement(By.xpath(".//a[@class='usurp-inventory-card-vdp-link']"));
                    String link = linkElement.getAttribute("href");
                    driver.switchTo().window(detailTabHandle);
                    driver.get(link);
                    Thread.sleep(2000);
                    try {
                        Car car = crawlCar(driver, wait, yearPattern);
                        logger.info("Get info for car({}) from url({})", car.getVin(), link);
                        if (carVinSet.add(car.getVin())) {
                            carList.add(car);
                        }
                    } catch (ParseException e) {
                        logger.warn("Failed to parse car price or mileage with url({}): {}", link, e.getMessage());
                    } catch (Exception e) {
                        logger.warn("Failed to crawl car info with url({}): {}", link, e.getMessage());
                    }
                    driver.switchTo().window(mainTabHandle);
                    Thread.sleep(2000);
                }
            }
        } finally {
            driver.quit();
        }
    }

    @Override
    public void save(String file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            gson.toJson(carList, fileWriter);
        }
    }

    public Car crawlCar(WebDriver driver, WebDriverWait wait, Pattern yearPattern) throws ParseException, InterruptedException {
        Car car = new Car();

        // year
        String title1 = driver.findElement(By.xpath(".//h1[@class=\"heading-2 d-inline-block mb-0_25\"]")).getText().toLowerCase();
        Matcher matcher = yearPattern.matcher(title1);
        matcher.find();
        String yearString = matcher.group();
        car.setYear(Integer.parseInt(matcher.group()));

        // make and model
        int index = title1.indexOf(yearString);
        String[] temp = title1.substring(index + 4).trim().split(" ");
        car.setMake(temp[0]);
        if ("mini".equals(temp[0])) {
            car.setMode("cooper");
        } else {
            car.setMode(temp[1]);
        }

        // title
        String title2 = driver.findElement(By.xpath(".//div[@class=\"heading-4 font-weight-normal mb-0_75\"]")).getText().toLowerCase();
        index = title2.indexOf("\n");
        car.setTitle(title1 + " " + title2.substring(0, index));

        // vin
        String vinSpan = driver.findElement(By.xpath(".//div[@class=\"medium text-gray mt-0_5\"]/span")).getText().toLowerCase();
        temp = vinSpan.split(" ");
        car.setVin(temp[1].trim());

        // mileage and gas
        List<WebElement> summary = driver.findElements(
                By.xpath(".//section[@class=\"vehicle-summary text-gray-darker\"]//div[@class=\"m-0 mb-1 row\"]"));
        for (WebElement element: summary) {
            try {
                WebElement icon = element.findElement(By.xpath(".//i"));
                String title = icon.getAttribute("title");
                if ("Mileage".equals(title)) {
                    String mileageString = element.findElement(By.xpath(".//div[@class='col']")).getText().trim();
                    String numberString = mileageString.split(" ")[0];
                    car.setMileage(NumberFormat.getNumberInstance(Locale.US).parse(numberString).intValue());
                }
            } catch (NoSuchElementException e) {

            }
        }

        // specs
        try {
            wait.until(presenceOfElementLocated(By.xpath(".//div[@class=\"modal-open-button-container\"]//button"))).click();
            WebElement modalContent = wait.until(
                    presenceOfElementLocated(By.xpath(".//div[@class=\"modal-content modal-content\"]")));
            List<WebElement> specElementList = wait.until(
                    visibilityOfNestedElementsLocatedBy(modalContent, By.xpath(".//li//span")));
            StringBuilder builder = new StringBuilder();
            for (WebElement element : specElementList) {
                builder.append(element.getText()).append(", ");
            }
            car.setSpec(builder.toString());
            driver.findElement(By.xpath(".//button[@class=\"modal-close-button p-0 btn btn-link\"]")).click();
        } catch (TimeoutException e) {
            try {
                WebElement featureSection = wait.until(
                        presenceOfElementLocated(
                                By.xpath(".//section[@data-tracking-parent=\"edm-entry-features-specs\"]")));
                List<WebElement> specElementList = featureSection.findElements(By.xpath(".//li"));
                StringBuilder builder = new StringBuilder();
                for (WebElement element : specElementList) {
                    builder.append(element.getText()).append(", ");
                }
                car.setSpec(builder.toString());
            } catch (TimeoutException e2) {
                logger.info("Skip specList for car({})", car.getVin());
            }
        }

        // description
        try {
            WebElement element = wait.until(presenceOfElementLocated(
                    By.xpath(".//div[@class=\"content-collapse\"]//div[@class=\"mb-0_5\"]")));
            Thread.sleep(1000);
            car.setDescription(element.getText());
        } catch (TimeoutException e) {
            car.setDescription("");
            logger.info("Skip description for car({})", car.getVin());
        }

        // price
        WebElement priceSpan = driver.findElement(By.xpath(".//span[@data-test=\"vdp-price-row\"]"));
        String priceString = priceSpan.getText().substring(1);
        car.setPrice(NumberFormat.getNumberInstance(Locale.US).parse(priceString).intValue());

        // image
        try {
            driver.findElement(By.xpath(".//button[@data-tracking-id=\"open_legacy_photoflipper\"]")).click();
            WebElement modalElement = wait.until(visibilityOfElementLocated(By.xpath(".//div[@class=\"modal-content\"]")));
            String imageUrl = modalElement.findElement(By.xpath(".//img")).getAttribute("src");
            car.setImageUrl(imageUrl);
            modalElement.findElement(By.xpath(".//button[@class=\"pf-close-btn close btn btn-secondary\"]")).click();
        } catch (Exception e) {
            car.setImageUrl("");
            logger.info("Skip imageUrl for car({})", car.getVin());
        }
        return car;
    }
}

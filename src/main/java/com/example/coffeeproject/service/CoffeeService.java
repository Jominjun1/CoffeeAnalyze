package com.example.coffeeproject.service;

import com.example.coffeeproject.Utill.WebDriverConfig;
import com.example.coffeeproject.model.Ingredient;
import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.respoitory.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class CoffeeService {

    private final IngredientRepository ingredientRepository;
    private final PaiksRepository paiksRepository;
    private final WebDriverConfig webDriverConfig;

    @Autowired
    public CoffeeService(
                        IngredientRepository ingredientRepository,
                         PaiksRepository paiksRepository, WebDriverConfig webDriverConfig) {
        this.ingredientRepository = ingredientRepository;
        this.paiksRepository = paiksRepository;
        this.webDriverConfig = webDriverConfig;
    }

    public void crawlPaiksCoffee() {
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        Map<String, String> categoryMap = Map.of(
                "커피", "https://paikdabang.com/menu/menu_coffee/",
                "음료", "https://paikdabang.com/menu/menu_drink/",
                "디저트", "https://paikdabang.com/menu/menu_dessert/",
                "빽스치노", "https://paikdabang.com/menu/menu_ccino/"
        );

        try {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String category = entry.getKey();
                String url = entry.getValue();

                webDriver.get(url);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.menu_list.clear > ul > li")));
                List<WebElement> menuItems = webDriver.findElements(By.cssSelector("div.menu_list.clear > ul > li"));
                System.out.println("크롤링한 메뉴 개수: " + menuItems.size());

                for (WebElement item : menuItems){
                    try {
                        Actions actions = new Actions(webDriver);
                        actions.moveToElement(item).perform();
                        Thread.sleep(300);
                        WebElement hover = item.findElement(By.cssSelector("div.hover"));

                        JavascriptExecutor js = (JavascriptExecutor) webDriver;
                        js.executeScript("arguments[0].style.display='block';", hover);

                        String name = item.findElement(By.cssSelector("p.menu_tit")).getText();
                        String engName = hover.findElement(By.cssSelector("div.menu_tit2.color-1")).getText();
                        String note = hover.findElement(By.cssSelector("p.txt")).getText();
                        String imageUrl = item.findElement(By.cssSelector("div.thumb img")).getAttribute("src");
                        String allergic;
                        try {
                            allergic = hover.findElement(By.cssSelector("div.ingredient_table_box")).getText();
                        } catch (NoSuchElementException e) {
                            allergic = "알레르기 정보 없음";
                        }
                        double kcal = 0, caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                        try {
                            WebElement table = hover.findElement(By.cssSelector("div.ingredient_table"));
                            List<WebElement> rows = table.findElements(By.tagName("li"));
                            for (WebElement row : rows) {
                                String label = row.findElements(By.tagName("div")).get(0).getText();
                                String valueStr = row.findElements(By.tagName("div")).get(1).getText().replaceAll("[^0-9.]", "");
                                double value = valueStr.isEmpty() ? 0 : Double.parseDouble(valueStr);
                                switch (label) {
                                    case "칼로리":
                                        kcal = value; break;
                                    case "카페인":
                                        caffeine = value; break;
                                    case "나트륨":
                                        sodium = value; break;
                                    case "당류":
                                        sugar = value; break;
                                    case "포화지방":
                                        saturatedFat = value; break;
                                    case "단백질":
                                        protein = value; break;
                                }
                            }
                        } catch (NoSuchElementException e) {
                            System.out.println("영양정보 없음: " + name);
                        }
                        Optional<PaiksCoffee> optPaiks = paiksRepository.findByName(name);
                        if (optPaiks.isPresent()) {
                            PaiksCoffee paiksCoffee = optPaiks.get();
                            Ingredient ingredient = paiksCoffee.getIngredients();

                            boolean isDifferent =
                                    Math.abs(ingredient.getKcal() - kcal) > 0.001 ||
                                            Math.abs(ingredient.getCaffeine() - caffeine) > 0.001 ||
                                            Math.abs(ingredient.getSodium() - sodium) > 0.001 ||
                                            Math.abs(ingredient.getSugar() - sugar) > 0.001 ||
                                            Math.abs(ingredient.getSaturated_fat() - saturatedFat) > 0.001 ||
                                            Math.abs(ingredient.getProtein() - protein) > 0.001 ||
                                            !Objects.equals(ingredient.getAllergic_ingredients(), allergic);

                            if (isDifferent) {
                                ingredient.setKcal(kcal);
                                ingredient.setCaffeine(caffeine);
                                ingredient.setSodium(sodium);
                                ingredient.setSugar(sugar);
                                ingredient.setSaturated_fat(saturatedFat);
                                ingredient.setProtein(protein);
                                ingredient.setAllergic_ingredients(allergic);
                                ingredientRepository.save(ingredient);

                                paiksCoffee.setNote("[" + category + "] " + note);
                                paiksCoffee.setImageUrl(imageUrl);
                                paiksRepository.save(paiksCoffee);
                            }
                        } else {
                            Ingredient ingredient = new Ingredient();
                            ingredient.setKcal(kcal);
                            ingredient.setCaffeine(caffeine);
                            ingredient.setSodium(sodium);
                            ingredient.setSugar(sugar);
                            ingredient.setSaturated_fat(saturatedFat);
                            ingredient.setProtein(protein);
                            ingredient.setAllergic_ingredients(allergic);
                            ingredientRepository.save(ingredient);

                            PaiksCoffee paiksCoffee = new PaiksCoffee();
                            paiksCoffee.setName(name);
                            paiksCoffee.setEng_name(engName);
                            paiksCoffee.setImageUrl(imageUrl);
                            paiksCoffee.setNote("[" + category + "] " + note);
                            paiksCoffee.setPrice(0);
                            paiksCoffee.setOunce(0);
                            paiksCoffee.setIngredients(ingredient);
                            paiksRepository.save(paiksCoffee);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("크롤링 실패: " + e.getMessage());
                        break;
                    }
                }
            }
        } finally {
            webDriver.quit();
        }
    }

    public void crawlMegaCoffee() {
        WebDriver webDriver = webDriverConfig.create();
        WebDriverWait wait = new WebDriverWait(webDriver , Duration.ofSeconds(15));
        Map<String , String> categoryMap = Map.of(
                "음료" , "https://www.mega-mgccoffee.com/menu/?menu_category1=1&menu_category2=1/",
                "푸드" , "https://www.mega-mgccoffee.com/menu/?menu_category1=1&menu_category2=2/",
                "상품" , "https://www.mega-mgccoffee.com/menu/?menu_category1=3&menu_category2=3/"
        );

        try{
            for(Map.Entry<String , String> entry : categoryMap.entrySet()){
                String category = entry.getKey();
                String url = entry.getValue();

                webDriver.get(url);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#menu_list > li")));
                int currentPage = 1;

                while (true) {
                    List<WebElement> items = webDriver.findElements(By.cssSelector("ul#menu_list > li"));
                    System.out.println(category + " - " + currentPage + "페이지: " + items.size() + "개");

                    for (WebElement item : items) {
                        try {
                            Actions actions = new Actions(webDriver);
                            actions.moveToElement(item).perform();
                            WebElement innerDiv = item.findElement(By.cssSelector("div.inner_modal"));
                            JavascriptExecutor js = (JavascriptExecutor) webDriver;
                            js.executeScript("arguments[0].style.display='block';", innerDiv);

                            WebElement modal = webDriver.findElement(By.cssSelector("div.inner_modal"));
                            String name = innerDiv.findElement(By.cssSelector("b")).getText();

                            System.out.println("이름: " + name);

                            WebElement closeBtn = modal.findElement(By.cssSelector("button.close"));
                            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", closeBtn);
                            wait.until(ExpectedConditions.invisibilityOf(modal));
                            Thread.sleep(500);

                        } catch (Exception e) {
                            System.out.println("모달 크롤링 실패: " + e.getMessage());
                        }
                    }
                    List<WebElement> pageLinks = webDriver.findElements(By.cssSelector("ul#board_page > li > a.board_page_link"));
                    if (currentPage < pageLinks.size()) {
                        WebElement nextPageLink = pageLinks.get(currentPage); // 0-based index
                        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", nextPageLink);
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#menu_list > li")));
                        Thread.sleep(1000);
                        currentPage++;
                    } else {
                        break;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("크롤링 실패: " + e.getMessage());
        }
    }
}

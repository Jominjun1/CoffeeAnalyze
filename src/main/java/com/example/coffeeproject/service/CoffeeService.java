package com.example.coffeeproject.service;

import com.example.coffeeproject.Utill.WebDriverConfig;
import com.example.coffeeproject.model.Ingredient;
import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.respoitory.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
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

                        String name = item.findElement(By.cssSelector("p.menu_tit")).getText();
                        String engName = hover.findElement(By.cssSelector("div.menu_tit2.color-1")).getText();
                        System.out.println("메뉴 영어 이름: " + engName);
                        String note = hover.findElement(By.cssSelector("p.txt")).getText();
                        System.out.println("설명: " + note);
                        String imageUrl = item.findElement(By.cssSelector("div.thumb img")).getAttribute("src");
                        System.out.println("이미지 링크: " + imageUrl);
                        String allergic;
                        try {
                            allergic = hover.findElement(By.cssSelector("div.ingredient_table_box")).getText();
                        } catch (NoSuchElementException e) {
                            allergic = "알레르기 정보 없음";
                        }
                        System.out.println("알레르기: " + allergic);

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

                                System.out.println("업데이트: " + name);
                            } else {
                                System.out.println("이미 최신 상태: " + name);
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

                            System.out.println("신규 등록: " + name);
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
}

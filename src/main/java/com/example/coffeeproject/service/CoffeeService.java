package com.example.coffeeproject.service;

import com.example.coffeeproject.Utill.WebDriverConfig;
import com.example.coffeeproject.model.Ingredient;
import com.example.coffeeproject.model.MegaCoffee;
import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.respoitory.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CoffeeService {

    private final IngredientRepository ingredientRepository;
    private final PaiksRepository paiksRepository;
    private final MegaRepository  megaRepository;
    private final WebDriverConfig webDriverConfig;

    @Autowired
    public CoffeeService(
            IngredientRepository ingredientRepository,
            PaiksRepository paiksRepository, MegaRepository megaRepository, WebDriverConfig webDriverConfig) {
        this.ingredientRepository = ingredientRepository;
        this.paiksRepository = paiksRepository;
        this.megaRepository = megaRepository;
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
                        double ounce =0;
                        String allergic = "알레르기 정보 없음";
                        try {
                            WebElement baseInfo = hover.findElement(By.cssSelector("p.menu_ingredient_basis"));
                            String text = baseInfo.getText();
                            System.out.println("기준 정보 : " + text);
                            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(oz|ml)");
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                double value = Double.parseDouble(matcher.group(1));
                                String unit = matcher.group(3).toLowerCase();
                                ounce = switch (unit) {
                                    case "oz" -> Math.round(value * 29.5 * 10) / 10.0;
                                    case "ml" -> Math.round(value * 10) / 10.0;
                                    case "g" -> value;
                                    default -> ounce;
                                };
                            }
                            String fullInfo = hover.findElement(By.cssSelector("div.ingredient_table_box")).getText();
                            String[] lines = fullInfo.split("\\r?\\n");
                            allergic = Arrays.stream(lines)
                                    .filter(line -> line.contains("알레르기"))
                                    .findFirst()
                                    .orElse("알레르기 정보 없음");
                        } catch (NoSuchElementException e) {
                            System.out.println("정보 없음");
                        }
                        double kcal = 0, caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                        try{
                            List<WebElement> rows = hover.findElements(By.cssSelector("ul.ingredient_table > li"));
                            for (WebElement row : rows) {
                                String text = row.getText().trim();
                                if (text.startsWith("칼로리")) {
                                    kcal = extractNumber(text);
                                } else if (text.startsWith("카페인")) {
                                    caffeine = extractNumber(text);
                                } else if (text.startsWith("나트륨")) {
                                    sodium = extractNumber(text);
                                } else if (text.startsWith("당류")) {
                                    sugar = extractNumber(text);
                                } else if (text.startsWith("포화지방")) {
                                    saturatedFat = extractNumber(text);
                                } else if (text.startsWith("단백질")) {
                                    protein = extractNumber(text);
                                }
                            }
                        } catch (NoSuchElementException e) {
                            System.out.println("영양정보 없음: " + name);
                        }
                        System.out.printf(
                                "용량 : %.1fml, 칼로리 : %.1fkcal, 카페인 : %.1fmg, 나트륨 : %.1fmg, 당류 : %.1fg, 포화지방 : %.1fg, 단백질 : %.1fg%n",
                                ounce, kcal, caffeine, sodium, sugar, saturatedFat, protein
                        );
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
                                paiksCoffee.setOunce(ounce);
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
                            paiksCoffee.setOunce(ounce);
                            paiksCoffee.setImageUrl(imageUrl);
                            paiksCoffee.setNote("[" + category + "] " + note);
                            paiksCoffee.setPrice(0);
                            paiksCoffee.setIngredients(ingredient);
                            paiksRepository.save(paiksCoffee);
                        }
                    } catch (Exception e) {
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
                "푸드" , "https://www.mega-mgccoffee.com/menu/?menu_category1=1&menu_category2=2/"
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
                            String name = innerDiv.findElement(By.cssSelector("b")).getText();
                            String engName = innerDiv.findElement(By.cssSelector("div.cont_text_inner.cont_text_info")).getText();
                            String note = innerDiv.findElement(By.cssSelector("div.cont_text")).getText();
                            String allergic = innerDiv.findElement(By.cssSelector("div.cont_text.cont_text_info")).getText();
                            String imageUrl = item.findElement(By.cssSelector("div.cont_gallery_list_img img")).getAttribute("src");
                            List<WebElement> infoList = innerDiv.findElements(By.cssSelector("div.cont_text_inner"));

                            double ounce =0;
                            double kcal =0;
                            for(WebElement info : infoList){
                                String text= info.getText();
                                System.out.println(text);
                                if(text.endsWith("ml")){
                                    String number = text.replaceAll("[^0-9]" ,"");
                                    if(!number.isEmpty()){
                                        ounce = Integer.parseInt(number);
                                    }
                                }else if(text.endsWith("oz")){
                                    String number = text.replaceAll("[^0-9]" ,"");
                                    if(!number.isEmpty()){
                                        ounce = (int) (Integer.parseInt(number) * 29.5);
                                    }
                                }else if(text.endsWith("kcal")){
                                    String number = text.replaceAll("1회" , "").replaceAll("[^0-9.]" ,"");
                                    if(!number.isEmpty()){
                                        kcal = Double.parseDouble(number);
                                    }
                                }
                            }
                            double caffeine = 0, sodium = 0, sugar = 0, saturatedFat = 0, protein = 0;
                            try{
                                WebElement elementList = innerDiv.findElement(By.cssSelector("div.cont_list ul"));
                                List<WebElement> rows = elementList.findElements(By.tagName("li"));

                                for (WebElement row : rows) {
                                    String text = row.getText().trim();
                                    if (text.startsWith("카페인")) {
                                        caffeine = extractNumber(text);
                                    } else if (text.startsWith("나트륨")) {
                                        sodium = extractNumber(text);
                                    } else if (text.startsWith("당류")) {
                                        sugar = extractNumber(text);
                                    } else if (text.startsWith("포화지방")) {
                                        saturatedFat = extractNumber(text);
                                    } else if (text.startsWith("단백질")) {
                                        protein = extractNumber(text);
                                    }
                                }
                            }catch (NoSuchElementException e) {
                                    System.out.println("영양정보 없음");
                            }
                            System.out.printf(
                                    "용량 : %.1fml, 칼로리 : %.1fkcal, 카페인 : %.1fmg, 나트륨 : %.1fmg, 당류 : %.1fg, 포화지방 : %.1fg, 단백질 : %.1fg%n",
                                    ounce, kcal, caffeine, sodium, sugar, saturatedFat, protein
                            );
                            Optional<MegaCoffee> optMega = megaRepository.findByName(name);
                            if(optMega.isPresent()) {
                                MegaCoffee megaCoffee = optMega.get();
                                Ingredient ingredient = megaCoffee.getIngredients();

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

                                    megaCoffee.setOunce(ounce);
                                    megaCoffee.setNote("[" + category + "] " + note);
                                    megaCoffee.setImageUrl(imageUrl);
                                    megaRepository.save(megaCoffee);
                                }
                            }else {
                                Ingredient ingredient = new Ingredient();
                                ingredient.setKcal(kcal);
                                ingredient.setCaffeine(caffeine);
                                ingredient.setSodium(sodium);
                                ingredient.setSugar(sugar);
                                ingredient.setSaturated_fat(saturatedFat);
                                ingredient.setProtein(protein);
                                ingredient.setAllergic_ingredients(allergic);
                                ingredientRepository.save(ingredient);

                                MegaCoffee megaCoffee = new MegaCoffee();
                                megaCoffee.setName(name);
                                megaCoffee.setOunce(ounce);
                                megaCoffee.setEng_name(engName);
                                megaCoffee.setImageUrl(imageUrl);
                                megaCoffee.setNote("[" + category + "] " + note);
                                megaCoffee.setPrice(0);
                                megaCoffee.setOunce(0);
                                megaCoffee.setIngredients(ingredient);
                                megaRepository.save(megaCoffee);
                            }
                        } catch (Exception e) {
                            System.out.println("모달 크롤링 실패: " + e.getMessage());
                        }
                    }
                    List<WebElement> pageLinks = webDriver.findElements(By.cssSelector("ul#board_page > li > a.board_page_link"));
                    if (currentPage < pageLinks.size()) {
                        WebElement nextPageLink = pageLinks.get(currentPage); // 0-based index
                        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", nextPageLink);
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#menu_list > li")));
                        currentPage++;
                    } else {
                        break;
                    }
                }
            }
        }catch (Exception e) {
            System.out.println("크롤링 실패: " + e.getMessage());
        }
    }
    private double extractNumber(String text) {
        String numStr = text.replaceAll("[^0-9.]", "");
        if (!numStr.isEmpty()) {
            return Double.parseDouble(numStr);
        }
        return 0.0;
    }
}

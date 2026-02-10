package id.ac.ui.cs.advprog.eshop.functional;

import io.github.bonigarcia.seljup.SeleniumJupiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SeleniumJupiter.class)
class CreateProductFunctionalTest {

    @LocalServerPort
    private int serverPort;

    @Value("${app.baseUrl:http://localhost}")
    private String testBaseUrl;

    private String baseUrl;

    @BeforeEach
    void setupTest() {
        baseUrl = String.format("%s:%d", testBaseUrl, serverPort);
    }

    @Test
    void createProduct_isCorrect(ChromeDriver driver) throws Exception {
        // Exercise
        driver.get(baseUrl + "/product/list");

        // Click Create Product button
        driver.findElement(By.linkText("Create Product")).click();

        // Fill the form
        String productName = "Test Product";
        int productQuantity = 10;

        driver.findElement(By.id("nameInput")).sendKeys(productName);
        driver.findElement(By.id("quantityInput")).sendKeys(String.valueOf(productQuantity));

        // Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify we are back on the list page
        String currentUrl = driver.getCurrentUrl();
        // Depending on configuration, it might contain query params or not, but usually ends with /product/list
        // or we can check the title
        assertEquals("Product List", driver.getTitle());

        // Verify the product is in the list
        // Simple check: page source contains the strings
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains(productName));
        assertTrue(pageSource.contains(String.valueOf(productQuantity)));
        
        // More specific check (optional but better): verify it's in a table cell
        boolean productFound = driver.findElements(By.tagName("td")).stream()
                .anyMatch(element -> element.getText().equals(productName));
        assertTrue(productFound, "Product name should be present in the table");
    }
}

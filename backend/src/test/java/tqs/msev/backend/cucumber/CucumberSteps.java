package tqs.msev.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class CucumberSteps {
    private final WebDriver driver;
    private final Wait<WebDriver> wait;

    public CucumberSteps() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
    }

    private final String webUrl = System.getProperty("webUrl");

    @When("I open the station details view")
    public void iOpenTheStationDetailsView() {
        driver.get(webUrl + "/stations");
    }

    @Then("I should see a list of its chargers")
    public void iShouldSeeAListOfItChargers() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='chargers']")));
    }

    @And("each charger should display its type")
    public void eachChargerShouldDisplayItsType() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='type']")));
    }

    @And("each charger should display its pricing")
    public void eachChargerShouldDisplayItsPricing() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='price']")));
    }

    @Given("that I am logged in")
    public void thatIAmLoggedIn() {
        driver.get(webUrl + "/login");

        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("test@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='login-btn']")).click();
    }

    @And("Search by name {string}")
    public void searchByName(String name) {
        throw new UnsupportedOperationException("TODO:");
    }

    @When("I open the app")
    public void iOpenTheApp() {
        driver.get(webUrl);
    }

    @And("click on login")
    public void clickOnLogin() {
        driver.findElement(By.cssSelector("[data-testid='nav-login']")).click();
    }

    @And("click on signup")
    public void clickOnSignup() {
        driver.findElement(By.cssSelector("[data-testid='signup-link']")).click();
    }

    @And("fill the signup form")
    public void fillTheSignupForm() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='name-input']")));

        driver.findElement(By.cssSelector("[data-testid='name-input']")).sendKeys("Test");
        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("testing@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='confirm-password-input']")).sendKeys("123");
    }

    @And("submit the signup form")
    public void submitTheSignupForm() {
        driver.findElement(By.cssSelector("[data-testid='signup-btn']")).click();
    }

    @Then("I should be redirected to the home page")
    public void iShouldBeRedirectedToTheHomePage() {
        wait.until(ExpectedConditions.urlToBe(webUrl + "/"));
        String url = driver.getCurrentUrl();

        assertThat(url).isEqualTo(webUrl + "/");
    }

    @And("I should see a logout button")
    public void iShouldSeeALogoutButton() {
        assertThat(driver.findElement(By.cssSelector("[data-testid='nav-logout']")).isDisplayed()).isTrue();
    }

    @And("fill the signup form with invalid password")
    public void fillTheSignupFormWithInvalidPassword() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='name-input']")));

        driver.findElement(By.cssSelector("[data-testid='name-input']")).sendKeys("Test");
        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("testing@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='confirm-password-input']")).sendKeys("1234");
    }

    @Then("I should see an alert saying {string}")
    public void iShouldSeeAnAlertSaying(String message) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#swal2-html-container")));

        String msg = driver.findElement(By.cssSelector("#swal2-html-container")).getText();

        assertThat(msg).isEqualTo(message);
    }
}
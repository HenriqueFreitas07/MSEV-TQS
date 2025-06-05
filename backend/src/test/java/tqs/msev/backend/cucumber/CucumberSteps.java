package tqs.msev.backend.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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
        options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
    }

    private final String webUrl = System.getProperty("webUrl");

    @When("I open the station discovery view")
    public void iOpenTheStationDiscoveryView() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='nav-stations']")));

        driver.findElement(By.cssSelector("[data-testid='nav-stations']")).click();
    }

    @Given("that I am logged in")
    public void thatIAmLoggedIn() {
        driver.get(webUrl + "/login");

        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("test_user@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='login-btn']")).click();
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

    @After
    public void cleanup() {
        driver.quit();
    }

    @And("Click on a station")
    public void clickOnAStation() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div[3]/div/div/div/div[3]/div[1]/div[2]/div/div[3]/div/gmp-advanced-marker")));

        driver.findElement(By.xpath("//*[@id=\"root\"]/div[3]/div/div/div/div[3]/div[1]/div[2]/div/div[3]/div/gmp-advanced-marker")).click();
    }

    @Then("I should see a title {string}")
    public void iShouldSeeATitle(String title) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-name']")));

        String actualTitle = driver.findElement(By.cssSelector("[data-testid='station-name']")).getText();

        assertThat(actualTitle).isEqualTo(title);
    }

    @And("I should see {int} charger cards")
    public void iShouldSeeTwoChargerCards(int amount) {
        var elements = driver.findElements(By.cssSelector("[data-testid='charger-card']"));

        assertThat(elements).hasSize(amount);
    }

    @Given("that I have made a station search")
    public void thatIHaveMadeAStationSearch() {
        driver.get(webUrl + "/login");

        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("test_user@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='login-btn']")).click();
        wait.until(ExpectedConditions.urlContains("stations"));
    }

    @Then("I should see a list of its chargers")
    public void iShouldSeeAListOfItsChargers() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-name']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='charger-card']"));

        assertThat(elements).hasSizeGreaterThan(0);
    }

    @And("each charger should display its type")
    public void eachChargerShouldDisplayItsType() {
        var elements = driver.findElements(By.cssSelector("[data-testid='type']"));

        assertThat(elements).hasSizeGreaterThan(0);
    }

    @And("each charger should display its pricing")
    public void eachChargerShouldDisplayItsPricing() {
        var elements = driver.findElements(By.cssSelector("[data-testid='price']"));

        assertThat(elements).hasSizeGreaterThan(0);
    }

    @Then("I should see a real-time indicator of each charger’s status")
    public void iShouldSeeARealTimeIndicatorOfEachChargerSStatus() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='status']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='status']"));

        assertThat(elements).hasSizeGreaterThan(0);
    }

    @And("the status should list if the charger is booked in the near future.")
    public void theStatusShouldListIfTheChargerIsBookedInTheNearFuture() {
        var elements = driver.findElements(By.cssSelector("[data-testid='availability-btn']"));

        elements.get(0).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='charger-reservations']")));

        String text = driver.findElement(By.cssSelector("[data-testid='charger-reservations']")).getText();

        assertThat(text).isEqualTo("Charger Reservations");
    }

    @Given("that I am viewing a charging station with available booking slots")
    public void thatIAmViewingAChargingStationWithAvailableBookingSlots() {
        driver.get(webUrl + "/login");

        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("test_user@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='login-btn']")).click();

        wait.until(ExpectedConditions.urlContains("stations"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div[3]/div/div/div/div[3]/div[1]/div[2]/div/div[3]/div/gmp-advanced-marker")));

        driver.findElement(By.xpath("//*[@id=\"root\"]/div[3]/div/div/div/div[3]/div[1]/div[2]/div/div[3]/div/gmp-advanced-marker")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-name']")));
    }

    @When("I select a date and time and confirm the reservation")
    public void iSelectADateAndTimeAndConfirmTheReservation() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='reserve-btn']")));

        var elements = driver.findElements(By.cssSelector("[data-testid='reserve-btn']"));
        elements.get(0).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='timeslot']")));

        driver.findElements(By.cssSelector("[data-testid='timeslot']")).get(0).click();
        driver.findElement(By.cssSelector("[data-testid='reserve-now-btn']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='tr-timeslot']")));

        elements = driver.findElements(By.cssSelector("[data-testid='tr-timeslot']"));

        assertThat(elements).hasSize(1);
        driver.findElement(By.cssSelector("[data-testid='confirm-reservation-btn']")).click();
    }

    @Then("the system should lock the selected time slot for my user account")
    public void theSystemShouldLockTheSelectedTimeSlotForMyUserAccount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='gray-timeslot']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='gray-timeslot']"));

        assertThat(elements).hasSize(1);
    }

    @And("my booking should appear in my bookings page")
    public void myBookingShouldAppearInMyBookingsPage() {
        driver.findElement(By.cssSelector("[data-testid='nav-my-reserves']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='reserve']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='reserve']"));
        assertThat(elements).hasSize(1);
    }

    @Given("that I am logged in as operator")
    public void thatIAmLoggedInAsOperator() {
        driver.get(webUrl + "/login");

        driver.findElement(By.cssSelector("[data-testid='login-input']")).sendKeys("operator@gmail.com");
        driver.findElement(By.cssSelector("[data-testid='password-input']")).sendKeys("123");
        driver.findElement(By.cssSelector("[data-testid='login-btn']")).click();
    }

    @When("I access the station monitoring section")
    public void iAccessTheStationMonitoringSection() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='nav-dashboard']")));
        driver.findElement(By.cssSelector("[data-testid='nav-dashboard']")).click();
    }

    @Then("I should see a list of all my stations with status indicators")
    public void iShouldSeeAListOfAllMyStationsWithStatusIndicators() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-card']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='station-card']"));
        assertThat(elements).hasSizeGreaterThan(0);
    }

    @When("I access a station in the dashboard")
    public void iAccessAStationInTheDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='nav-dashboard']")));
        driver.findElement(By.cssSelector("[data-testid='nav-dashboard']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-card']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='station-card']"));
        elements.get(0).click();
    }

    @And("click add charger")
    public void clickAddCharger() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='add-charger-btn']")));

        driver.findElement(By.cssSelector("[data-testid='add-charger-btn']")).click();
    }

    @And("fill the charger form with valid details")
    public void fillTheChargerFormWithValidDetails() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='connector-select']")));

        Select select = new Select(driver.findElement(By.cssSelector("[data-testid='connector-select']")));
        select.selectByIndex(1);

        driver.findElement(By.cssSelector("[data-testid='price']")).sendKeys("10");
        driver.findElement(By.cssSelector("[data-testid='speed']")).sendKeys("72");
        driver.findElement(By.cssSelector("[data-testid='create-btn']")).click();
    }

    @And("the new station should be added to the network and appear in the monitoring dashboard")
    public void theNewStationShouldBeAddedToTheNetworkAndAppearInTheMonitoringDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".swal2-confirm")));

        driver.findElement(By.cssSelector(".swal2-confirm")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='charger-card']")));
        var elements = driver.findElements(By.cssSelector("[data-testid='charger-card']"));
        assertThat(elements).hasSizeGreaterThan(2);
    }

    @And("fill the charger form with invalid details")
    public void fillTheChargerFormWithInvalidDetails() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='connector-select']")));

        Select select = new Select(driver.findElement(By.cssSelector("[data-testid='connector-select']")));
        select.selectByIndex(1);

        driver.findElement(By.cssSelector("[data-testid='price']")).sendKeys("10");
        driver.findElement(By.cssSelector("[data-testid='create-btn']")).click();
    }

    @Then("The charger won't be created")
    public void theChargerWonTBeCreated() {
        var elements = driver.findElements(By.cssSelector(".swal2-confirm"));

        assertThat(elements).isEmpty();
    }
}
package tqs.msev.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

// public class CucumberSteps {
//     private WebDriver driver = new ChromeDriver();
//     private Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));

    // private UUID stationId;

    // @Given("that I have made a station search")
    // public void thatIHaveMadeAStationSearch() {
    // driver.get("https://localhost:5173/station/" + stationId);
    // }

    // @When("I open the station details view")
    // public void iOpenTheStationDetailsView() {
    // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='station']")));
    // }

    // @Then("I should see a list of its chargers")
    // public void iShouldSeeAListOfItChargers() {
    // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='chargers']")));
    // }

    // @And("each charger should display its type")
    // public void eachChargerShouldDisplayItsType() {
    // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='type']")));
    // }

    // @And("each charger should display its pricing")
    // public void eachChargerShouldDisplayItsPricing() {
    // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='price']")));
    // }

// }
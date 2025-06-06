@MSEV-19
Feature: Register in the application
  Scenario: Valid sign up
    When I open the app
    And click on login
    And click on signup
    And fill the signup form
    And submit the signup form
    Then I should be redirected to the home page
    And I should see a logout button

  Scenario: Invalid sign up
    When I open the app
    And click on login
    And click on signup
    And fill the signup form with invalid password
    And submit the signup form
    Then I should see an alert saying "Passwords mismatch"

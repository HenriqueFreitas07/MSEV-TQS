@MSEV-23
Feature: Create Charger
  Scenario: Create Valid Charger
    Given that I am logged in as operator
    When I access a station in the dashboard
    And click add charger
    And fill the charger form with valid details
    And the new station should be added to the network and appear in the monitoring dashboard

  Scenario: Create Invalid Charger
    Given that I am logged in as operator
    When I access a station in the dashboard
    And click add charger
    And fill the charger form with invalid details
    Then The charger won't be created

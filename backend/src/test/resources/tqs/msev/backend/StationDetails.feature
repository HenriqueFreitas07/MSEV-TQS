@MSEV-16
Feature: Search for a station
  Scenario: Search for a valid station details
    Given that I am logged in
    When I open the station discovery view
    And Click on a station
    Then I should see a title "Station GetCharged"
    And I should see some charger cards
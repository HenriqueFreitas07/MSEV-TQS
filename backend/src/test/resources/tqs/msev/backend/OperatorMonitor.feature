@MSEV-21
Feature: Monitor the stations
  Scenario: Monitor the stations
    Given that I am logged in as operator
    When I access the station monitoring section
    Then I should see a list of all my stations with status indicators

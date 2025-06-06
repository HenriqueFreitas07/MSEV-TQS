@MSEV-18
Feature: See charger details
  Scenario: See details of a charger
    Given that I have made a station search
    When Click on a station
    Then I should see a list of its chargers
    And each charger should display its type
    And each charger should display its pricing


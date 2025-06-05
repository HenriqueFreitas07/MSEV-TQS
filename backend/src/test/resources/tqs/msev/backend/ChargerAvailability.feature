@MSEV-17
Feature: See charger details
  Scenario: See details of a charger
    Given that I have made a station search
    When Click on a station
    Then I should see a real-time indicator of each charger’s status
    And the status should list if the charger is booked in the near future.

Feature: Search for a station details
  Scenario: Search for a valid station details
    Given that I have made a station search
    When I open the station details view
    Then I should see a list of its chargers
    And each charger should display its type
    And each charger should display its pricing
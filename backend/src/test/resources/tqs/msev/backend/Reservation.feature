@MSEV-19
Feature: Make a reservation
  Scenario: Make a valid reservation
    Given that I am viewing a charging station with available booking slots
    When I select a date and time and confirm the reservation
    Then the system should lock the selected time slot for my user account
    And my booking should appear in my bookings page


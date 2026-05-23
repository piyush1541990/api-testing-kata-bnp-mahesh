# Tests for the hotel booking API
# Based on the swagger spec in src/test/resources/spec/booking.yaml
# Website: https://automationintesting.online

Feature: Booking API Tests

  # Test 1: health check - is the API  running?

  Scenario: API health check returns UP status
    Given I am logged in as admin
    When I check if the booking API is running
    Then the response status should be 200
    And the API status should be UP

  # Test 2: create a new booking with all required fields

  Scenario: Create a new booking successfully
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email           | phone       |
      | 1      | Mahesh    | Takle    | true        | 2040-01-10 | 2040-01-14 | mahesh@test.com | 12345678901 |
    Then the booking was created successfully
    And the response should have a booking ID
    And the booking firstname should be "Mahesh"
    And the booking lastname should be "Takle"

  # Test 3: validation - missing roomid should return 400
  Scenario: Create a booking with a firstname that is too short returns 400
    When I try to create a booking with a firstname that is too short
    Then the response status should be 400
    And the response should have a validation error message

  # Test 4: get a booking by ID - needs token
  Scenario: Get a booking by ID returns correct details
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone       |
      | 1      | John      | Tendulkar    | false       | 2040-02-10 | 2040-02-14 | john@test.com | 12345678901 |
    Then the booking was created successfully
    When I get the booking I just created
    Then the response status should be 200
    And the response firstname should be "John"
    And the response should have valid checkin and checkout dates

  # Test 5: get a booking without a token - should be rejected
  Scenario: Get a booking without a token is rejected
    When I try to get booking 1 without a token
    Then the response status should be 403

  # Test 6: update a booking with PUT
  Scenario: Update a booking lastname with PUT
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone       |
      | 1      | Rohit      | Sharma     | true        | 2040-03-10 | 2040-03-14 | test@test.com | 12345678901 |
    Then the booking was created successfully
    When I update the booking lastname to "UpdatedUser"
    Then the response status should be 200
    And the updated lastname should be "UpdatedUser"

  # Test 7: create two bookings independently
  Scenario: Create two bookings independently
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email          | phone       |
      | 1      | Alice     | Gambhir  | false       | 2040-04-10 | 2040-04-14 | alice@test.com | 12345678901 |
    Then the booking was created successfully
    And the booking firstname should be "Alice"
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email        | phone       |
      | 1      | Bob       | Gavaskar | true        | 2040-05-10 | 2040-05-14 | bob@test.com | 12345678901 |
    Then the booking was created successfully
    And the booking firstname should be "Bob"

  # Test 8: full flow - create, update, delete
  Scenario: Full booking lifecycle - create update and delete
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone       |
      | 1      | Rahul | Dravid     | true        | 2040-06-10 | 2040-06-14 | life@test.com | 12345678901 |
    Then the booking was created successfully
    And the response should have a booking ID
    When I update the booking lastname to "Updated"
    Then the response status should be 200
    When I delete the booking I just created
    Then the response status should be 202
    And the booking should no longer exist

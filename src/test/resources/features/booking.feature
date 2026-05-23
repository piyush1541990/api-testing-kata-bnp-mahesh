# Tests for the hotel booking API

Feature: Booking API Tests

  # Test 1: health check - to verify if API is running

  Scenario: API health check returns UP status
    Given I am logged in as admin
    When I check if the booking API is running
    Then the response status should be 200
    And the API status should be UP

  # Test 2: create a new booking with all required fields

  Scenario: Create a new booking successfully
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone         |
      | 1      | Mahesh    | Takle    | true        | 2025-09-01 | 2025-09-05 | mahesh@test.com    | 12345678901   |
    Then the response status should be 200
    And the response should have a booking ID
    And the booking firstname should be "Mahesh"
    And the booking lastname should be "Takle"

  # Test 3: validation - firstname too short should return 400

  Scenario: Create a booking with a firstname that is too short returns 400
    When I try to create a booking with a firstname that is too short
    Then the response status should be 400
    And the response should have a validation error message

  # Test 4: get a booking by ID - needs token

  Scenario: Get a booking by ID returns correct details
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone         |
      | 1      | John      | Smith    | false       | 2025-10-01 | 2025-10-03 | john@test.com      | 12345678901   |
    Then the response status should be 200
    When I get the booking I just created
    Then the response status should be 200
    And the response firstname should be "John"
    And the response should have valid checkin and checkout dates

  # Test 5: get a booking without a token - should be 401

  Scenario: Get a booking without a token returns 401
    When I try to get booking 1 without a token
    Then the response status should be 401

  # Test 6: update a booking with PUT

  Scenario: Update a booking lastname with PUT
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone         |
      | 1      | Test      | User     | true        | 2025-11-01 | 2025-11-05 | test@test.com      | 12345678901   |
    Then the response status should be 200
    When I update the booking lastname to "UpdatedUser"
    Then the response status should be 200
    And the updated lastname should be "UpdatedUser"

  # Test 7: partial update with PATCH

  Scenario: Partially update a booking with PATCH
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone         |
      | 1      | Patch     | Test     | false       | 2025-12-01 | 2025-12-05 | patch@test.com     | 12345678901   |
    Then the response status should be 200
    When I patch the booking to set depositpaid to "true"
    Then the response status should be 200
    And the depositpaid in the response should be "true"

  # Test 8: full flow - create, get, update, delete

  Scenario: Full booking lifecycle - create update and delete
    Given I am logged in as admin
    When I create a booking with these details:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone         |
      | 1      | Lifecycle | Test     | true        | 2026-01-01 | 2026-01-05 | life@test.com      | 12345678901   |
    Then the response status should be 200
    And the response should have a booking ID
    When I update the booking lastname to "Updated"
    Then the response status should be 200
    When I delete the booking I just created
    Then the response status should be 201
    And the booking should no longer exist

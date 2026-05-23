# Kata API Testing in Java

API Testing and Java Exercise: Setting up a Basic API Test Automation Framework.

## Objective
Set up an API test framework using Java, Rest-Assured and Cucumber against the hotel booking API.

Website: https://automationintesting.online
Booking API docs: https://automationintesting.online/booking/swagger-ui/index.html

## How to run

Java 17 and Maven installed, then run:

```
mvn test
```
Test report will be at: `target/cucumber-reports.html`

## Project structure

```
src/test/java/com/booking/
    api/
        AuthApi.java          - handles login and returns the auth token
        BookingApi.java       - all booking endpoint calls (GET, POST, PUT, PATCH, DELETE)
    models/
        Booking.java          - booking object with all fields from the API spec
        BookingResponse.java  - the response wrapper returned by POST /booking
    stepdefinitions/
        BookingSteps.java     - maps feature file steps to Java code
        TestContext.java      - stores shared data like token and booking ID between steps
    TestRunner.java           - runs all the Cucumber tests

src/test/resources/
    features/
        booking.feature       - all test scenarios in plain English
        messages.feature      - original starter scenario (kept from starter project)
    spec/
        booking.yaml          - API swagger spec (provided with starter project)
```

## What I tested

- Health check - is the API running and returning UP status
- POST /booking - create a booking with all required fields, check fields in response
- POST /booking - validation error when firstname is too short (returns 400)
- GET /booking/{id} - retrieve a booking using auth token, check firstname and date format
- GET /booking/{id} - without a token should return 401 unauthorised
- PUT /booking/{id} - update full booking, check lastname changed
- PATCH /booking/{id} - partial update, check depositpaid changed
- Full lifecycle - create, update, delete and verify it is gone (404)

## Dependencies added

- jackson-databind - converts Java objects to JSON and back
- cucumber-picocontainer - lets Cucumber share data between step definition classes

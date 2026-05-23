package com.booking.stepdefinitions;

import com.booking.api.AuthApi;
import com.booking.api.BookingApi;
import com.booking.models.Booking;
import com.booking.models.BookingResponse;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// connection from feature file to actual Java code

public class BookingSteps {

    private TestContext context;
    private BookingApi bookingApi;
    private AuthApi authApi;

    public BookingSteps(TestContext context) {
        this.context = context;
        this.bookingApi = new BookingApi();
        this.authApi = new AuthApi();
    }

    // ---- auth ----

    @Given("I am logged in as admin")
    public void iAmLoggedInAsAdmin() {
        String token = authApi.getAdminToken();
        assertNotNull(token, "Login failed - token was null");
        context.authToken = token;
    }

    // ---- health check ----

    @When("I check if the booking API is running")
    public void iCheckIfTheBookingAPIIsRunning() {
        context.response = bookingApi.healthCheck();
    }

    @Then("the API status should be UP")
    public void theAPIStatusShouldBeUP() {
        String status = context.response.jsonPath().getString("status");
        assertEquals("UP", status, "API health check failed");
    }

    // ---- create booking ----

    @When("I create a booking with these details:")
    public void iCreateABookingWithTheseDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        Booking.BookingDates dates = new Booking.BookingDates(
                data.get("checkin"),
                data.get("checkout")
        );

        Booking booking = new Booking(
                Integer.parseInt(data.get("roomid")),
                data.get("firstname"),
                data.get("lastname"),
                Boolean.parseBoolean(data.get("depositpaid")),
                dates,
                data.get("email"),
                data.get("phone")
        );

        context.response = bookingApi.createBooking(booking);

        // save the created booking so later steps can use the ID
        if (context.response.getStatusCode() == 200) {
            context.createdBooking = context.response.as(BookingResponse.class);
            context.bookingId = context.createdBooking.bookingid;
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        int actualStatus = context.response.getStatusCode();
        assertEquals(expectedStatus, actualStatus,
                "Expected HTTP " + expectedStatus + " but got " + actualStatus);
    }

    @And("the response should have a booking ID")
    public void theResponseShouldHaveABookingID() {
        assertTrue(context.createdBooking.bookingid > 0,
                "Booking ID should be a positive number");
    }

    @And("the booking firstname should be {string}")
    public void theBookingFirstnameShouldBe(String expected) {
        assertEquals(expected, context.createdBooking.booking.firstname,
                "firstname does not match");
    }

    @And("the booking lastname should be {string}")
    public void theBookingLastnameShouldBe(String expected) {
        assertEquals(expected, context.createdBooking.booking.lastname,
                "lastname does not match");
    }

    // ---- get booking by ID ----

    @When("I get the booking I just created")
    public void iGetTheBookingIJustCreated() {
        context.response = bookingApi.getBookingById(context.bookingId, context.authToken);
    }

    @And("the response firstname should be {string}")
    public void theResponseFirstnameShouldBe(String expected) {
        String actual = context.response.jsonPath().getString("firstname");
        assertEquals(expected, actual, "firstname does not match");
    }

    @And("the response should have valid checkin and checkout dates")
    public void theResponseShouldHaveValidCheckinAndCheckoutDates() {
        String checkin = context.response.jsonPath().getString("bookingdates.checkin");
        String checkout = context.response.jsonPath().getString("bookingdates.checkout");
        // dates should be in YYYY-MM-DD format
        assertTrue(checkin.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkin date format wrong: " + checkin);
        assertTrue(checkout.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkout date format wrong: " + checkout);
    }

    // ---- get booking - not authorised ----

    @When("I try to get booking {int} without a token")
    public void iTryToGetBookingWithoutAToken(int id) {
        // passing empty token to simulate unauthorised request
        context.response = bookingApi.getBookingById(id, "");
    }

    // ---- update booking ----

    @When("I update the booking lastname to {string}")
    public void iUpdateTheBookingLastnameTo(String newLastname) {
        // build updated booking - keeping same data but changing lastname
        Booking.BookingDates dates = new Booking.BookingDates("2025-06-01", "2025-06-05");
        Booking updated = new Booking(1, "Mahesh", newLastname, true, dates,
                "mahesh@test.com", "12345678901");
        context.response = bookingApi.updateBooking(context.bookingId, updated, context.authToken);
    }

    @And("the updated lastname should be {string}")
    public void theUpdatedLastnameShouldBe(String expected) {
        String actual = context.response.jsonPath().getString("lastname");
        assertEquals(expected, actual, "lastname not updated correctly");
    }

    // ---- patch booking ----

    @When("I patch the booking to set depositpaid to {string}")
    public void iPatchTheBookingToSetDepositpaidTo(String value) {
        String body = "{ \"depositpaid\": " + value + " }";
        context.response = bookingApi.patchBooking(context.bookingId, body, context.authToken);
    }

    @And("the depositpaid in the response should be {string}")
    public void theDepositpaidInTheResponseShouldBe(String expected) {
        boolean actual = context.response.jsonPath().getBoolean("depositpaid");
        assertEquals(Boolean.parseBoolean(expected), actual, "depositpaid value mismatch");
    }

    // ---- delete booking ----

    @When("I delete the booking I just created")
    public void iDeleteTheBookingIJustCreated() {
        context.response = bookingApi.deleteBooking(context.bookingId, context.authToken);
    }

    @And("the booking should no longer exist")
    public void theBookingShouldNoLongerExist() {
        // try to get the deleted booking - should return 404
        int status = bookingApi.getBookingById(context.bookingId, context.authToken).getStatusCode();
        assertEquals(404, status, "Booking still exists after delete");
    }

    // ---- validation errors ----

    @When("I try to create a booking with a firstname that is too short")
    public void iTryToCreateABookingWithAFirstnameThatIsTooShort() {
        // firstname must be between 3 and 18 chars - "Jo" should fail
        Booking.BookingDates dates = new Booking.BookingDates("2025-06-01", "2025-06-05");
        Booking booking = new Booking(1, "Jo", "Smith", true, dates,
                "test@test.com", "12345678901");
        context.response = bookingApi.createBooking(booking);
    }

    @And("the response should have a validation error message")
    public void theResponseShouldHaveAValidationErrorMessage() {
        // API returns errors array with message like "size must be between 3 and 18"
        assertFalse(context.response.jsonPath().getList("errors").isEmpty(),
                "Expected validation errors but got none");
    }

}

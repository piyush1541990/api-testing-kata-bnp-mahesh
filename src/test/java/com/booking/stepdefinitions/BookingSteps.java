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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSteps {

    private TestContext context;
    private BookingApi bookingApi;
    private AuthApi authApi;
    private static final Logger log = LoggerFactory.getLogger(BookingSteps.class);

    // Keep the exact Booking we sent in POST so we can reuse it in PUT without waiting for response parsing

    private Booking lastSentBooking;

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

    // ---- generic status check ----

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.response.getStatusCode(),
                "Unexpected status code. Body: " + context.response.getBody().asString());
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

        // Save exactly what we sent — used in PUT later, no re-parsing needed
        lastSentBooking = booking;

        context.response = bookingApi.createBooking(booking);

        int status = context.response.getStatusCode();
        if (status == 200 || status == 201) {
            try {
                String body = context.response.getBody().asString();
                log.info("POST response body: {}", body);
                context.createdBooking = context.response.as(BookingResponse.class);
                context.bookingId = context.createdBooking.bookingid;

                // Handle flat response {"bookingid":"...","firstname":"..."} (no nested "booking")
                if (context.createdBooking.booking == null) {
                    context.createdBooking.booking = lastSentBooking;
                }

            } catch (Exception e) {
                log.error("Failed to parse booking response: {}", e.getMessage());
                log.error("Response body: {}", context.response.getBody().asString());

            }
        }
    }

    @Then("the booking was created successfully")
    public void theBookingWasCreatedSuccessfully() {
        int status = context.response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected 200 or 201 but got: " + status
                        + ". Body: " + context.response.getBody().asString());
    }

    @And("the response should have a booking ID")
    public void theResponseShouldHaveABookingId() {
        assertNotNull(context.createdBooking, "No booking response was parsed");
        assertTrue(context.createdBooking.bookingid > 0,
                "Booking ID should be a positive number, got: " + context.createdBooking.bookingid);
    }

    @And("the booking firstname should be {string}")
    public void theBookingFirstnameShouldBe(String expected) {
        assertNotNull(context.createdBooking, "Booking was not created");
        assertNotNull(context.createdBooking.booking, "Booking object is null in response");
        assertEquals(expected, context.createdBooking.booking.firstname, "firstname does not match");
    }

    @And("the booking lastname should be {string}")
    public void theBookingLastnameShouldBe(String expected) {
        assertNotNull(context.createdBooking, "Booking was not created");
        assertNotNull(context.createdBooking.booking, "Booking object is null in response");
        assertEquals(expected, context.createdBooking.booking.lastname, "lastname does not match");
    }

    // ---- get booking by ID ----

    @When("I get the booking I just created")
    public void iGetTheBookingIJustCreated() {
        assertTrue(context.bookingId > 0,
                "Cannot GET booking - bookingId was not set (create step may have failed)");
        context.response = bookingApi.getBookingById(context.bookingId, context.authToken);
    }

    @And("the response firstname should be {string}")
    public void theResponseFirstnameShouldBe(String expected) {
        String actual = context.response.jsonPath().getString("firstname");
        assertEquals(expected, actual, "firstname does not match");
    }

    @And("the response should have valid checkin and checkout dates")
    public void theResponseShouldHaveValidCheckinAndCheckoutDates() {
        String checkin  = context.response.jsonPath().getString("bookingdates.checkin");
        String checkout = context.response.jsonPath().getString("bookingdates.checkout");
        assertTrue(checkin.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkin date format wrong: " + checkin);
        assertTrue(checkout.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkout date format wrong: " + checkout);
    }

    // ---- get without token ----

    @When("I try to get booking {int} without a token")
    public void iTryToGetBookingWithoutAToken(int id) {
        context.response = bookingApi.getBookingById(id, null);
    }

    // ---- update booking with PUT ----

    @When("I update the booking lastname to {string}")
    public void iUpdateTheBookingLastnameTo(String newLastname) {
        assertTrue(context.bookingId > 0,
                "Cannot UPDATE - bookingId not set");
        assertNotNull(lastSentBooking,
                "Cannot UPDATE - lastSentBooking is null (create step did not run)");
        assertNotNull(lastSentBooking.bookingdates,
                "Cannot UPDATE - lastSentBooking.bookingdates is null");

        // Re-login for a fresh token
        String freshToken = authApi.getAdminToken();
        assertNotNull(freshToken, "Re-login failed before PUT");
        context.authToken = freshToken;

        // Use different dates than the original booking to avoid a 409 conflict —
        // the API rejects a PUT if the new dates overlap an *existing* booking for that room,
        // hence shifting further by one month so that each scenario uses a unique month

        String originalCheckin  = lastSentBooking.bookingdates.checkin;
        String originalCheckout = lastSentBooking.bookingdates.checkout;
        // e.g. "2040-03-10" -> "2040-03-20", "2040-03-14" -> "2040-03-24"
        String updatedCheckin  = originalCheckin.substring(0, 8)
                + String.format("%02d", Integer.parseInt(originalCheckin.substring(8))  + 10);
        String updatedCheckout = originalCheckout.substring(0, 8)
                + String.format("%02d", Integer.parseInt(originalCheckout.substring(8)) + 10);

        Booking updatedBooking = new Booking(
                lastSentBooking.roomid,
                lastSentBooking.firstname,
                newLastname,
                lastSentBooking.depositpaid,
                new Booking.BookingDates(updatedCheckin, updatedCheckout),
                lastSentBooking.email,
                lastSentBooking.phone
        );

        context.response = bookingApi.updateBooking(
                context.bookingId, updatedBooking, context.authToken);


    }

    @And("the updated lastname should be {string}")
    public void theUpdatedLastnameShouldBe(String expected) {
        String actual = context.response.jsonPath().getString("booking.lastname");
        assertNotNull(actual, "lastname was null in PUT response. Body: "
                + context.response.getBody().asString());
        assertEquals(expected, actual, "lastname not updated correctly");
    }

    // ---- delete booking ----

    @When("I delete the booking I just created")
    public void iDeleteTheBookingIJustCreated() {
        context.response = bookingApi.deleteBooking(context.bookingId, context.authToken);
    }

    @And("the booking should no longer exist")
    public void theBookingShouldNoLongerExist() {
        int status = bookingApi.getBookingById(
                context.bookingId, context.authToken).getStatusCode();
        assertEquals(404, status, "Booking still exists after delete");
    }

    // ---- validation ----

    @When("I try to create a booking with a firstname that is too short")
    public void iTryToCreateABookingWithAFirstnameThatIsTooShort() {
        Booking.BookingDates dates = new Booking.BookingDates("2040-09-01", "2040-09-05");
        // null roomid — the API rejects missing required fields with 400
        Booking booking = new Booking(null, "Jo", "Smith", true, dates,
                "test@test.com", "12345678901");
        context.response = bookingApi.createBooking(booking);
    }

    @And("the response should have a validation error message")
    public void theResponseShouldHaveAValidationErrorMessage() {
        assertFalse(context.response.jsonPath().getList("errors").isEmpty(),
                "Expected validation errors but got none. Body: "
                        + context.response.getBody().asString());
    }

}
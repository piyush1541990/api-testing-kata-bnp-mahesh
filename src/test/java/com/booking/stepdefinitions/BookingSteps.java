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
        assertTrue(checkin.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkin date format wrong: " + checkin);
        assertTrue(checkout.matches("\\d{4}-\\d{2}-\\d{2}"),
                "checkout date format wrong: " + checkout);
    }

    // ---- get without token ----

    @When("I try to get booking {int} without a token")
    public void iTryToGetBookingWithoutAToken(int id) {
        context.response = bookingApi.getBookingById(id, "");
    }

}

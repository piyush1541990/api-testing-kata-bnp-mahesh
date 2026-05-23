package com.booking.stepdefinitions;

import com.booking.models.BookingResponse;
import io.restassured.response.Response;

// To share data between different steps in the same scenario
// for example I created a booking in one step and need the booking ID in the next step
// Cucumber would thus create a new instance of this for each scenario
public class TestContext {

    public Response response;
    public String authToken;
    public int bookingId;
    public BookingResponse createdBooking;

}

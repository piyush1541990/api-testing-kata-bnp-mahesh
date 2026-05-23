package com.booking.api;

import com.booking.models.Booking;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

// all the API calls for the /booking endpoint
// GET /booking/{id} requires a token cookie (from the spec)

public class BookingApi {

    private static final String BASE_URL = "https://automationintesting.online/api";

    // GET /booking/{id} - token required as a cookie
    public Response getBookingById(int id, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Accept", "application/json")
                .cookie("token", token)
                .when()
                .get("/booking/" + id);
    }

    // POST /booking - create a new booking with no auth is needed
    public Response createBooking(Booking booking) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(booking)
                .when()
                .post("/booking");
    }


    // health check - verify the API is up before running tests
    public Response healthCheck() {
        return given()
                .baseUri(BASE_URL)
                .when()
                .get("/booking/actuator/health");
    }


    // PUT /booking/{id} - full update, token required
    public Response updateBooking(int id, Booking booking, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .cookie("token", token)
                .body(booking)
                .when()
                .put("/booking/" + id);
    }

    // PATCH /booking/{id} - partial update, token required
    public Response patchBooking(int id, String body, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .cookie("token", token)
                .body(body)
                .when()
                .patch("/booking/" + id);
    }

    // DELETE /booking/{id} - token required, returns 201 on success
    public Response deleteBooking(int id, String token) {
        return given()
                .baseUri(BASE_URL)
                .cookie("token", token)
                .when()
                .delete("/booking/" + id);
    }

}

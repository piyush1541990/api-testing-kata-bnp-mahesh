package com.booking.api;

import com.booking.models.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

// all the API calls for the /booking endpoint
// base URL confirmed from starter project: https://automationintesting.online/api
public class BookingApi {

    private static final String BASE_URL = "https://automationintesting.online";
    private static final String BOOKING_PATH = "/api/booking";

    // GET /api/booking/{id} - token required as a cookie
    // If token is null or empty the cookie is omitted entirely so the API returns 403
    public Response getBookingById(int id, String token) {
        RequestSpecification req = given()
                .baseUri(BASE_URL)
                .header("Accept", "application/json");
        if (token != null && !token.isEmpty()) {
            req = req.cookie("token", token);
        }
        return req.when().get(BOOKING_PATH + "/" + id);
    }

    // POST /api/booking - create a new booking
    public Response createBooking(Booking booking) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(booking)
                .when()
                .post(BOOKING_PATH);
    }

    // PUT /api/booking/{id} - full update, token required
    public Response updateBooking(int id, Booking booking, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .cookie("token", token)
                .body(booking)
                .when()
                .put(BOOKING_PATH + "/" + id);
    }

    // PATCH /api/booking/{id} - partial update, token required
    public Response patchBooking(int id, String body, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .cookie("token", token)
                .body(body)
                .when()
                .patch(BOOKING_PATH + "/" + id);
    }

    // DELETE /api/booking/{id} - token required
    public Response deleteBooking(int id, String token) {
        return given()
                .baseUri(BASE_URL)
                .cookie("token", token)
                .when()
                .delete(BOOKING_PATH + "/" + id);
    }

    // health check to verify the API is running
    public Response healthCheck() {
        return given()
                .baseUri(BASE_URL)
                .when()
                .get(BOOKING_PATH + "/actuator/health");
    }

}

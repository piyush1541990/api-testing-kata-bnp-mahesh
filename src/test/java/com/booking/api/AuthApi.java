package com.booking.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

// handles login - we need a token for GET, PUT, DELETE booking requests
public class AuthApi {

    private static final String BASE_URL = "https://automationintesting.online";

    public Response login(String username, String password) {
        String body = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/auth/login");
    }

    // logs in as admin and returns just the token string
    public String getAdminToken() {
        return login("admin", "password")
                .jsonPath()
                .getString("token");
    }

}

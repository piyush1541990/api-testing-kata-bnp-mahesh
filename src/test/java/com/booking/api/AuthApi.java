package com.booking.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

// handles login and getting tokens from API

public class AuthApi {

    private static final String BASE_URL = "https://automationintesting.online/api";

    public Response login(String username, String password) {
        String body = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/auth/login");
    }

    // logs in as admin to get token string
    public String getAdminToken() {
        return login("admin", "password")
                .jsonPath()
                .getString("token");
    }

}



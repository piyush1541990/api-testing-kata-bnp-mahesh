package com.booking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Booking object based on the booking.yaml spec
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    @JsonProperty("roomid")
    public Integer roomid;

    @JsonProperty("firstname")
    public String firstname;

    @JsonProperty("lastname")
    public String lastname;

    @JsonProperty("depositpaid")
    public Boolean depositpaid;

    @JsonProperty("bookingdates")
    public BookingDates bookingdates;

    @JsonProperty("email")
    public String email;

    @JsonProperty("phone")
    public String phone;

    // empty constructor needed by Jackson
    public Booking() {}

    public Booking(Integer roomid, String firstname, String lastname, Boolean depositpaid,
                   BookingDates bookingdates, String email, String phone) {
        this.roomid = roomid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.depositpaid = depositpaid;
        this.bookingdates = bookingdates;
        this.email = email;
        this.phone = phone;
    }

    // checkin and checkout dates
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookingDates {

        @JsonProperty("checkin")
        public String checkin;

        @JsonProperty("checkout")
        public String checkout;

        public BookingDates() {}

        public BookingDates(String checkin, String checkout) {
            this.checkin = checkin;
            this.checkout = checkout;
        }
    }
}

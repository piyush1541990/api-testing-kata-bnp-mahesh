package com.booking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Booking objects taken from booking.yaml spec
// fields: roomid, firstname, lastname, depositpaid, bookingdates, email, phone
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    public Integer roomid;
    public String firstname;
    public String lastname;
    public Boolean depositpaid;
    public BookingDates bookingdates;
    public String email;
    public String phone;

    // empty constructor
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
        public String checkin;
        public String checkout;

        public BookingDates() {}

        public BookingDates(String checkin, String checkout) {
            this.checkin = checkin;
            this.checkout = checkout;
        }
    }
}

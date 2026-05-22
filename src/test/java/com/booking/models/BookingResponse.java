package com.booking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// getting the new booking ID in addition to the booking details from the "booking" field
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {

    public int bookingid;
    public Booking booking;

    public Booking booking;

}

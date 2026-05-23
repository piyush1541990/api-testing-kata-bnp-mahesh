package com.booking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// when we create a booking the API returns the new booking ID
// plus the full booking details inside a "booking" field
// example: {"bookingid":7,"booking":{"firstname":"Mahesh","lastname":"Takle",...}}
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {

    @JsonProperty("bookingid")
    public int bookingid;

    @JsonProperty("booking")
    public Booking booking;

}

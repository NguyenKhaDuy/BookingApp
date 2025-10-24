package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {
    private Integer stars;
    private String comment;
    private String customer_id;
    private String technician_id;
}

package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchByLocationRequest {
    private String district;
    private String ward;
    private String conscious;
}

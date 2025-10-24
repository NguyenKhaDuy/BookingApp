package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteRequest {
    private List<Long> id;
}

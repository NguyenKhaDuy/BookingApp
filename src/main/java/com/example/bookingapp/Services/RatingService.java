package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RatingDTO;
import com.example.bookingapp.Models.Request.RatingRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RatingService<T> {
    T getAllRatingsByTechnician(Integer pageNo, String id_technician);
    Object createRatingTechnician(RatingRequest ratingRequest);
    Object deleteRating(Long id_rating);
}

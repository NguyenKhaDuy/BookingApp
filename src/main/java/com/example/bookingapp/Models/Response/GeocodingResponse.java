package com.example.bookingapp.Models.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeocodingResponse {

    private List<Feature> features;

    @Getter
    @Setter
    public static class Feature {

        private Geometry geometry;

        private Properties properties;
    }

    @Getter
    @Setter
    public static class Geometry {

        private String type;

        private List<Double> coordinates;
    }

    @Getter
    @Setter
    public static class Properties {

        private String name;

        private String street;

        private String housenumber;

        private String city;

        private String district;

        private String state;

        private String country;

        private String postcode;
    }
}
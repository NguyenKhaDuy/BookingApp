package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Models.Response.GeocodingResponse;
import com.example.bookingapp.Services.GeocodingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    @Value("${geocoding.url}")
    private String geocodingUrl;

    private final RestTemplate restTemplate;

    public GeocodingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public GeocodingResponse getCoordinates(String address) {

        String url = UriComponentsBuilder
                .fromUriString(geocodingUrl)
                .queryParam("q", address)
                .queryParam("limit", 1)
                .build()
                .toUriString();

        ResponseEntity<GeocodingResponse> response =
                restTemplate.getForEntity(
                        url,
                        GeocodingResponse.class
                );

        System.out.println("HTTP STATUS: " + response.getStatusCode());

        GeocodingResponse body = response.getBody();

        if (body == null ||
                body.getFeatures() == null ||
                body.getFeatures().isEmpty()) {

            throw new RuntimeException(
                    "Photon không tìm thấy tọa độ cho địa chỉ: "
                            + address
            );
        }

        GeocodingResponse.Feature feature =
                body.getFeatures().get(0);

        Double longitude =
                feature.getGeometry()
                        .getCoordinates()
                        .get(0);

        Double latitude =
                feature.getGeometry()
                        .getCoordinates()
                        .get(1);

        System.out.println("LATITUDE: " + latitude);
        System.out.println("LONGITUDE: " + longitude);

        if (feature.getProperties() != null) {
            System.out.println(
                    "NAME: "
                            + feature.getProperties().getName()
            );

            System.out.println(
                    "CITY: "
                            + feature.getProperties().getCity()
            );

            System.out.println(
                    "STATE: "
                            + feature.getProperties().getState()
            );

            System.out.println(
                    "COUNTRY: "
                            + feature.getProperties().getCountry()
            );
        }


        return body;
    }
}
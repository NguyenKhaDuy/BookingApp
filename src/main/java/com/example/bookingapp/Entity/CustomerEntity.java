package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class CustomerEntity extends UserEntity{
    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<RepairRequestEntity> repairRequestEntities = new ArrayList<>();

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<InvoicesEntity> invoicesEntities = new ArrayList<>();

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<FeedbackEntity> feedbackEntities = new ArrayList<>();

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<RatingEntity> ratingEntities = new ArrayList<>();
}

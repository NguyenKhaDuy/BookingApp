package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "status")
public class StatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_status;

    @Column(name = "name_status")
    private String nameStatus;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "statusEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<OtpVerificationEntity> otpVerificationEntities = new ArrayList<>();

    @OneToMany(mappedBy = "statusEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval = true)
    private List<NotificationUserEntity> notificationUserEntities = new ArrayList<>();

    @OneToMany(mappedBy = "statusEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval = true)
    private List<TechnicianScheduleEntity> technicianScheduleEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "statusEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<RepairRequestEntity> repairRequestEntities = new ArrayList<>();

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}

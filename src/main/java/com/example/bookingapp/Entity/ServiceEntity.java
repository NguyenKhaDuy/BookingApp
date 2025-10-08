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
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_service;

    @Column(name = "name_service")
    private String name_service;

    @OneToMany(mappedBy = "serviceEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    private List<RepairRequestEntity> repairRequestEntities = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "service_technician", joinColumns = @JoinColumn(name = "service_id"), inverseJoinColumns = @JoinColumn(name = "technician_id"))
    private List<TechnicianEntity> technicianEntities = new ArrayList<>();

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}

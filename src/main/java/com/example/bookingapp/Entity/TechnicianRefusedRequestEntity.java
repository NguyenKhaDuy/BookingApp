package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "technician_refused_request")
public class TechnicianRefusedRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private TechnicianEntity technicianEntity;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private RepairRequestEntity repairRequestEntity;
}

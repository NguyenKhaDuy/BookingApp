package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "technician")
public class TechnicianEntity extends UserEntity{
    @Column(name = "working_area")
    private String working_area;

    @Column(name = "experience_year")
    private Integer experience_year;

    @ManyToOne
    @JoinColumn(name = "level_id")
    private LevelEntity levelEntity;

    @OneToMany(mappedBy = "technicianEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval = true)
    private List<TechnicianScheduleEntity> technicianScheduleEntityList = new ArrayList<>();
}

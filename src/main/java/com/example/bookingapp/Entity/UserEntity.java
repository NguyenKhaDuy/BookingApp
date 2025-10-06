package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class UserEntity {
    @Id
    @Column(name = "id_user")
    private String id_user;

    @Column(name = "phone_number")
    private String phone_number;

    
}

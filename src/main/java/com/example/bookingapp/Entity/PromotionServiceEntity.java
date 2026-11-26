package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promotion_service")
public class PromotionServiceEntity {
    @Id
    @Column(name = "id_promotion_service")
    private String idPromotionService;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private PromotionsEntity promotionsEntity;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity serviceEntity;
}

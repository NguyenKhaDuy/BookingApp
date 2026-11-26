package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository
        extends JpaRepository<WalletEntity, String> {

    WalletEntity findByUserEntity(
            UserEntity userEntity
    );

//    List<WalletEntity> findByTechnicianEntity_IdUser(
//            String idUser
//    );
//
//    List<WalletEntity> findByCustomerEntity_IdUser(
//            String idUser
//    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM WalletEntity w
            WHERE w.idWallet = :walletId
            """)
    Optional<WalletEntity> lockWallet(
            @Param("walletId") String walletId
    );
}
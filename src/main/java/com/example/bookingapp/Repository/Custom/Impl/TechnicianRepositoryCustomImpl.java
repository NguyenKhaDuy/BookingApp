package com.example.bookingapp.Repository.Custom.Impl;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.Request.SearchByLocationRequest;
import com.example.bookingapp.Repository.Custom.TechnicianRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TechnicianRepositoryCustomImpl implements TechnicianRepositoryCustom {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<TechnicianEntity> findByLocation(SearchByLocationRequest searchByLocationRequest, Pageable pageable) {
        StringBuilder sql = new StringBuilder(
                "SELECT technician.*, user.* FROM technician" +
                        " JOIN user ON technician.id_user = user.id_user" +
                        " JOIN technician_location ON technician.id_user = technician_location.user_id" +
                        " JOIN location ON technician_location.location_id = location.id_location" +
                        " WHERE 1 = 1 ");
        Query query = entityManager.createNativeQuery(AppendLocation(searchByLocationRequest, sql), TechnicianEntity.class);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult((int) pageable.getOffset());
        return new PageImpl<>(query.getResultList(), pageable, CountTechnician(searchByLocationRequest));
    }


    //Hàm đếm tổng số thợ
    public Long CountTechnician(SearchByLocationRequest searchByLocationRequest) {
        StringBuilder sql = new StringBuilder(
                "SELECT count(*) FROM technician" +
                        " JOIN user ON technician.id_user = user.id_user" +
                        " JOIN technician_location ON technician.id_user = technician_location.user_id" +
                        " JOIN location ON technician_location.location_id = location.id_location" +
                        " WHERE 1 = 1 ");
        Query query = entityManager.createNativeQuery(AppendLocation(searchByLocationRequest, sql));
        Number total = (Number) query.getSingleResult();
        return total.longValue();
    }


    //hàm thêm điều kiện location vào
    public String AppendLocation(SearchByLocationRequest searchByLocationRequest, StringBuilder sql) {
        if (searchByLocationRequest.getWard() != null) {
            sql.append("AND location.ward = '" + searchByLocationRequest.getWard() + "'");
        }
        if (searchByLocationRequest.getDistrict() != null) {
            sql.append(" AND location.district = '" + searchByLocationRequest.getDistrict() + "'");
        }
        if (searchByLocationRequest.getConscious() != null) {
            sql.append(" AND location.conscious = '" + searchByLocationRequest.getConscious() + "'");
        }
        return sql.toString();
    }
}

package com.example.bookingapp.Repository.Custom.Impl;

import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Models.Request.FillterRequest;
import com.example.bookingapp.Models.Request.SearchRequest;
import com.example.bookingapp.Repository.Custom.RequestRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class RequestRepositoryCustomImpl implements RequestRepositoryCustom {
    @Autowired
    EntityManager entityManager;
    @Override
    public Page<RepairRequestEntity> searchRequest(SearchRequest searchRequest, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "repair_request.scheduled_date, " +
                "repair_request.scheduled_time, " +
                "repair_request.created_at, " +
                "repair_request.id_request, " +
                "repair_request.service_id, " +
                "repair_request.status_id, " +
                "repair_request.updated_at, " +
                "repair_request.customer_id, " +
                "repair_request.description, " +
                "repair_request.location, " +
                "repair_request.technician_id, " +
                "user.phone_number " +
                "FROM " +
                "repair_request " +
                "JOIN customer ON repair_request.customer_id = customer.id_user " +
                "JOIN user ON user.id_user = customer.id_user " +
                "WHERE 1 = 1 ");

        if (searchRequest.getId_request() != null && !searchRequest.getId_request().isEmpty()) {
            String id_request = searchRequest.getId_request();
            sql.append(" AND repair_request.id_request LIKE '%" + id_request + "%'");
        }

        if (searchRequest.getPhoneNumber() != null && !searchRequest.getPhoneNumber().isEmpty()) {
            String phone = searchRequest.getPhoneNumber();
            sql.append(" AND user.phone_number LIKE '%" + phone + "%'");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), RepairRequestEntity.class);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult((int) pageable.getOffset());
        return new PageImpl<>(query.getResultList(), pageable, CountRequest(searchRequest));
    }

    @Override
    public Page<RepairRequestEntity> fillterRequest(FillterRequest fillterRequest, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "repair_request.scheduled_date, " +
                "repair_request.scheduled_time, " +
                "repair_request.created_at, " +
                "repair_request.id_request, " +
                "repair_request.service_id, " +
                "repair_request.status_id, " +
                "repair_request.updated_at, " +
                "repair_request.customer_id, " +
                "repair_request.description, " +
                "repair_request.location, " +
                "repair_request.technician_id " +
                "FROM " +
                "repair_request " +
                "JOIN service ON repair_request.service_id = service.id_service " +
                "JOIN technician ON repair_request.technician_id = technician.id_user " +
                "JOIN user ON user.id_user = technician.id_user " +
                "WHERE 1 = 1 ");

        if (fillterRequest.getName_service() != null && !fillterRequest.getName_service().isEmpty()) {
            String name_service = fillterRequest.getName_service();
            sql.append(" AND service.name_service LIKE '%" + name_service + "%'");
        }

        if (fillterRequest.getName_technician() != null && !fillterRequest.getName_technician().isEmpty()) {
            String name_technician = fillterRequest.getName_technician();
            sql.append(" AND user.fullname LIKE '%" + name_technician + "%'");
        }

        if (fillterRequest.getDate_from() != null &&  fillterRequest.getDate_to() != null && !fillterRequest.getDate_from().equals("") && !fillterRequest.getDate_to().equals("")) {
            LocalDate dateFrom = fillterRequest.getDate_from();
            LocalDate dateTo = fillterRequest.getDate_to();
            sql.append(" AND DATE(repair_request.created_at) >= '" + dateFrom + "' AND DATE(repair_request.created_at) <= '" + dateTo + "'");
        }
        Query query = entityManager.createNativeQuery(sql.toString(), RepairRequestEntity.class);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult((int) pageable.getOffset());
        return new PageImpl<>(query.getResultList(), pageable, CountFillterRequest(fillterRequest));
    }

    public Long CountFillterRequest(FillterRequest fillterRequest) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "count(*) " +
                "FROM " +
                "repair_request " +
                "JOIN service ON repair_request.service_id = service.id_service " +
                "JOIN technician ON repair_request.technician_id = technician.id_user " +
                "JOIN user ON user.id_user = technician.id_user " +
                "WHERE 1 = 1 ");

        if (fillterRequest.getName_service() != null && !fillterRequest.getName_service().isEmpty()) {
            String name_service = fillterRequest.getName_service();
            sql.append(" AND service.name_service LIKE '%" + name_service + "%'");
        }

        if (fillterRequest.getName_technician() != null && !fillterRequest.getName_technician().isEmpty()) {
            String name_technician = fillterRequest.getName_technician();
            sql.append(" AND user.fullname LIKE '%" + name_technician + "%'");
        }

        if (fillterRequest.getDate_from() != null &&  fillterRequest.getDate_to() != null && !fillterRequest.getDate_from().equals("") && !fillterRequest.getDate_to().equals("")) {
            LocalDate dateFrom = fillterRequest.getDate_from();
            LocalDate dateTo = fillterRequest.getDate_to();
            sql.append(" AND DATE(repair_request.created_at) >= '" + dateFrom + "' AND DATE(repair_request.created_at) <= '" + dateTo + "'");
        }
        Query query = entityManager.createNativeQuery(sql.toString());
        Number total = (Number) query.getSingleResult();
        return total.longValue();
    }

    public Long CountRequest(SearchRequest searchRequest) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "count(*) " +
                "FROM " +
                "repair_request " +
                "JOIN customer ON repair_request.customer_id = customer.id_user " +
                "JOIN user ON user.id_user = customer.id_user " +
                "WHERE 1 = 1 ");

        if (searchRequest.getId_request() != null && !searchRequest.getId_request().isEmpty()) {
            String id_request = searchRequest.getId_request();
            sql.append(" AND repair_request.id_request LIKE '%" + id_request + "%'");
        }

        if (searchRequest.getPhoneNumber() != null && !searchRequest.getPhoneNumber().isEmpty()) {
            String phone = searchRequest.getPhoneNumber();
            sql.append(" AND user.phone_number LIKE '%" + phone + "%'");
        }
        Query query = entityManager.createNativeQuery(sql.toString());
        Number total = (Number) query.getSingleResult();
        return total.longValue();
    }
}

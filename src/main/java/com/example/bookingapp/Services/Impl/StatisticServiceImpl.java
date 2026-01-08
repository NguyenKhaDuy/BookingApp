package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.DetailInvoicesEntity;
import com.example.bookingapp.Entity.InvoicesEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Models.DTO.RevenueByServiceDTO;
import com.example.bookingapp.Models.DTO.RevenueDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderByServiceDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderDTO;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import com.example.bookingapp.Repository.InvoicesRepository;
import com.example.bookingapp.Repository.RepairRequestRepository;
import com.example.bookingapp.Repository.ServiceRepository;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {
    @Autowired
    InvoicesRepository invoicesRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Override
    public StatisticsResponse RevenueStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfDay(dateFrom, dateTo, id_technician);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += detailInvoicesEntity.getTotal_price() - ((detailInvoicesEntity.getTotal_price() * 20) / 100);
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse RevenueStatisticsOfMonth(Long curentMonth, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfMonth(curentMonth, id_technician);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += detailInvoicesEntity.getTotal_price() - ((detailInvoicesEntity.getTotal_price() * 20) / 100);
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse RevenueStatisticsOfYear(Long currentYear, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfYear(currentYear, id_technician);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += detailInvoicesEntity.getTotal_price() - ((detailInvoicesEntity.getTotal_price() * 20) / 100);
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, Long id_status, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfDay(dateFrom, dateTo, id_technician ,id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfMonth(Long curentMonth, Long id_status, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfMonth(curentMonth, id_technician, id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfYear(Long curentYear, Long id_status, String id_technician) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfYear(curentYear, id_technician, id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public List<RevenueDTO> monthlyRevenue(Long year, String id_technician) {
        List<RevenueDTO> revenueDTOS = new ArrayList<>();
        for (Long i = 1L; i <= 12; i++){
            List<InvoicesEntity> invoicesEntities = invoicesRepository.monthlyRevenueThroughoutYearOfTechnician(i, year, id_technician);
            float result = 0;
            RevenueDTO revenueDTO = new RevenueDTO();
            revenueDTO.setMonth(i);
            for (InvoicesEntity invoicesEntity : invoicesEntities){
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                    if (detailInvoicesEntity.getName().equals("Công thợ")){
                        result += detailInvoicesEntity.getTotal_price() - ((detailInvoicesEntity.getTotal_price() * 20) / 100);
                    }
                }
            }
            revenueDTO.setTotalRevenue(result);
            revenueDTOS.add(revenueDTO);
        }
        return revenueDTOS;
    }

    @Override
    public List<StatisticOrderDTO> monthlyStatisticOrder(Long year, String id_technician, Long id_status) {
        List<StatisticOrderDTO> statisticOrderDTOS = new ArrayList<>();
        for (Long i = 1L; i <= 12; i++){
            List<RepairRequestEntity> requestEntities = repairRequestRepository.monthlyOrderStatistics(i, year, id_technician, id_status);
            StatisticOrderDTO statisticOrderDTO = new StatisticOrderDTO();
            statisticOrderDTO.setMonth(i);
            statisticOrderDTO.setTotalRequest(requestEntities.size());
            statisticOrderDTOS.add(statisticOrderDTO);
        }
        return statisticOrderDTOS;
    }

    //admin
    @Override
    public StatisticsResponse RevenueOfDayAdmin(LocalDate dateFrom, LocalDate dateTo) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfDay(dateFrom, dateTo);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += (detailInvoicesEntity.getTotal_price() * 20) / 100;
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse RevenueOfMonthAdmin(Long curentMonth) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfMonth(curentMonth);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += (detailInvoicesEntity.getTotal_price() * 20) / 100;
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse RevenueOfYearAdmin(Long currentYear) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<InvoicesEntity> invoicesEntities = invoicesRepository.findByInvoiceOfYear(currentYear);
        for (InvoicesEntity invoicesEntity : invoicesEntities){
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                if (detailInvoicesEntity.getName().equals("Công thợ")){
                    result += (detailInvoicesEntity.getTotal_price() * 20) / 100;
                }
            }
        }
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public List<RevenueDTO> monthlyRevenue(Long year) {
        List<RevenueDTO> revenueDTOS = new ArrayList<>();
        for (Long i = 1L; i <= 12; i++){
            List<InvoicesEntity> invoicesEntities = invoicesRepository.monthlyRevenueThroughoutYear(i, year);
            float result = 0;
            RevenueDTO revenueDTO = new RevenueDTO();
            revenueDTO.setMonth(i);
            for (InvoicesEntity invoicesEntity : invoicesEntities){
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                    if (detailInvoicesEntity.getName().equals("Công thợ")){
                       result += (detailInvoicesEntity.getTotal_price() * 20) / 100;
                    }
                }
            }
            revenueDTO.setTotalRevenue(result);
            revenueDTOS.add(revenueDTO);
        }
        return revenueDTOS;
    }


    @Override
    public List<RevenueByServiceDTO> monthlyRevenueOfService(Long year) {
        List<RevenueByServiceDTO> revenueByServiceDTOS = new ArrayList<>();
        List<ServiceEntity> serviceEntities = serviceRepository.findAll();
        for (ServiceEntity serviceEntity : serviceEntities){
            List<InvoicesEntity> invoicesEntities = invoicesRepository.monthlyRevenueThroughoutYearOfService(year, serviceEntity.getId_service());
            float result = 0;
            RevenueByServiceDTO revenueByServiceDTO = new RevenueByServiceDTO();
            revenueByServiceDTO.setServiceName(serviceEntity.getName_service());
            for (InvoicesEntity invoicesEntity : invoicesEntities){
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                    if (detailInvoicesEntity.getName().equals("Công thợ")){
                        result += (detailInvoicesEntity.getTotal_price() * 20) / 100;
                    }
                }
            }
            revenueByServiceDTO.setTotalRevenue(result);
            revenueByServiceDTOS.add(revenueByServiceDTO);
        }
        return revenueByServiceDTOS;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfDayAdmin(LocalDate dateFrom, LocalDate dateTo, Long id_status) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfDay(dateFrom, dateTo ,id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfMonthAdmin(Long curentMonth, Long id_status) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfMonth(curentMonth, id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public StatisticsResponse OrderStatisticsOfYearAdmin(Long curentYear, Long id_status) {
        float result = 0;
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        List<RepairRequestEntity> requestEntities = repairRequestRepository.StatisticsOrderOfYear(curentYear, id_status);
        result = requestEntities.size();
        statisticsResponse.setData(result);
        statisticsResponse.setMessage("Success");
        statisticsResponse.setHttpStatus(HttpStatus.OK);
        return statisticsResponse;
    }

    @Override
    public List<StatisticOrderDTO> monthlyStatisticOrder(Long year, Long id_status) {
        List<StatisticOrderDTO> statisticOrderDTOS = new ArrayList<>();
        for (Long i = 1L; i <= 12; i++){
            List<RepairRequestEntity> requestEntities = repairRequestRepository.monthlyOrderStatistics(i, year, id_status);
            StatisticOrderDTO statisticOrderDTO = new StatisticOrderDTO();
            statisticOrderDTO.setMonth(i);
            statisticOrderDTO.setTotalRequest(requestEntities.size());
            statisticOrderDTOS.add(statisticOrderDTO);
        }
        return statisticOrderDTOS;
    }


    @Override
    public List<StatisticOrderByServiceDTO> monthlyStatisticOrderOfService(Long year, Long id_status) {
        List<ServiceEntity> serviceEntities = serviceRepository.findAll();
        List<StatisticOrderByServiceDTO> statisticOrderByServiceDTOS = new ArrayList<>();
        for (ServiceEntity serviceEntity : serviceEntities){
            List<RepairRequestEntity> requestEntities = repairRequestRepository.monthlyOrderStatistics(serviceEntity.getId_service(), year, id_status);
            StatisticOrderByServiceDTO statisticOrderByServiceDTO = new StatisticOrderByServiceDTO();
            statisticOrderByServiceDTO.setServiceName(serviceEntity.getName_service());
            statisticOrderByServiceDTO.setTotalRequest(requestEntities.size());
            statisticOrderByServiceDTOS.add(statisticOrderByServiceDTO);
        }
        return statisticOrderByServiceDTOS;
    }
}

package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface StatisticService {
    StatisticsResponse RevenueStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, String id_technician);
    StatisticsResponse RevenueStatisticsOfMonth(Long curentMonth, String id_technician);
    StatisticsResponse RevenueStatisticsOfYear(Long currentYear, String id_technician);
    StatisticsResponse OrderStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, String id_status, String id_technician);
    StatisticsResponse OrderStatisticsOfMonth(Long curentMonth, String id_status, String id_technician);
    StatisticsResponse OrderStatisticsOfYear(Long curentYear, String id_status, String id_technician);
    StatisticsResponse RevenueOfDayAdmin(LocalDate dateFrom, LocalDate dateTo);
    StatisticsResponse RevenueOfMonthAdmin(Long curentMonth);
    StatisticsResponse RevenueOfYearAdmin(Long currentYear);
    List<RevenueDTO> monthlyRevenue(Long year);
    List<RevenueDTO> monthlyRevenue(Long year, String id_technician);
    List<RevenueByServiceDTO> monthlyRevenueOfService(Long year);
    StatisticsResponse OrderStatisticsOfDayAdmin(LocalDate dateFrom, LocalDate dateTo, String id_status);
    StatisticsResponse OrderStatisticsOfMonthAdmin(Long curentMonth, String id_status);
    StatisticsResponse OrderStatisticsOfYearAdmin(Long curentYear, String id_status);
    List<StatisticOrderDTO> monthlyStatisticOrder(Long year, String id_status);
    List<StatisticOrderDTO> monthlyStatisticOrder(Long year, String id_technician, String id_status);
    List<StatisticOrderByServiceDTO> monthlyStatisticOrderOfService(Long year, String id_status);
    List<StatisticServiceCustomer> statisticServiceCustomer(Integer month);
}

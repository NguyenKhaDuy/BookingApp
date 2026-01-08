package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RevenueByServiceDTO;
import com.example.bookingapp.Models.DTO.RevenueDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderByServiceDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderDTO;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface StatisticService {
    StatisticsResponse RevenueStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, String id_technician);
    StatisticsResponse RevenueStatisticsOfMonth(Long curentMonth, String id_technician);
    StatisticsResponse RevenueStatisticsOfYear(Long currentYear, String id_technician);
    StatisticsResponse OrderStatisticsOfDay(LocalDate dateFrom, LocalDate dateTo, Long id_status, String id_technician);
    StatisticsResponse OrderStatisticsOfMonth(Long curentMonth, Long id_status, String id_technician);
    StatisticsResponse OrderStatisticsOfYear(Long curentYear, Long id_status, String id_technician);
    StatisticsResponse RevenueOfDayAdmin(LocalDate dateFrom, LocalDate dateTo);
    StatisticsResponse RevenueOfMonthAdmin(Long curentMonth);
    StatisticsResponse RevenueOfYearAdmin(Long currentYear);
    List<RevenueDTO> monthlyRevenue(Long year);
    List<RevenueDTO> monthlyRevenue(Long year, String id_technician);
    List<RevenueByServiceDTO> monthlyRevenueOfService(Long year);
    StatisticsResponse OrderStatisticsOfDayAdmin(LocalDate dateFrom, LocalDate dateTo, Long id_status);
    StatisticsResponse OrderStatisticsOfMonthAdmin(Long curentMonth, Long id_status);
    StatisticsResponse OrderStatisticsOfYearAdmin(Long curentYear, Long id_status);
    List<StatisticOrderDTO> monthlyStatisticOrder(Long year, Long id_status);
    List<StatisticOrderDTO> monthlyStatisticOrder(Long year, String id_technician, Long id_status);
    List<StatisticOrderByServiceDTO> monthlyStatisticOrderOfService(Long year, Long id_status);
}

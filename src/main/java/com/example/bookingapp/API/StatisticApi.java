package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.RevenueByServiceDTO;
import com.example.bookingapp.Models.DTO.RevenueDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderByServiceDTO;
import com.example.bookingapp.Models.DTO.StatisticOrderDTO;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import com.example.bookingapp.Services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class StatisticApi {
    @Autowired
    StatisticService statisticService;

    //technician
    @GetMapping(value = "/api/technician/statistic/revenueOfDay/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfDay(@RequestParam LocalDate dateFrom,
                                                                     @RequestParam LocalDate dateTo,
                                                                     @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.RevenueStatisticsOfDay(dateFrom, dateTo, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/revenueOfMonth/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfMonth(@RequestParam Long currentMonth,
                                                                       @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.RevenueStatisticsOfMonth(currentMonth, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/revenueOfYear/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfYear(@RequestParam Long currentYear,
                                                                      @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.RevenueStatisticsOfYear(currentYear, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/orderOfDay/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfDay(@RequestParam LocalDate dateFrom,
                                                                   @RequestParam LocalDate dateTo,
                                                                   @RequestParam Long id_status,
                                                                   @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfDay(dateFrom, dateTo, id_status, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/orderOfMonth/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfMonth(@RequestParam Long currentMonth,
                                                                     @RequestParam Long id_status,
                                                                     @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfMonth(currentMonth, id_status, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/orderOfYear/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfYear(@RequestParam Long currentYear,
                                                                    @RequestParam Long id_status,
                                                                    @RequestParam String id_technician){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfYear(currentYear, id_status, id_technician);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/monthlyRevenue/")
    public ResponseEntity<Object> monthlyRevenueOfTechnician(@RequestParam Long year,
                                                             @RequestParam String id_technician){
        List<RevenueDTO> revenueDTOS = statisticService.monthlyRevenue(year, id_technician);
        return ResponseEntity.ok(revenueDTOS);
    }

    @GetMapping(value = "/api/technician/statistic/monthlyOrder/")
    public ResponseEntity<Object> monthlyStatisticOrderOfTechnician(@RequestParam Long year,
                                                                    @RequestParam String id_technician,
                                                                    @RequestParam Long id_status){
        List<StatisticOrderDTO> statisticOrderDTOS = statisticService.monthlyStatisticOrder(year, id_technician, id_status);
        return ResponseEntity.ok(statisticOrderDTOS);
    }


    //admin
    @GetMapping(value = "/api/admin/statistic/revenueOfDay/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfDayAdmin(@RequestParam LocalDate dateFrom,
                                                                     @RequestParam LocalDate dateTo){
        StatisticsResponse statisticsResponse = statisticService.RevenueOfDayAdmin(dateFrom, dateTo);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/revenueOfMonth/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfMonthAdmin(@RequestParam Long currentMonth){
        StatisticsResponse statisticsResponse = statisticService.RevenueOfMonthAdmin(currentMonth);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/revenueOfYear/")
    public ResponseEntity<StatisticsResponse> RevenueStatisticsOfYearAdmin(@RequestParam Long currentYear){
        StatisticsResponse statisticsResponse = statisticService.RevenueOfYearAdmin(currentYear);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/monthlyRevenue/")
    public ResponseEntity<Object> monthlyRevenueOfAdmin(@RequestParam Long year){
        List<RevenueDTO> revenueDTOS = statisticService.monthlyRevenue(year);
        return ResponseEntity.ok(revenueDTOS);
    }

    @GetMapping(value = "/api/admin/statistic/monthlyRevenueOfService/")
    public ResponseEntity<Object> monthlyRevenueOfService(@RequestParam Long year){
        List<RevenueByServiceDTO> revenueByServiceDTOS = statisticService.monthlyRevenueOfService(year);
        return ResponseEntity.ok(revenueByServiceDTOS);
    }

    @GetMapping(value = "/api/admin/statistic/orderOfDay/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfDayAdmin(@RequestParam LocalDate dateFrom,
                                                                   @RequestParam LocalDate dateTo,
                                                                   @RequestParam Long id_status){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfDayAdmin(dateFrom, dateTo, id_status);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/orderOfMonth/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfMonthAdmin(@RequestParam Long currentMonth,
                                                                     @RequestParam Long id_status){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfMonthAdmin(currentMonth, id_status);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/orderOfYear/")
    public ResponseEntity<StatisticsResponse> OrderStatisticsOfYearAdmin(@RequestParam Long currentYear,
                                                                    @RequestParam Long id_status){
        StatisticsResponse statisticsResponse = statisticService.OrderStatisticsOfYearAdmin(currentYear, id_status);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/statistic/monthlyOrder/")
    public ResponseEntity<Object> monthlyStatisticOrderOfAdmin(@RequestParam Long year,
                                                                    @RequestParam Long id_status){
        List<StatisticOrderDTO> statisticOrderDTOS = statisticService.monthlyStatisticOrder(year, id_status);
        return ResponseEntity.ok(statisticOrderDTOS);
    }

    @GetMapping(value = "/api/admin/statistic/monthlyOrderOfService/")
    public ResponseEntity<Object> monthlyStatisticOrderOfService(@RequestParam Long year,
                                                                 @RequestParam Long id_status){
        List<StatisticOrderByServiceDTO> statisticOrderByServiceDTOS = statisticService.monthlyStatisticOrderOfService(year, id_status);
        return ResponseEntity.ok(statisticOrderByServiceDTOS);
    }

}

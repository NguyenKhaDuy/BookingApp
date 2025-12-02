package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.Request.LoginRequest;
import com.example.bookingapp.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApi {
    @Autowired
    UserService userService;
    @PostMapping(value = "/api/login/")
    public ResponseEntity<Object> Login(@RequestBody LoginRequest loginRequest){
        String result = userService.login(loginRequest);
        if (result == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Login failed");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setData(result);
        dataDTO.setMessage("Login success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}

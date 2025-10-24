package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Services.RepairRequestService;
import com.example.bookingapp.Services.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TechnicianApi {
    @Autowired
    TechnicianService technicianService;
    @Autowired
    RepairRequestService repairRequestService;
    @GetMapping(value = "/api/technician/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchlocation")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestBody SearchByLocationRequest searchByLocationRequest){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByLocation(pageNo, searchByLocationRequest);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/id={id_technician}")
    public ResponseEntity<Object> getById(@PathVariable String id_technician){
        Object result = technicianService.getById(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchName/")
    public ResponseEntity<DataDTO> searchByName(@RequestParam(value = "name_technician") String name_technician,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByName(pageNo, name_technician);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchService/")
    public ResponseEntity<Object> searchByName(@RequestParam(value = "id_service") Long id_service,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByService(pageNo, id_service);
        if (technicicanDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found service");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/")
    public ResponseEntity<Object> updateProfile(@RequestBody TechnicianProfileRequest technicianProfileRequest){
        Object result = technicianService.updateProfile(technicianProfileRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/avatar/")
    public ResponseEntity<Object> updateAvatar(@ModelAttribute AvatarRequest avatarRequest){
        Object result = technicianService.updateAvatar(avatarRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/skill/")
    public ResponseEntity<Object> addSkill(@RequestBody SkillTechnicianRequest skillTechnicianRequest){
        Object result = technicianService.addSkill(skillTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/skill/id={id_user}")
    public ResponseEntity<Object> getSkill(@PathVariable String id_user,
                                               @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<SkillDTO> skillDTOS = technicianService.getSkill(id_user, pageNo);
        if (skillDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(skillDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(skillDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/technician/profile/skill/")
    public ResponseEntity<Object> deleteSkill(@RequestBody SkillTechnicianRequest skillTechnicianRequest){
        Object result = technicianService.deleteSkillOfTechnician(skillTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/location/")
    public ResponseEntity<Object> addLocation(@RequestBody LocationTechnicianRequest locationTechnicianRequest){
        Object result = technicianService.addLocation(locationTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/location/id={id_user}")
    public ResponseEntity<Object> getLocation(@PathVariable String id_user,
                                           @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<LocationDTO> locationDTOS = technicianService.getLocation(id_user, pageNo);
        if (locationDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(locationDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(locationDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/technician/profile/location/")
    public ResponseEntity<Object> deleteLocation(@RequestBody LocationTechnicianRequest locationTechnicianRequest){
        Object result = technicianService.deleteLocationOfTechnician(locationTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/request/{id_user}/status_code={status_code}")
    public ResponseEntity<Object> getRequestByStatus(@PathVariable String status_code, @PathVariable String id_user, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getByStatusAndTechnician(pageNo, id_user, status_code);
        if(repairRequestDTOS == null){
            errorDTO.setMessage("Can not found status or technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/accept-request/")
    public ResponseEntity<Object> acceptRequest(@RequestBody AcceptRequest acceptRequest){
        Object result = repairRequestService.acceptRequest(acceptRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

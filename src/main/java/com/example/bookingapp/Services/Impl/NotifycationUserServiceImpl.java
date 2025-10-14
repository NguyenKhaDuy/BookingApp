package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Repository.NotificationRepository;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Services.NotifycationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotifycationServiceImpl implements NotifycationService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    StatusRepository statusRepository;
    @Override
    public Page<NotificationDTO> getAllByUser(String id_user, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<NotificationsEntity> notificationsEntities = null;
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        try {
            UserEntity userEntity = userRepository.findById(id_user).get();
//            notificationsEntities = notificationRepository.findByUserEntities(userEntity, pageable);
            for (NotificationsEntity notificationsEntity : notificationsEntities){
                NotificationDTO notificationDTO = new NotificationDTO();
                modelMapper.map(notificationsEntity, notificationDTO);
//                notificationDTO.setStatus_id(notificationsEntity.getStatusEntity().getId_status());
//                notificationDTO.setName_status(notificationsEntity.getStatusEntity().getName_status());
                notificationDTOS.add(notificationDTO);
            }
        }catch (NoSuchElementException ex){
            return null;
        }
        return new PageImpl<>(notificationDTOS, notificationsEntities.getPageable(), notificationsEntities.getTotalElements());
    }

    @Override
    public Object getById(String id_user, Long id_notify) {
        UserEntity userEntity = null;
        NotificationsEntity notificationsEntity = null;
        ErrorDTO errorDTO = new ErrorDTO();
        NotificationDTO notificationDTO = new NotificationDTO();
        try {
            //Tìm kiếm người dùng
            userEntity = userRepository.findById(id_user).get();
            try {
                //Tìm kiếm thông báo thông qua người dùng và id của thông báo
//                notificationsEntity = notificationRepository.findByUserEntitiesAndId_notify(userEntity, id_notify);
                //Lấy ra trạng thái hiện tại của thông báo
//                String status_code = notificationsEntity.getStatusEntity().getName_status();
                //Nếu thông báo chưa đọc thì tiến hành cập lại trạng thái cho thông báo
//                if(status_code.equals("UNREAD")){
                    //Tìm kiếm trạng thái thông qua tên trang thái
//                    StatusEntity statusEntity = statusRepository.findByName_status("READ");
                    //Cập nhật lại trang thái cho thông báo
//                    notificationsEntity.setStatusEntity(statusEntity);
                    //Lưu lại thông báo
                    notificationRepository.save(notificationsEntity);
//                }
                //Lấy ra thông tin của thông báo
                modelMapper.map(notificationsEntity, notificationDTO);
//                notificationDTO.setStatus_id(notificationsEntity.getStatusEntity().getId_status());
//                notificationDTO.setName_status(notificationsEntity.getStatusEntity().getName_status());
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found notification");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return notificationDTO;
    }

    @Override
    public MessageDTO deleteNotification(List<Long> id_notifies) {
        return null;
    }
}

package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import com.example.bookingapp.Models.Request.SendNotificationRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.NotificationService;
import com.example.bookingapp.Services.NotificationUserService;
import com.example.bookingapp.Services.WebSocketService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotifycationUserServiceImpl implements NotificationUserService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    NotificationUserRepository notificationUserRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    NotificationTypeRepository notificationTypeRepository;
    @Override
    public Page<NotificationDTO> getAllByUser(String id_user, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<NotificationUserEntity> notificationUserEntities = null;
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        try {
            UserEntity userEntity = userRepository.findById(id_user).get();
            notificationUserEntities = notificationUserRepository.findByUserEntity(userEntity, pageable);
            for (NotificationUserEntity notificationUserEntity : notificationUserEntities) {
                try {
                    NotificationsEntity notificationsEntity = notificationRepository.findById(notificationUserEntity.getNotificationsEntity().getId_notify()).get();
                    NotificationDTO notificationDTO = new NotificationDTO();
                    modelMapper.map(notificationsEntity, notificationDTO);
                    notificationDTO.setId_type(notificationsEntity.getNotificationTypeEntity().getId());
                    notificationDTO.setType(notificationsEntity.getNotificationTypeEntity().getType());
                    notificationDTO.setStatus_id(notificationUserEntity.getStatusEntity().getId_status());
                    notificationDTO.setName_status(notificationUserEntity.getStatusEntity().getNameStatus());
                    notificationDTOS.add(notificationDTO);
                } catch (NoSuchElementException ex) {
                    continue;
                }
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
        return new PageImpl<>(notificationDTOS, notificationUserEntities.getPageable(), notificationUserEntities.getTotalElements());
    }

    @Override
    public List<NotificationDTO> getAllByUser(String id_user) {
        List<NotificationUserEntity> notificationUserEntities = null;
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        try {
            UserEntity userEntity = userRepository.findById(id_user).get();
            notificationUserEntities = notificationUserRepository.findByUserEntity(userEntity);
            for (NotificationUserEntity notificationUserEntity : notificationUserEntities) {
                try {
                    NotificationsEntity notificationsEntity = notificationRepository.findById(notificationUserEntity.getNotificationsEntity().getId_notify()).get();
                    NotificationDTO notificationDTO = new NotificationDTO();
                    modelMapper.map(notificationsEntity, notificationDTO);
                    notificationDTO.setId_type(notificationsEntity.getNotificationTypeEntity().getId());
                    notificationDTO.setType(notificationsEntity.getNotificationTypeEntity().getType());
                    notificationDTO.setStatus_id(notificationUserEntity.getStatusEntity().getId_status());
                    notificationDTO.setName_status(notificationUserEntity.getStatusEntity().getNameStatus());
                    notificationDTOS.add(notificationDTO);
                } catch (NoSuchElementException ex) {
                    continue;
                }
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
        return notificationDTOS;
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
                //Tìm kiếm thông báo
                notificationsEntity = notificationRepository.findById(id_notify).get();
                //Tìm kiếm thông báo thông qua người dùng và id của thông báo
                NotificationUserEntity notificationUserEntity = notificationUserRepository.findByUserEntityAndNotificationsEntity(userEntity, notificationsEntity);
                //Lấy ra trạng thái hiện tại của thông báo
                String status_code = notificationUserEntity.getStatusEntity().getNameStatus();
                //Nếu thông báo chưa đọc thì tiến hành cập lại trạng thái cho thông báo
                if (status_code.equals("UNREAD")) {
                    //Tìm kiếm trạng thái thông qua tên trang thái
                    StatusEntity statusEntity = statusRepository.findByNameStatus("READ");
                    //Cập nhật lại trang thái cho thông báo
                    notificationUserEntity.setStatusEntity(statusEntity);
                    //Lưu lại trạng thái thông báo
                    notificationUserRepository.save(notificationUserEntity);
                }
                //Lấy ra thông tin của thông báo
                modelMapper.map(notificationsEntity, notificationDTO);
                notificationDTO.setId_type(notificationsEntity.getNotificationTypeEntity().getId());
                notificationDTO.setType(notificationsEntity.getNotificationTypeEntity().getType());
                notificationDTO.setStatus_id(notificationUserEntity.getStatusEntity().getId_status());
                notificationDTO.setName_status(notificationUserEntity.getStatusEntity().getNameStatus());
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found notification");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return notificationDTO;
    }


    //Có thể dùng để xóa 1 thông báo hoặc những thông báo đã được chọn hoặc toàn bộ thông báo của người đùng
    @Override
    public Object deleteNotification(String id_user, DeleteRequest deleteRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            //Tìm kiếm người dùng
            UserEntity userEntity = userRepository.findById(id_user).get();
            if (deleteRequest.getId() != null){
                for (Long id_notify : deleteRequest.getId()) {
                    //Tìm kiếm thông báo
                    NotificationsEntity notificationsEntity = notificationRepository.findById(id_notify).get();
                    //Tìm kiếm thông báo của người dùng
                    NotificationUserEntity notificationUserEntity = notificationUserRepository.findByUserEntityAndNotificationsEntity(userEntity, notificationsEntity);
                    //Xóa thông báo
                    notificationUserRepository.delete(notificationUserEntity);
                }
                //Tạo thông báo cho người dùng là đã xóa thành công
                messageResponse.setMessage("Deleted notifications");
                messageResponse.setHttpStatus(HttpStatus.OK);
            }
            messageResponse.setMessage("id notification null");
            messageResponse.setHttpStatus(HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return messageResponse;
    }

    @Override
    public Object updateStatusNotification(String userId, Long notify_id) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try{
            UserEntity userEntity = userRepository.findById(userId).get();
            NotificationsEntity notificationsEntity = notificationRepository.findById(notify_id).get();
            NotificationUserEntity notificationUserEntity = notificationUserRepository.findByUserEntityAndNotificationsEntity(userEntity, notificationsEntity);
            try {
                StatusEntity statusEntity = statusRepository.findByNameStatus("READ");
                notificationUserEntity.setStatusEntity(statusEntity);
                notificationUserRepository.save(notificationUserEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found status");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notify or user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object sendNotificationToAll(SendNotificationRequest sendNotificationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus("UNREAD");
            //Lấy tất cả người dùng
            List<UserEntity> userEntities = userRepository.findAll();

            MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
            messageNotifiDTO.setType(sendNotificationRequest.getType());
            messageNotifiDTO.setBody(sendNotificationRequest.getBody());
            messageNotifiDTO.setDateTime(LocalDateTime.now());
            messageNotifiDTO.setTitle(sendNotificationRequest.getTitle());
            messageNotifiDTO.setType(sendNotificationRequest.getType());

            //Gửi thông báo cho người dùng
            webSocketService.sendAllUser(messageNotifiDTO);

            //Lưu thông báo cho người dùng
            saveNotificationForUser(messageNotifiDTO, userEntities, statusEntity);

            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Send notification success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.OK);
            return errorDTO;
        }
    }

    @Override
    public Object sendNotificationToUser(SendNotificationRequest sendNotificationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus("UNREAD");


            MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
            messageNotifiDTO.setType(sendNotificationRequest.getType());
            messageNotifiDTO.setBody(sendNotificationRequest.getBody());
            messageNotifiDTO.setDateTime(LocalDateTime.now());
            messageNotifiDTO.setTitle(sendNotificationRequest.getTitle());
            messageNotifiDTO.setType(sendNotificationRequest.getType());

            //Gửi thông báo cho người dùng
            List<UserEntity> userEntities = new ArrayList<>();
            for (String email : sendNotificationRequest.getEmailUser()){
                UserEntity userEntity = userRepository.findByEmail(email);
                userEntities.add(userEntity);
                webSocketService.sendPrivateUser(email, messageNotifiDTO);
            }

            //Lưu thông báo cho người dùng
            saveNotificationForUser(messageNotifiDTO, userEntities, statusEntity);

            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Send notification success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.OK);
            return errorDTO;
        }
    }

    @Override
    public void saveNotificationForUser(MessageNotifiDTO messageNotifiDTO, List<UserEntity> userEntities, StatusEntity statusEntity) {
        for (UserEntity userEntity : userEntities){
            NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());
            NotificationsEntity notificationsEntity = new NotificationsEntity();
            notificationsEntity.setTitle(messageNotifiDTO.getTitle());
            notificationsEntity.setMessage(messageNotifiDTO.getBody());
            notificationsEntity.setCreated_at(LocalDateTime.now());
            notificationsEntity.setUpdated_at(LocalDateTime.now());
            notificationsEntity.setNotificationTypeEntity(notificationTypeEntity);

            NotificationUserEntity notificationUserEntity = new NotificationUserEntity();
            notificationUserEntity.setUserEntity(userEntity);
            notificationUserEntity.setNotificationsEntity(notificationsEntity);
            notificationUserEntity.setStatusEntity(statusEntity);

            notificationsEntity.getNotificationUserEntities().add(notificationUserEntity);

            notificationRepository.save(notificationsEntity);
        }
    }
}

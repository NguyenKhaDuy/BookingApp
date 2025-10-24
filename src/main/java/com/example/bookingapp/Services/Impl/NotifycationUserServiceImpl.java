package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.NotificationUserEntity;
import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Repository.NotificationRepository;
import com.example.bookingapp.Repository.NotificationUserRepository;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Services.NotificationUserService;
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
        MessageDTO messageDTO = new MessageDTO();
        try {
            //Tìm kiếm người dùng
            UserEntity userEntity = userRepository.findById(id_user).get();
            for (Long id_notify : deleteRequest.getId()) {
                //Tìm kiếm thông báo
                NotificationsEntity notificationsEntity = notificationRepository.findById(id_notify).get();
                //Tìm kiếm thông báo của người dùng
                NotificationUserEntity notificationUserEntity = notificationUserRepository.findByUserEntityAndNotificationsEntity(userEntity, notificationsEntity);
                //Xóa thông báo
                notificationUserRepository.delete(notificationUserEntity);
            }
            //Tạo thông báo cho người dùng là đã xóa thành công
            messageDTO.setMessage("Deleted notifications");
            messageDTO.setHttpStatus(HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return messageDTO;
    }
}

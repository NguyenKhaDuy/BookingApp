package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.RepairRequestService;
import com.example.bookingapp.Services.WebSocketService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.ConvertEntityToDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class RepairRequestServiceImpl implements RepairRequestService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    ImageRequestRepository imageRequestRepository;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    TechnicianServiceImpl technicianService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    NotificationTypeRepository notificationTypeRepository;
    @Autowired
    TechnicianRefusedRquestRepository technicianRefusedRquestRepository;
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    public Object createRepairRequest(RequestCustomerRequest requestCustomerRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        CustomerEntity customerEntity = null;
        ServiceEntity serviceEntity = null;
        StatusEntity statusEntity = null;
        TechnicianEntity technicianEntity = null;
        //Bắt lỗi tìm kiếm khách hàng
        try {
            customerEntity = customerRepository.findById(requestCustomerRequest.getId_customer()).get();
            if(customerEntity.getPhone_number() == null || customerEntity.getAddress() == null){
                messageResponse.setMessage("Vui lòng cập nhật thông tin trước khi đặt yêu cầu");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            }
            //Bắt lỗi tìm kiếm dịch vụ
            try {
                serviceEntity = serviceRepository.findById(requestCustomerRequest.getId_service()).get();
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found service");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }

            if (requestCustomerRequest.getId_technician() == null) {
                statusEntity = statusRepository.findByNameStatus("SEARCHING");
            } else {
                statusEntity = statusRepository.findByNameStatus("WAITING_FOR_TECHNICIAN");
            }

            //Khởi tạo một yêu cầu mới
            RepairRequestEntity repairRequestEntity = new RepairRequestEntity();
            modelMapper.map(requestCustomerRequest, repairRequestEntity);
            repairRequestEntity.setStatusEntity(statusEntity);
            repairRequestEntity.setCustomerEntity(customerEntity);
            repairRequestEntity.setServiceEntity(serviceEntity);
            repairRequestEntity.setCreated_at(LocalDateTime.now());
            repairRequestEntity.setUpdated_at(LocalDateTime.now());
            //Khách hàng có thể gửi nhiều hình ảnh mô tả về sự cố
            // lặp qua từng ảnh và lưu vào database
            for (MultipartFile file : requestCustomerRequest.getImageRequest()) {
                //Tạo một đối tượng hình ảnh mới
                ImageRequestEntity imageRequestEntity = new ImageRequestEntity();
                try {
                    //Chuyển từ multipartfile sang byte để lưu
                    imageRequestEntity.setImage(file.getBytes());
                } catch (IOException ex) {
                    errorDTO.setMessage("Can not convert Multipartfile to byte");
                    errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    return errorDTO;
                }
                imageRequestEntity.setCreated_at(LocalDateTime.now());
                imageRequestEntity.setUpdated_at(LocalDateTime.now());
                //tiến hành set image rồi mới add vào list hình ảnh của repair
                imageRequestEntity.setRepairRequestEntity(repairRequestEntity);
                repairRequestEntity.getImageRequestEntities().add(imageRequestEntity);
            }
            //Tiến hành lưu vào database
            RepairRequestEntity repairRequest = repairRequestRepository.save(repairRequestEntity);

            //Kiểm tra xem khách hàng có yêu cầu 1 thợ cụ thể hay không nếu không sẽ để null và sẽ
            //tìm kiếm đến một thợ gần khách nhất hoặc random ngẫu nhiên để thông báo nhận yêu cầu cho
            //một thợ theo dịch vụ
            if (requestCustomerRequest.getId_technician() != null) {
                try {
                    technicianEntity = technicianRepository.findById(requestCustomerRequest.getId_technician()).get();
                    //thông báo đến thợ
                    String title = "Có đơn hàng mới";
                    String body = "Vui lòng xác nhận để nhận đơn hàng";
                    String type = "REQUEST_CREATED";
                    MessageNotifyRequestDTO messageNotifyRequestDTO = new MessageNotifyRequestDTO();
                    messageNotifyRequestDTO.setType(type);
                    messageNotifyRequestDTO.setTitle(title);
                    messageNotifyRequestDTO.setBody(body);
                    messageNotifyRequestDTO.setDateTime(LocalDateTime.now());
                    messageNotifyRequestDTO.setId_request(repairRequest.getId_request());
                    webSocketService.sendPrivateUser(technicianEntity.getEmail(), messageNotifyRequestDTO);
                    saveNotification(messageNotifyRequestDTO, technicianEntity);
                    messageResponse.setMessage("SUCCESS");
                    messageResponse.setHttpStatus(HttpStatus.OK);
                    return messageResponse;
                } catch (NoSuchElementException ex) {
                    errorDTO.setMessage("Can not found service");
                    errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                    return errorDTO;
                }
            }
            // chạy tìm thợ BẤT ĐỒNG BỘ
//            findTechnicianAsync(null, repairRequest.getId_request());
            startFindTechnician(repairRequest.getId_request());

            messageResponse.setMessage("SUCCESS");
            messageResponse.setHttpStatus(HttpStatus.OK);

            return messageResponse;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    private final Map<Long, Future<?>> asyncTasks = new ConcurrentHashMap<>();
    private String id_tech = null;

    public void startFindTechnician(Long requestId) {
        Future<?> future = ((AsyncTaskExecutor) taskExecutor).submit(() -> {
            findTechnicianAsync(null, requestId);
        });
        asyncTasks.put(requestId, future);
    }


    public void findTechnicianAsync(String idTechRefuse, Long idRequest) {

        try {
            //LOAD REQUEST BAN ĐẦU
            RepairRequestEntity request =
                    repairRequestRepository.findById(idRequest).orElse(null);
            if (request == null) return;

            if (!"SEARCHING".equals(request.getStatusEntity().getNameStatus()))
            {
                return;
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            //TÌM THỢ (CÓ BLOCK + SLEEP)
            String idTechnician = loadTechnician(idTechRefuse, idRequest);

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            //LOAD LẠI REQUEST SAU KHI TÌM
            request = repairRequestRepository.findById(idRequest).orElse(null);
            if (request == null) {
                return;
            }

            //KHÁCH ĐÃ HỦY TRONG LÚC ĐANG TÌM / ĐANG CHỜ
            if (!"SEARCHING".equals(request.getStatusEntity().getNameStatus())) {
                return;
            }

            //KHÔNG CÓ THỢ
            if (idTechnician == null) {
                request.setStatusEntity(
                        statusRepository.findByNameStatus("CANCEL")
                );
                repairRequestRepository.save(request);

                MessageNotifyRequestDTO dto = new MessageNotifyRequestDTO();
                dto.setType("REQUEST_CANCEL");
                dto.setTitle("Yêu cầu đã bị hủy");
                dto.setBody("Không có thợ phù hợp, vui lòng thử lại sau");
                dto.setDateTime(LocalDateTime.now());
                dto.setId_request(idRequest);

                webSocketService.sendPrivateUser(
                        request.getCustomerEntity().getEmail(), dto);
                saveNotification(dto, request.getCustomerEntity());
                return;
            }

            //LOAD TECH
            TechnicianEntity tech =
                    technicianRepository.findById(idTechnician).orElse(null);
            if (tech == null){
                return;
            }

            //CHECK LẠI TRƯỚC KHI NOTIFY
            request = repairRequestRepository.findById(idRequest).orElse(null);
            if (request == null) {
                return;
            }

            if (!"SEARCHING".equals(request.getStatusEntity().getNameStatus())) {
                // khách đã bấm HỦY trong lúc vừa tìm ra thợ
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // NOTIFY TECH
            MessageNotifyRequestDTO dto = new MessageNotifyRequestDTO();
            dto.setType("REQUEST_CREATED");
            dto.setTitle("Có đơn hàng mới");
            dto.setBody("Vui lòng xác nhận để nhận đơn");
            dto.setDateTime(LocalDateTime.now());
            dto.setId_request(idRequest);
            id_tech = idTechnician;
            webSocketService.sendPrivateUser(tech.getEmail(), dto);
            saveNotification(dto, tech);

            //CHECK LẦN CUỐI TRƯỚC KHI SET
            request = repairRequestRepository.findById(idRequest).orElse(null);
            if (request == null) {
                return;
            }

            if (!"SEARCHING".equals(request.getStatusEntity().getNameStatus())) {
                //khách bấm HỦY NGAY SAU KHI SEND NOTIFY
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // SET THỢ + CHUYỂN TRẠNG THÁI
            request.setTechnicianEntity(tech);
            request.setStatusEntity(
                    statusRepository.findByNameStatus("WAITING_FOR_TECHNICIAN")
            );
            repairRequestRepository.save(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String loadTechnician(String idTechRefuse, Long id_request) {

        int maxRetries = 10;
        long retryInterval = 10_000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            RepairRequestEntity request =
                    repairRequestRepository.findById(id_request).orElse(null);

            //request không còn tồn tại
            if (request == null) {
                return null;
            }

            //khách đã hủy
            if (request.getStatusEntity().getNameStatus().equals("CANCEL")) {
                return null;
            }

            //không còn trạng thái tìm thợ
            if (!request.getStatusEntity().getNameStatus().equals("SEARCHING")) {
                return null;
            }

            String idTechnician = technicianService.filterTechnician(
                    request.getScheduled_time(),
                    request.getScheduled_date(),
                    request.getServiceEntity().getId_service(),
                    idTechRefuse
            );

            if (idTechnician != null) {

                TechnicianEntity technicianEntity = technicianRepository.findById(idTechnician).orElse(null);

                if (technicianEntity == null) {
                    continue;
                }

                TechnicianRefusedRequestEntity refused = technicianRefusedRquestRepository.findByTechnicianEntityAndRepairRequestEntity(technicianEntity, request);

                if (refused == null) {
                    return idTechnician;
                }
            }

            // SLEEP NHƯNG CHO PHÉP DỪNG NGAY
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    //bắt interrupt khi cancel
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }



    @Override
    public Page<RepairRequestDTO> getAll(Integer pageNo) {
        //setup pageable để tryền vào repo để phân trang
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findAll(pageable);
        List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
        for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
            RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
            modelMapper.map(repairRequestEntity, repairRequestDTO);

            //Lấy ra customer
            CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
            CustomerDTO customerDTO = new CustomerDTO();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi yêu cầu chưa có thợ nhận yêu cầu sẽ trả về null
            try {
                TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                repairRequestDTO.setTechnicicanDTO(technicicanDTO);
            } catch (NullPointerException ex) {
                repairRequestDTO.setTechnicicanDTO(null);
            }

            //Lấy ra dịch vụ đã yêu cầu
            ServiceEntity service = repairRequestEntity.getServiceEntity();
            repairRequestDTO.setName_service(service.getName_service());

            //Lấy ra hình ảnh kèm theo của yêu cầu
            for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
            }
            repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

            //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
            // tức là chưa có hóa đơn
            try {
                //lấy ra hóa đơn của đơn hàng
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                modelMapper.map(invoicesEntity, invoicesDTO);
                //lấy ra danh sách chi tiết của hóa đơn
                List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                    detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                    detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                    detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                    detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                    detailInvoiceDTOS.add(detailInvoiceDTO);
                }
                invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                repairRequestDTO.setInvoices(invoicesDTO);
            } catch (IllegalArgumentException ex) {
                repairRequestDTO.setInvoices(null);
            }

            //Lấy ra đánh giá của yêu cầu
            for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                FeedbackDTO feedbackDTO = new FeedbackDTO();
                modelMapper.map(feedbackEntity, feedbackDTO);
                repairRequestDTO.getFeedback().add(feedbackDTO);
            }

            repairRequestDTOS.add(repairRequestDTO);
        }
        return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
    }

    @Override
    public List<RepairRequestDTO> getAllByCustomer(String id_user) {
        List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
        List<RepairRequestEntity> repairRequestEntities = null;
        try {
            //Tìm user
            CustomerEntity customerEntity = customerRepository.findById(id_user).get();
            //tìm yêu cầu thông qua user
            repairRequestEntities = repairRequestRepository.findByCustomerEntity(customerEntity);
            for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
                RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
                modelMapper.map(repairRequestEntity, repairRequestDTO);
                CustomerDTO customerDTO = new CustomerDTO();
                modelMapper.map(customerEntity, customerDTO);
                customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
                for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }
                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi yêu cầu chưa có thợ nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                    technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                    repairRequestDTO.setTechnicicanDTO(technicicanDTO);
                } catch (NullPointerException ex) {
                    repairRequestDTO.setTechnicicanDTO(null);
                }

                //Lấy ra dịch vụ đã yêu cầu
                ServiceEntity service = repairRequestEntity.getServiceEntity();
                repairRequestDTO.setName_service(service.getName_service());

                //Lấy ra hình ảnh kèm theo của yêu cầu
                for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                    repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
                }
                repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

                //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
                // tức là chưa có hóa đơn
                try {
                    //lấy ra hóa đơn của đơn hàng
                    InvoicesDTO invoicesDTO = new InvoicesDTO();
                    InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                    modelMapper.map(invoicesEntity, invoicesDTO);
                    //lấy ra danh sách chi tiết của hóa đơn
                    List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                    for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                        DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                        detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                        detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                        detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                        detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                        detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                        detailInvoiceDTOS.add(detailInvoiceDTO);
                    }
                    invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                    repairRequestDTO.setInvoices(invoicesDTO);
                } catch (IllegalArgumentException ex) {
                    repairRequestDTO.setInvoices(null);
                }

                //Lấy ra đánh giá của yêu cầu
                for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    modelMapper.map(feedbackEntity, feedbackDTO);
                    repairRequestDTO.getFeedback().add(feedbackDTO);
                }

                repairRequestDTOS.add(repairRequestDTO);
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
        return repairRequestDTOS;
    }

    @Override
    public Object getById(Long id_request) {
        RepairRequestEntity repairRequestEntity = null;
        ErrorDTO errorDTO = new ErrorDTO();
        RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
        try {
            repairRequestEntity = repairRequestRepository.findById(id_request).get();
            //Tìm user
            CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
            modelMapper.map(repairRequestEntity, repairRequestDTO);

            CustomerDTO customerDTO = new CustomerDTO();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            try {
                TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                repairRequestDTO.setTechnicicanDTO(technicicanDTO);
            } catch (NullPointerException ex) {
                repairRequestDTO.setTechnicicanDTO(null);
            }

            //Lấy ra dịch vụ đã yêu cầu
            ServiceEntity service = repairRequestEntity.getServiceEntity();
            repairRequestDTO.setName_service(service.getName_service());

            //Lấy ra hình ảnh kèm theo của yêu cầu
            for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
            }
            repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

            //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
            // tức là chưa có hóa đơn
            try {
                //lấy ra hóa đơn của đơn hàng
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                modelMapper.map(invoicesEntity, invoicesDTO);
                //lấy ra danh sách chi tiết của hóa đơn
                List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                    detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                    detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                    detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                    detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                    detailInvoiceDTOS.add(detailInvoiceDTO);
                }
                invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                repairRequestDTO.setInvoices(invoicesDTO);
            } catch (IllegalArgumentException ex) {
                repairRequestDTO.setInvoices(null);
            }

            //Lấy ra đánh giá của yêu cầu
            for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                FeedbackDTO feedbackDTO = new FeedbackDTO();
                modelMapper.map(feedbackEntity, feedbackDTO);
                repairRequestDTO.getFeedback().add(feedbackDTO);
            }

        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return repairRequestDTO;
    }

    @Override
    public Object cancelRequest(Long id_request) {

        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();

        try {
            RepairRequestEntity request =
                    repairRequestRepository.findById(id_request).orElse(null);

            if (request == null) {
                errorDTO.setMessage("Can not found request");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }

            String status = request.getStatusEntity().getNameStatus();

            // chỉ cấm khi đã làm xong hoặc đang sửa
            if ("IN_PROGRESS".equals(status) || "COMPLETED".equals(status)) {
                messageResponse.setMessage("Can not cancel request");
                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                return messageResponse;
            }

            // DỪNG LUỒNG ASYNC
            Future<?> future = asyncTasks.get(id_request);
            if (future != null) {
                future.cancel(true);
                asyncTasks.remove(id_request);
            }

            //gửi thông báo đến thợ khi mà yêu cầu đang chờ thợ đồng ý
            if(id_tech != null){
                MessageNotifyRequestDTO dto = new MessageNotifyRequestDTO();
                dto.setType("REQUEST_CANCEL");
                dto.setTitle("Yêu cầu đã bị hủy");
                dto.setBody("Khách hàng đã hủy yêu cầu");
                dto.setDateTime(LocalDateTime.now());
                dto.setId_request(id_request);
                TechnicianEntity technicianEntity = technicianRepository.findById(id_tech).orElse(null);
                webSocketService.sendPrivateUser(technicianEntity.getEmail(), dto);
                saveNotification(dto, technicianEntity);
            }

            request.setStatusEntity(
                    statusRepository.findByNameStatus("CANCEL")
            );
            request.setUpdated_at(LocalDateTime.now());
            request.setTechnicianEntity(null);
            repairRequestRepository.save(request);

            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;

        } catch (Exception e) {
            errorDTO.setMessage("Error");
            errorDTO.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return errorDTO;
        }
    }


    @Override
    public Page<RepairRequestDTO> getByStatusAndCustomer(Integer pageNo, String id_user, String status_code) {
        //setup pageable để tryền vào repo để phân trang
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus(status_code);
            CustomerEntity customerEntity = customerRepository.findById(id_user).get();
            Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findByStatusEntityAndCustomerEntity(statusEntity, customerEntity, pageable);
            List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
            for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
                RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
                modelMapper.map(repairRequestEntity, repairRequestDTO);

                //Lấy ra customer
                CustomerDTO customerDTO = new CustomerDTO();
                modelMapper.map(customerEntity, customerDTO);
                customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
                for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                    technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                    repairRequestDTO.setTechnicicanDTO(technicicanDTO);
                } catch (NullPointerException ex) {
                    repairRequestDTO.setTechnicicanDTO(null);
                }

                //Lấy ra dịch vụ đã yêu cầu
                ServiceEntity service = repairRequestEntity.getServiceEntity();
                repairRequestDTO.setName_service(service.getName_service());

                //Lấy ra hình ảnh kèm theo của yêu cầu
                for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                    repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
                }
                repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

                //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
                // tức là chưa có hóa đơn
                try {
                    //lấy ra hóa đơn của đơn hàng
                    InvoicesDTO invoicesDTO = new InvoicesDTO();
                    InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                    modelMapper.map(invoicesEntity, invoicesDTO);
                    //lấy ra danh sách chi tiết của hóa đơn
                    List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                    for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                        DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                        detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                        detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                        detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                        detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                        detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                        detailInvoiceDTOS.add(detailInvoiceDTO);
                    }
                    invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                    repairRequestDTO.setInvoices(invoicesDTO);
                } catch (IllegalArgumentException ex) {
                    repairRequestDTO.setInvoices(null);
                }

                //Lấy ra đánh giá của yêu cầu
                for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    modelMapper.map(feedbackEntity, feedbackDTO);
                    repairRequestDTO.getFeedback().add(feedbackDTO);
                }

                repairRequestDTOS.add(repairRequestDTO);
            }
            return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public Page<RepairRequestDTO> getByStatus(Integer pageNo, String status_code) {
        //setup pageable để tryền vào repo để phân trang
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus(status_code);
            Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findByStatusEntity(statusEntity, pageable);
            List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
            for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
                RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
                modelMapper.map(repairRequestEntity, repairRequestDTO);

                //Lấy ra customer
                CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
                CustomerDTO customerDTO = new CustomerDTO();
                modelMapper.map(customerEntity, customerDTO);
                customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
                for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                    technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                    repairRequestDTO.setTechnicicanDTO(technicicanDTO);
                } catch (NullPointerException ex) {
                    repairRequestDTO.setTechnicicanDTO(null);
                }

                //Lấy ra dịch vụ đã yêu cầu
                ServiceEntity service = repairRequestEntity.getServiceEntity();
                repairRequestDTO.setName_service(service.getName_service());

                //Lấy ra hình ảnh kèm theo của yêu cầu
                for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                    repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
                }
                repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

                //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
                // tức là chưa có hóa đơn
                try {
                    //lấy ra hóa đơn của đơn hàng
                    InvoicesDTO invoicesDTO = new InvoicesDTO();
                    InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                    modelMapper.map(invoicesEntity, invoicesDTO);
                    //lấy ra danh sách chi tiết của hóa đơn
                    List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                    for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                        DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                        detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                        detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                        detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                        detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                        detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                        detailInvoiceDTOS.add(detailInvoiceDTO);
                    }
                    invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                    repairRequestDTO.setInvoices(invoicesDTO);
                } catch (IllegalArgumentException ex) {
                    repairRequestDTO.setInvoices(null);
                }

                //Lấy ra đánh giá của yêu cầu
                for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    modelMapper.map(feedbackEntity, feedbackDTO);
                    repairRequestDTO.getFeedback().add(feedbackDTO);
                }

                repairRequestDTOS.add(repairRequestDTO);
            }
            return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public MessageResponse deleteRequest(DeleteRequest deleteRequest) {
        MessageResponse messageResponse = new MessageResponse();
        for (Long id_request : deleteRequest.getId()) {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(id_request).get();
            repairRequestRepository.delete(repairRequestEntity);
        }
        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public Page<RepairRequestDTO> getByTechnician(Integer pageNo, String id_user) {
        //setup pageable để tryền vào repo để phân trang
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findByTechnicianEntity(technicianEntity, pageable);
            List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
            for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
                RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
                modelMapper.map(repairRequestEntity, repairRequestDTO);

                //Lấy ra customer
                CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
                CustomerDTO customerDTO = new CustomerDTO();
                modelMapper.map(customerEntity, customerDTO);
                customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
                for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                    technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                    repairRequestDTO.setTechnicicanDTO(technicicanDTO);
                } catch (NullPointerException ex) {
                    repairRequestDTO.setTechnicicanDTO(null);
                }

                //Lấy ra dịch vụ đã yêu cầu
                ServiceEntity service = repairRequestEntity.getServiceEntity();
                repairRequestDTO.setName_service(service.getName_service());

                //Lấy ra hình ảnh kèm theo của yêu cầu
                for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                    repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
                }
                repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

                //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
                // tức là chưa có hóa đơn
                try {
                    //lấy ra hóa đơn của đơn hàng
                    InvoicesDTO invoicesDTO = new InvoicesDTO();
                    InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                    modelMapper.map(invoicesEntity, invoicesDTO);
                    //lấy ra danh sách chi tiết của hóa đơn
                    List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                    for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                        DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                        detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                        detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                        detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                        detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                        detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                        detailInvoiceDTOS.add(detailInvoiceDTO);
                    }
                    invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                    repairRequestDTO.setInvoices(invoicesDTO);
                } catch (IllegalArgumentException ex) {
                    repairRequestDTO.setInvoices(null);
                }

                //Lấy ra đánh giá của yêu cầu
                for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    modelMapper.map(feedbackEntity, feedbackDTO);
                    repairRequestDTO.getFeedback().add(feedbackDTO);
                }

                repairRequestDTOS.add(repairRequestDTO);
            }
            return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public Object acceptRequest(AcceptRequest acceptRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(acceptRequest.getId_request()).get();
            //Kiểm tra xem yêu cầu có bị hủy hay chưa và đã có thợ nào nhận yêu cầu hay chưa
            if (!repairRequestEntity.getStatusEntity().getNameStatus().equals("CANCEL")) {
                if (repairRequestEntity.getStatusEntity().getNameStatus().equals("WAITING_FOR_TECHNICIAN")) {
                    //Tìm kiếm thợ thông qua id
                    TechnicianEntity technicianEntity = technicianRepository.findById(acceptRequest.getId_technician()).get();
                    repairRequestEntity.setTechnicianEntity(technicianEntity);

                    //Tìm kiếm trạng thái nhận và cập nhật lại trạng thái cho yêu cầu
                    StatusEntity statusEntity = statusRepository.findByNameStatus("RECEIVED");
                    repairRequestEntity.setStatusEntity(statusEntity);

                    //Gửi thông báo đến người dùng là thợ đã nhận yêu cầu
                    String title = "Yêu cầu đã được nhận";
                    String body = "Yêu cầu " + repairRequestEntity.getId_request() + " của bạn được được thợ tên " + technicianEntity.getFull_name() + " nhận";
                    String type = "ACCEPTED_REQUEST";
                    String emailCustomer = repairRequestEntity.getCustomerEntity().getEmail();

                    MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
                    messageNotifiDTO.setType(type);
                    messageNotifiDTO.setDateTime(LocalDateTime.now());
                    messageNotifiDTO.setBody(body);
                    messageNotifiDTO.setTitle(title);
                    webSocketService.sendPrivateUser(emailCustomer, messageNotifiDTO);

                    //lưu lại thông báo
                    saveNotification(messageNotifiDTO, repairRequestEntity.getCustomerEntity());

                    //Cập nhật lại yêu cầu
                    repairRequestEntity.setUpdated_at(LocalDateTime.now());
                    repairRequestRepository.save(repairRequestEntity);

                    //Cộng thêm hiệu suất cho chợ
                    Long newEfficiency = technicianEntity.getEfficiency() + 2;
                    technicianEntity.setEfficiency(newEfficiency);
                    technicianRepository.save(technicianEntity);

                    messageResponse.setMessage("Success");
                    messageResponse.setHttpStatus(HttpStatus.OK);
                } else {
                    messageResponse.setMessage("Request has been received by technician");
                    messageResponse.setHttpStatus(HttpStatus.OK);
                }
            } else {
                messageResponse.setMessage("Request canceled");
                messageResponse.setHttpStatus(HttpStatus.OK);
            }
            return messageResponse;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object refuseRequest(String id_tech, Long id_request) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            //thợ đã từ chối đơn hàng
            RepairRequestEntity request =
                    repairRequestRepository.findById(id_request).orElseThrow();

            request.setTechnicianEntity(null);
            request.setStatusEntity(
                    statusRepository.findByNameStatus("SEARCHING")
            );
            repairRequestRepository.save(request);

            TechnicianEntity tech =
                    technicianRepository.findById(id_tech).get();
            tech.setEfficiency(tech.getEfficiency() - 2);
            technicianRepository.save(tech);

            // thêm thợ vào thợ từ chối yêu cầu
            TechnicianRefusedRequestEntity technicianRefusedRequestEntity = new TechnicianRefusedRequestEntity();
            technicianRefusedRequestEntity.setTechnicianEntity(tech);
            technicianRefusedRequestEntity.setRepairRequestEntity(request);
            technicianRefusedRquestRepository.save(technicianRefusedRequestEntity);

            // tìm thợ mới (loại trừ thợ vừa từ chối)
            findTechnicianAsync(id_tech, id_request);

            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Page<RepairRequestDTO> searchRequest(SearchRequest searchRequest, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.searchRequest(searchRequest, pageable);
        List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
        for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
            RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
            modelMapper.map(repairRequestEntity, repairRequestDTO);

            //Lấy ra customer
            CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
            CustomerDTO customerDTO = new CustomerDTO();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                customerDTO.getRoleDTOS().add(roleDTO);
            }

            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
            try {
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                repairRequestDTO.setTechnicicanDTO(technicicanDTO);
            } catch (NullPointerException ex) {
                repairRequestDTO.setTechnicicanDTO(null);
            }

            //Lấy ra dịch vụ đã yêu cầu
            ServiceEntity service = repairRequestEntity.getServiceEntity();
            repairRequestDTO.setName_service(service.getName_service());

            //Lấy ra hình ảnh kèm theo của yêu cầu
            for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
            }
            repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

            //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
            // tức là chưa có hóa đơn
            try {
                //lấy ra hóa đơn của đơn hàng
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                modelMapper.map(invoicesEntity, invoicesDTO);
                //lấy ra danh sách chi tiết của hóa đơn
                List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                    detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                    detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                    detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                    detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                    detailInvoiceDTOS.add(detailInvoiceDTO);
                }
                invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                repairRequestDTO.setInvoices(invoicesDTO);
            } catch (IllegalArgumentException ex) {
                repairRequestDTO.setInvoices(null);
            }

            //Lấy ra đánh giá của yêu cầu
            for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                FeedbackDTO feedbackDTO = new FeedbackDTO();
                modelMapper.map(feedbackEntity, feedbackDTO);
                repairRequestDTO.getFeedback().add(feedbackDTO);
            }

            repairRequestDTOS.add(repairRequestDTO);
        }
        return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
    }

    @Override
    public Page<RepairRequestDTO> fillterRequest(FillterRequest fillterRequest, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.fillterRequest(fillterRequest, pageable);
        List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
        for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
            RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
            modelMapper.map(repairRequestEntity, repairRequestDTO);

            //Lấy ra customer
            CustomerEntity customerEntity = repairRequestEntity.getCustomerEntity();
            CustomerDTO customerDTO = new CustomerDTO();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                customerDTO.getRoleDTOS().add(roleDTO);
            }

            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
            try {
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                repairRequestDTO.setTechnicicanDTO(technicicanDTO);
            } catch (NullPointerException ex) {
                repairRequestDTO.setTechnicicanDTO(null);
            }

            //Lấy ra dịch vụ đã yêu cầu
            ServiceEntity service = repairRequestEntity.getServiceEntity();
            repairRequestDTO.setName_service(service.getName_service());

            //Lấy ra hình ảnh kèm theo của yêu cầu
            for (ImageRequestEntity imageRequestEntity : repairRequestEntity.getImageRequestEntities()) {
                repairRequestDTO.getImage_request().add(ConvertByteToBase64.toBase64(imageRequestEntity.getImage()));
            }
            repairRequestDTO.setStatus_code(repairRequestEntity.getStatusEntity().getNameStatus());

            //Bắt lỗi trường hợp yêu cầu đang trong trạng thái chưa hoàn thành hoặc chưa có thợ nhận
            // tức là chưa có hóa đơn
            try {
                //lấy ra hóa đơn của đơn hàng
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                InvoicesEntity invoicesEntity = repairRequestEntity.getInvoicesEntity();
                modelMapper.map(invoicesEntity, invoicesDTO);
                //lấy ra danh sách chi tiết của hóa đơn
                List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                    detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                    detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                    detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                    detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                    detailInvoiceDTOS.add(detailInvoiceDTO);
                }
                invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                repairRequestDTO.setInvoices(invoicesDTO);
            } catch (IllegalArgumentException ex) {
                repairRequestDTO.setInvoices(null);
            }

            //Lấy ra đánh giá của yêu cầu
            for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()) {
                FeedbackDTO feedbackDTO = new FeedbackDTO();
                modelMapper.map(feedbackEntity, feedbackDTO);
                repairRequestDTO.getFeedback().add(feedbackDTO);
            }

            repairRequestDTOS.add(repairRequestDTO);
        }
        return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
    }

    @Override
    public Object updateStatusRequest(UpdateStatusRquest updateStatusRquest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(updateStatusRquest.getId_request()).get();
            try {
                StatusEntity statusEntity = statusRepository.findById(updateStatusRquest.getId_status()).get();
                repairRequestEntity.setStatusEntity(statusEntity);
                repairRequestEntity.setUpdated_at(LocalDateTime.now());
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found status");
                errorDTO.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            }
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }
    }

    public void saveNotification(MessageNotifiDTO messageNotifiDTO, UserEntity userEntity) {
        //tạo thông báo mới để lưu vào cơ sở dữ liệu
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());
        NotificationsEntity notificationsEntity = new NotificationsEntity();
        notificationsEntity.setTitle(messageNotifiDTO.getTitle());
        notificationsEntity.setMessage(messageNotifiDTO.getBody());
        notificationsEntity.setNotificationTypeEntity(notificationTypeEntity);
        notificationsEntity.setCreatedAt(LocalDateTime.now());
        notificationsEntity.setUpdated_at(LocalDateTime.now());

        NotificationUserEntity userNotify = new NotificationUserEntity();
        StatusEntity statusNotify = statusRepository.findByNameStatus("UNREAD");
        userNotify.setStatusEntity(statusNotify);
        userNotify.setUserEntity(userEntity);
        userNotify.setNotificationsEntity(notificationsEntity);

        //thêm vào notify
        notificationsEntity.getNotificationUserEntities().add(userNotify);
        //lưu vào cơ sở dữ liệu
        notificationRepository.save(notificationsEntity);
    }
}

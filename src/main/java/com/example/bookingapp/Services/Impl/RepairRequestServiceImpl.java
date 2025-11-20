package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.RepairRequestService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    ModelMapper modelMapper;

    @Override
    public Object createRepairRequest(RequestCustomerRequest requestCustomerRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        CustomerEntity customerEntity = null;
        ServiceEntity serviceEntity = null;
        StatusEntity statusEntity = null;
        TechnicianEntity technicianEntity = null;
        //Bắt lỗi tìm kiếm khách hàng
        try {
            customerEntity = customerRepository.findById(requestCustomerRequest.getId_customer()).get();
            //Bắt lỗi tìm kiếm dịch vụ
            try {
                serviceEntity = serviceRepository.findById(requestCustomerRequest.getId_service()).get();
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found service");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Kiểm tra xem khách hàng có yêu cầu 1 thợ cụ thể hay không nếu không sẽ để null và sẽ
            //tìm kiếm đến một thợ gần khách nhất hoặc random ngẫu nhiên để thông báo nhận yêu cầu cho
            //một thợ theo dịch vụ
            if (requestCustomerRequest.getId_technician() != null) {
                try {
                    technicianEntity = technicianRepository.findById(requestCustomerRequest.getId_technician()).get();
                } catch (NoSuchElementException ex) {
                    errorDTO.setMessage("Can not found service");
                    errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                    return errorDTO;
                }
            }
            //Tìm status để set cho yêu cầu
            try {
                statusEntity = statusRepository.findByNameStatus("WAITING_FOR_TECHNICIAN");
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found status");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Khởi tạo một yêu cầu mới
            RepairRequestEntity repairRequestEntity = new RepairRequestEntity();
            modelMapper.map(requestCustomerRequest, repairRequestEntity);
            repairRequestEntity.setStatusEntity(statusEntity);
            repairRequestEntity.setCustomerEntity(customerEntity);
            repairRequestEntity.setServiceEntity(serviceEntity);
            repairRequestEntity.setTechnicianEntity(technicianEntity);
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
            repairRequestRepository.save(repairRequestEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
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
                roleDTO.setRole_name(roleEntity.getRole_name());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi yêu cầu chưa có thợ nhận yêu cầu sẽ trả về null
            try {
                TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                repairRequestDTO.setId_technician(technicianEntity.getId_user());
            } catch (NullPointerException ex) {
                repairRequestDTO.setName_techinician(null);
                repairRequestDTO.setId_technician(null);
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
    public Page<RepairRequestDTO> getAllByCustomer(Integer pageNo, String id_user) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        List<RepairRequestDTO> repairRequestDTOS = new ArrayList<>();
        Page<RepairRequestEntity> repairRequestEntities = null;
        try {
            //Tìm user
            CustomerEntity customerEntity = customerRepository.findById(id_user).get();
            //tìm yêu cầu thông qua user
            repairRequestEntities = repairRequestRepository.findByCustomerEntity(customerEntity, pageable);
            for (RepairRequestEntity repairRequestEntity : repairRequestEntities) {
                RepairRequestDTO repairRequestDTO = new RepairRequestDTO();
                modelMapper.map(repairRequestEntity, repairRequestDTO);

                CustomerDTO customerDTO = new CustomerDTO();
                modelMapper.map(customerEntity, customerDTO);
                customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
                for (RoleEntity roleEntity : customerEntity.getRoleEntities()) {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRole_name());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }
                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi yêu cầu chưa có thợ nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                    repairRequestDTO.setId_technician(technicianEntity.getId_user());
                } catch (NullPointerException ex) {
                    repairRequestDTO.setName_techinician(null);
                    repairRequestDTO.setId_technician(null);
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
        return new PageImpl<>(repairRequestDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
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
                roleDTO.setRole_name(roleEntity.getRole_name());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            try {
                TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                repairRequestDTO.setId_technician(technicianEntity.getId_user());
            } catch (NullPointerException ex) {
                repairRequestDTO.setName_techinician(null);
                repairRequestDTO.setId_technician(null);
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
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            StatusEntity statusEntity = null;
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(id_request).get();
            try {
                statusEntity = statusRepository.findByNameStatus("CANCEL");
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found status");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
            if (technicianEntity != null) {
                messageDTO.setMessage("Can not cancel request");
                messageDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            } else {
                repairRequestEntity.setStatusEntity(statusEntity);
                repairRequestEntity.setUpdated_at(LocalDateTime.now());
                repairRequestRepository.save(repairRequestEntity);
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
            }
            return messageDTO;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
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
                    roleDTO.setRole_name(roleEntity.getRole_name());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                    repairRequestDTO.setId_technician(technicianEntity.getId_user());
                } catch (NullPointerException ex) {
                    repairRequestDTO.setName_techinician(null);
                    repairRequestDTO.setId_technician(null);
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
                    roleDTO.setRole_name(roleEntity.getRole_name());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                    repairRequestDTO.setId_technician(technicianEntity.getId_user());
                } catch (NullPointerException ex) {
                    repairRequestDTO.setName_techinician(null);
                    repairRequestDTO.setId_technician(null);
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
    public MessageDTO deleteRequest(DeleteRequest deleteRequest) {
        MessageDTO messageDTO = new MessageDTO();
        for (Long id_request : deleteRequest.getId()) {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(id_request).get();
            repairRequestRepository.delete(repairRequestEntity);
        }
        messageDTO.setMessage("Success");
        messageDTO.setHttpStatus(HttpStatus.OK);
        return messageDTO;
    }

    @Override
    public Page<RepairRequestDTO> getByStatusAndTechnician(Integer pageNo, String id_user, String status_code) {
        //setup pageable để tryền vào repo để phân trang
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus(status_code);
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findByStatusEntityAndTechnicianEntity(statusEntity, technicianEntity, pageable);
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
                    roleDTO.setRole_name(roleEntity.getRole_name());
                    customerDTO.getRoleDTOS().add(roleDTO);
                }

                //set customer cho repair request
                repairRequestDTO.setCustomer(customerDTO);

                //Lấy ra thợ đã nhận đơn hàng
                //bắt lỗi thợ chưa nhận yêu cầu
                try {
                    repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                    repairRequestDTO.setId_technician(technicianEntity.getId_user());
                } catch (NullPointerException ex) {
                    repairRequestDTO.setName_techinician(null);
                    repairRequestDTO.setId_technician(null);
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
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(acceptRequest.getId_request()).get();
            //Kiểm tra xem yêu cầu có bị hủy hay chưa và đã có thợ nào nhận yêu cầu hay chưa
            if(!repairRequestEntity.getStatusEntity().getNameStatus().equals("CANCEL")){
                if(repairRequestEntity.getTechnicianEntity() == null){
                    //Tìm kiếm thợ thông qua id
                    TechnicianEntity technicianEntity = technicianRepository.findById(acceptRequest.getId_technician()).get();
                    repairRequestEntity.setTechnicianEntity(technicianEntity);

                    //Tìm kiếm trạng thái nhận và cập nhật lại trạng thái cho yêu cầu
                    StatusEntity statusEntity = statusRepository.findByNameStatus("RECEIVED");
                    repairRequestEntity.setStatusEntity(statusEntity);

                    //Cập nhật lại yêu cầu
                    repairRequestEntity.setUpdated_at(LocalDateTime.now());
                    repairRequestRepository.save(repairRequestEntity);
                    messageDTO.setMessage("Success");
                    messageDTO.setHttpStatus(HttpStatus.OK);
                }else {
                    messageDTO.setMessage("Request has been received by technician");
                    messageDTO.setHttpStatus(HttpStatus.OK);
                }
            }else {
                messageDTO.setMessage("Request canceled");
                messageDTO.setHttpStatus(HttpStatus.OK);
            }
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found request");
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
                roleDTO.setRole_name(roleEntity.getRole_name());
                customerDTO.getRoleDTOS().add(roleDTO);
            }

            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
            try {
                repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                repairRequestDTO.setId_technician(technicianEntity.getId_user());
            } catch (NullPointerException ex) {
                repairRequestDTO.setName_techinician(null);
                repairRequestDTO.setId_technician(null);
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
                roleDTO.setRole_name(roleEntity.getRole_name());
                customerDTO.getRoleDTOS().add(roleDTO);
            }

            //set customer cho repair request
            repairRequestDTO.setCustomer(customerDTO);

            //Lấy ra thợ đã nhận đơn hàng
            //bắt lỗi thợ chưa nhận yêu cầu
            TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
            try {
                repairRequestDTO.setName_techinician(technicianEntity.getFull_name());
                repairRequestDTO.setId_technician(technicianEntity.getId_user());
            } catch (NullPointerException ex) {
                repairRequestDTO.setName_techinician(null);
                repairRequestDTO.setId_technician(null);
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
}

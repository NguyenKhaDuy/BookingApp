package com.example.bookingapp.Services.Impl;


import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.ConversationDTO;
import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.Request.ChatRequest;
import com.example.bookingapp.Models.Response.ChatResponse;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.ChatService;

import com.example.bookingapp.Utils.JwtTokenUtils;
import com.example.bookingapp.Utils.RandomIdUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    @Autowired
    ChatClient chatClient;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    ConversationRepository conversationRepository;
    @Autowired
    MessageRepository messageRepository;

    @Override
    public ChatResponse ask(ChatRequest chatRequest, String authorization) {
        String intent = classifyIntent(chatRequest.getMessage());
        ConversationEntity conversationEntity = null;
        String result;
        ChatResponse chatResponse = new ChatResponse();
        boolean isLoggedIn = false;
        if (authorization != null && !authorization.isBlank() && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (!token.isBlank() && token.split("\\.").length == 3) {
                try {
                    String email = jwtTokenUtils.getUsernameFromJWT(token);
                    UserEntity user = userRepository.findByEmail(email);
                    if (user != null) {
                        isLoggedIn = true;
                        if (chatRequest.getIdConversation() != null) {
                            conversationEntity =
                                    conversationRepository
                                            .findById(chatRequest.getIdConversation())
                                            .orElse(null);
                        }
                        if (conversationEntity == null) {
                            conversationEntity = new ConversationEntity();
                            conversationEntity.setIdConversation(
                                    RandomIdUtils.generateRandomId("CON", 15)
                            );
                            conversationEntity.setUserEntity(user);
                            conversationEntity.setCreatedAt(LocalDateTime.now());
                            conversationEntity.setUpdatedAt(LocalDateTime.now());
                            conversationEntity.setTitle(chatRequest.getMessage());
                            conversationRepository.save(conversationEntity);
                        }
                    }
                } catch (Exception e) {
                    isLoggedIn = false;
                    conversationEntity = null;
                }
            }
        }
        if (isLoggedIn && conversationEntity != null) {
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setIdMessage(RandomIdUtils.generateRandomId("CHAT", 15));
            messageEntity.setConversationEntity(conversationEntity);
            messageEntity.setSender("USER");
            messageEntity.setContent(chatRequest.getMessage());
            messageEntity.setCreatedAt(LocalDateTime.now());
            messageRepository.save(messageEntity);
        }
        switch (intent) {
            case "LIST_SERVICE":
                result = listService();
                break;
            case "SEARCH_SERVICE":
                result = searchService(chatRequest.getMessage());
                break;
            case "PRICE_SERVICE":
                result = priceServiceGuide(chatRequest.getMessage());
                break;
            case "SIMPLE_REPAIR":
                result = repairGuide(chatRequest.getMessage());
                break;
            case "ERROR_CODE":
                result = errorCodeGuide(chatRequest.getMessage());
                break;
            case "MAINTENANCE":
                result = maintenanceGuide(chatRequest.getMessage());
                break;
            case "CHECK_BOOKING":
                result = checkBookingGuide(
                        chatRequest.getMessage(),
                        authorization
                );
                break;
            case "CALL_TECHNICIAN":
                result = callTechnicianGuide(chatRequest.getMessage());
                break;
            case "SEARCH_WORKER":
                result = searchWorkerGuide(chatRequest.getMessage());
                break;
            case "CONSULT_DEVICE":
                result = consultDeviceGuide(chatRequest.getMessage());
                break;
            case "GREETING":
                result = greetingPrompt();
                break;
            default:
                result = unknownPrompt(chatRequest.getMessage());
                break;
        }
        if (isLoggedIn && conversationEntity != null) {
            MessageEntity messageAI = new MessageEntity();
            messageAI.setIdMessage(
                    RandomIdUtils.generateRandomId("MSG", 15)
            );
            messageAI.setConversationEntity(conversationEntity);
            messageAI.setSender("AI");
            messageAI.setContent(result);
            messageAI.setCreatedAt(LocalDateTime.now());
            messageRepository.save(messageAI);
        }
        chatResponse.setMessage(result);
        if (conversationEntity != null) {
            chatResponse.setIdConversation(conversationEntity.getIdConversation());
        } else {
            chatResponse.setIdConversation(null);
        }
        return chatResponse;
    }

    @Override
    public Object getConversationsByUser(String userId) {
        MessageResponse messageResponse = new MessageResponse();
        DataDTO dataDTO = new DataDTO();
        UserEntity userEntity = null;
        try {
            userEntity = userRepository.findById(userId).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Can not found user");
            messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        List<ConversationEntity> conversationEntities = conversationRepository.findByUserEntity(userEntity);
        List<ConversationDTO> conversationDTOS = conversationEntities.stream().map(conversationEntity -> {
            ConversationDTO conversationDTO = new ConversationDTO();
            conversationDTO.setIdConversation(conversationEntity.getIdConversation());
            conversationDTO.setTitle(conversationEntity.getTitle());
            conversationDTO.setCreatedAt(conversationEntity.getCreatedAt());
            conversationDTO.setUpdatedAt(conversationEntity.getUpdatedAt());

            List<MessageEntity> messageEntities = conversationEntity.getMessageEntities();
            List<MessageDTO> messageDTOS = messageEntities.stream().map(messageEntity -> {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setIdMessage(messageEntity.getIdMessage());
                messageDTO.setContent(messageEntity.getContent());
                messageDTO.setSender(messageEntity.getSender());
                messageDTO.setCreatedAt(messageEntity.getCreatedAt());
                return messageDTO;
            }).toList();
            conversationDTO.setMessageDTOS(messageDTOS);
            return conversationDTO;
        }).toList();

        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(conversationDTOS);
        return dataDTO;
    }

    @Override
    public MessageResponse deleteConversation(String conversationId) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            ConversationEntity conversationEntity = conversationRepository.findById(conversationId).get();
            conversationRepository.delete(conversationEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Can not found conversation");
            messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    private String classifyIntent(String question) {

        if (question == null || question.isBlank()) {
            return "OTHER";
        }

        String q = question.toLowerCase().trim();

        // Nhận diện mã đơn RExxxxxxxxxxxxxxxxxx
        if (question.matches("(?i).*RE[a-z0-9]{20}.*")) {
            return "CHECK_BOOKING";
        }

        //Mã lỗi
        if (q.matches(".*\\b(e\\d+|f\\d+|p\\d+|ie|oe|le|ue|de|h1)\\b.*")) {
            return "ERROR_CODE";
        }
        //Hướng dẫn sửa chữa
        if (contains(q,
                // Máy lạnh / điều hòa
                "không mát",
                "không lạnh",
                "máy lạnh không chạy",
                "điều hòa không chạy",
                "máy lạnh yếu",
                "lạnh yếu",
                "không ra hơi lạnh",
                "không thổi gió",
                "quạt không quay",
                "máy lạnh kêu lớn",
                "máy lạnh chảy nước",
                "điều hòa chảy nước",
                "đóng tuyết",
                "hết gas",
                "thiếu gas",

                // Máy giặt
                "không chạy",
                "không quay",
                "không cấp nước",
                "không xả",
                "không vắt",
                "giặt không sạch",
                "máy giặt rung",
                "máy giặt kêu to",
                "cửa máy giặt không mở",
                "máy giặt bị khóa",
                "máy giặt báo lỗi",
                "máy giặt bị hôi",

                // Tủ lạnh
                "không đông đá",
                "không lạnh ngăn dưới",
                "không lạnh ngăn đá",
                "tủ lạnh không chạy",
                "tủ lạnh kêu",
                "tủ lạnh chảy nước",
                "tủ lạnh nóng",
                "đóng tuyết tủ lạnh",

                // Bình nóng lạnh
                "không nóng",
                "nước không nóng",
                "bình nóng lạnh không nóng",
                "bình nóng lạnh bị rò nước",
                "bình nóng lạnh chập",

                // Tivi
                "không lên hình",
                "màn hình đen",
                "không có tiếng",
                "tivi không bật được",
                "tivi bị sọc",
                "mất hình",

                // Điện gia dụng
                "không lên nguồn",
                "không hoạt động",
                "không bật được",
                "bật không lên",
                "chạy nhưng không hoạt động",
                "hoạt động yếu",

                // Điện
                "cầu dao",
                "aptomat",
                "atomat",
                "cb",
                "nhảy điện",
                "nhảy cầu dao",
                "cầu dao bị nhảy",
                "mất điện",
                "không có điện",
                "điện bị ngắt",
                "không bật lên được",
                "bật không lên",
                "chập điện",

                // Âm thanh / dấu hiệu bất thường
                "chảy nước",
                "rò nước",
                "rò điện",
                "rung mạnh",
                "kêu to",
                "ồn",
                "nóng bất thường",
                "có mùi",
                "mùi khét",
                "phát tiếng kêu",
                "bốc khói"

        )) {

            return "SIMPLE_REPAIR";
        }

        //Kiểm tra đơn
        if (contains(q,
                "kiểm tra đơn",
                "kiểm yêu cầu",
                "đơn hàng",
                "yêu cầu",
                "lịch sửa",
                "trạng thái đơn",
                "trạng thái yêu cầu",
                "những đơn nào",
                "những yêu cầu nào",
                "có những đơn nào",
                "có những yêu cầu nào",
                "danh sách đơn",
                "danh sách yêu cầu",
                "các đơn của tôi",
                "các yêu cầu của tôi",
                "đơn của tôi",
                "yêu cầu của tôi")) {

            return "CHECK_BOOKING";
        }

        // Hỏi có cần gọi thợ hay không
        if (contains(q,
                "có nên gọi thợ không",
                "có cần gọi thợ không",
                "có cần thợ không",
                "có nên gọi kỹ thuật viên không",
                "có cần kỹ thuật viên không",
                "cần gọi thợ không",
                "nên gọi thợ không",
                "khi nào cần gọi thợ",
                "khi nào nên gọi thợ",
                "có phải gọi thợ không",
                "có cần gọi người sửa không")) {

            return "CALL_TECHNICIAN";
        }

        if (contains(q,
                "thợ",
                "kỹ thuật viên")) {
            return "SEARCH_WORKER";
        }

        // giá
        if (contains(q,
                "giá",
                "bao nhiêu",
                "chi phí",
                "bảng giá")) {
            return "PRICE_SERVICE";
        }

        if (contains(q,
                "hóa đơn",
                "invoice")) {
            return "INVOICE";
        }

        //tư vấn
        if (contains(q,
                "nên sửa",
                "nên mua mới")) {
            return "CONSULT_DEVICE";
        }

        //danh sách dịch vụ
        if (contains(q,
                "dịch vụ",
                "có sửa",
                "sửa được",
                "danh sách dịch vụ")) {
            return "LIST_SERVICE";
        }

        //tìm dịch vụ
        if (containsDevice(q)) {
            return "SEARCH_SERVICE";
        }

        //chào hỏi
        if (contains(q,
                "xin chào",
                "chào",
                "hello",
                "hi",
                "hey")) {
            return "GREETING";
        }

        // bảo dươn vệ sinh
        if (contains(q,
                "bảo dưỡng",
                "bảo trì",
                "vệ sinh",
                "làm sạch",
                "bao lâu vệ sinh",
                "bao lâu bảo dưỡng",
                "bao lâu bảo trì",
                "bao lâu nên vệ sinh",
                "bao lâu nên bảo dưỡng",
                "cách vệ sinh",
                "hướng dẫn vệ sinh",
                "vệ sinh máy lạnh",
                "vệ sinh điều hòa",
                "vệ sinh máy giặt",
                "vệ sinh tủ lạnh",
                "vệ sinh bình nóng lạnh",
                "bảo dưỡng máy lạnh",
                "bảo dưỡng điều hòa",
                "bảo dưỡng máy giặt",
                "bảo dưỡng tủ lạnh",
                "bảo dưỡng bình nóng lạnh",
                "bảo trì máy lạnh",
                "bảo trì máy giặt",
                "bảo trì tủ lạnh")) {

            return "MAINTENANCE";
        }

        return "OTHER";
    }

    private boolean contains(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDevice(String text) {
        return contains(text,
                "máy lạnh",
                "điều hòa",
                "máy giặt",
                "tủ lạnh",
                "máy nước nóng",
                "bình nóng lạnh",
                "quạt",
                "tivi",
                "máy bơm",
                "bếp từ",
                "bếp điện",
                "lò vi sóng",
                "máy lọc nước",
                "máy hút mùi");
    }

    private String listService() {
        List<ServiceEntity> services =
                serviceRepository.findAll();
        String context =
                services.stream()
                        .map(service ->
                                "- " + service.getNameService()
                        )
                        .reduce(
                                "",
                                (a, b) -> a + b + "\n"
                        );
        return chatClient.prompt("""              
                        Bạn là chatbot tư vấn dịch vụ.
                        Danh sách dịch vụ:
                        %s
                        Hãy trả lời khách hàng thân thiện.
                        """.formatted(context))
                .call()
                .content();

    }

    private String searchService(String question) {

        String keyword = extractKeyword(question);

        if (keyword.isBlank()) {
            return "Xin lỗi, tôi chưa xác định được thiết bị bạn cần sửa.";
        }

        ServiceEntity service = serviceRepository.findByKeyword(keyword);

        if (service == null) {

            List<ServiceEntity> all = serviceRepository.findAll();

            service = all.stream()
                    .filter(s -> keyword.toLowerCase()
                            .contains(s.getNameService().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }

        if (service == null) {
            return "Xin lỗi, tôi không tìm thấy dịch vụ phù hợp.";
        }

        String context = """
                Tên dịch vụ: %s
                Giá: %s
                Mô tả: %s
                """.formatted(
                service.getNameService()
        );

        return """
                Tên dịch vụ: %s
                """.formatted(
                service.getNameService()
        );
    }

    private String extractKeyword(String question) {

        List<ServiceEntity> services =
                serviceRepository.findAll();


        String serviceList = services.stream()
                .map(ServiceEntity::getNameService)
                .reduce("", (a,b)-> a + "- " + b + "\n");


        String result = chatClient.prompt()
                .user("""
                Bạn là hệ thống phân loại dịch vụ.

                Danh sách nhóm dịch vụ:
                %s

                Người dùng hỏi:
                "%s"

                Chỉ trả về đúng 1 tên dịch vụ
                trong danh sách trên.

                Không giải thích.
                Không markdown.

                """.formatted(
                        serviceList,
                        question
                ))
                .call()
                .content()
                .trim();


        return result;
    }

    private String prompt(String question) {

        return """
                Bạn là kỹ thuật viên sửa chữa điện lạnh và điện gia dụng.
                
                Nhiệm vụ:
                - Xác định thiết bị.
                - Phân tích nguyên nhân.
                - Chỉ hướng dẫn các bước an toàn.
                - Không hướng dẫn tháo thiết bị có điện áp cao.
                - Nếu có nguy cơ mất an toàn thì khuyên khách ngừng sử dụng và đặt lịch sửa.
                
                Trả lời theo mẫu:
                
                Thiết bị:
                ...
                
                Nguyên nhân có thể:
                - ...
                - ...
                
                Cách kiểm tra:
                1....
                2....
                3....
                
                Khi nào nên gọi thợ:
                ...
                
                Câu hỏi của khách:
                
                %s
                """.formatted(question);
    }

    private String repairGuide(String question) {

        try {

            return chatClient.prompt(prompt(question))
                    .call()
                    .content();

        } catch (Exception ex) {

            Throwable cause = ex.getCause();
            String message = cause != null ? cause.getMessage() : ex.getMessage();

            if (message != null && message.contains("429")) {
                return "AI hiện đã vượt giới hạn sử dụng. Vui lòng thử lại sau khoảng 1 phút.";
            }

            return "AI đang tạm thời không phản hồi. Vui lòng thử lại sau.";
        }
    }

    private String errorCodeGuide(String question) {

        if (question == null || question.trim().isEmpty()) {
            return """
                    Tôi có thể giải thích mã lỗi của thiết bị.
                    
                    Ví dụ:
                    - Điều hòa: E1, F0, P6...
                    - Máy giặt: IE, OE, LE...
                    - Tủ lạnh: H1...
                    
                    Hãy gửi mã lỗi của thiết bị để tôi giải thích.
                    """;
        }

        String prompt = """
                Bạn là kỹ thuật viên sửa chữa thiết bị.
                
                Người dùng đang hỏi:
                "%s"
                
                Hãy giải thích mã lỗi theo cấu trúc:
                
                1. Tên lỗi / ý nghĩa mã lỗi
                2. Nguyên nhân thường gặp
                3. Cách kiểm tra và xử lý đơn giản
                4. Khi nào cần gọi kỹ thuật viên
                
                Trả lời bằng tiếng Việt, ngắn gọn, dễ hiểu.
                """.formatted(question);


        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String maintenanceGuide(String question) {

        if (question == null || question.trim().isEmpty()) {
            return """
                    Tôi có thể hướng dẫn vệ sinh và bảo dưỡng thiết bị.
                    
                    Ví dụ:
                    - Vệ sinh máy lạnh
                    - Vệ sinh máy giặt
                    - Vệ sinh tủ lạnh
                    - Bảo dưỡng bình nóng lạnh
                    
                    Hãy cho tôi biết thiết bị cần bảo dưỡng.
                    """;
        }

        String prompt = """
                Bạn là kỹ thuật viên bảo trì thiết bị điện lạnh.
                
                Người dùng đang hỏi:
                "%s"
                
                Hãy hướng dẫn bảo dưỡng thiết bị theo cấu trúc:
                
                1. Thiết bị cần bảo dưỡng
                2. Tần suất vệ sinh / bảo dưỡng phù hợp
                3. Các bước vệ sinh và kiểm tra cơ bản
                4. Những lưu ý khi tự bảo dưỡng
                5. Khi nào cần gọi kỹ thuật viên
                
                Trả lời bằng tiếng Việt, ngắn gọn, dễ hiểu.
                """.formatted(question);


        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String searchWorkerGuide(String question) {

        if (question == null || question.trim().isEmpty()) {
            return """
                    Tôi có thể tìm thợ phù hợp.
                    Vui lòng cho tôi biết:
                    - Dịch vụ cần sửa chữa
                    - Khu vực cần tìm thợ (nếu có)
                    Ví dụ:
                    - Cần thợ sửa điện
                    - Tìm thợ sửa máy lạnh ở Ninh Kiều, Cần Thơ
                    """;
        }
        String prompt = """
                Bạn là hệ thống phân tích yêu cầu tìm thợ sửa chữa.
                Chỉ được trả về JSON thuần.
                Không dùng markdown.
                Không giải thích thêm.
                Format bắt buộc:
                {
                  "serviceName": "",
                  "district": "",
                  "ward": "",
                  "conscious": ""
                }
                Người dùng:
                "%s"
                Quy tắc:
                - Luôn cố gắng lấy serviceName.
                - Các cụm:
                  + sửa điện
                  + sửa máy lạnh
                  + sửa máy giặt
                  + sửa tủ lạnh
                  + vệ sinh máy lạnh
                  là tên dịch vụ.
                - Nếu không có dịch vụ thì serviceName = null.
                - Nếu người dùng không nói vị trí:
                  district = null
                  ward = null
                  conscious = null
                Quy đổi địa chỉ:
                - Ninh Kiều là district.
                - Cần Thơ là conscious.
                - An Bình, Cái Khế là ward.
                Ví dụ:
                Input:
                "tôi cần tìm thợ sửa điện"
                Output:
                {
                  "serviceName":"sửa điện",
                  "district":null,
                  "ward":null,
                  "conscious":null
                }
                Input:
                "tìm thợ sửa điện ở Ninh Kiều Cần Thơ"
                Output:
                {
                  "serviceName":"sửa điện",
                  "district":"Ninh Kiều",
                  "ward":null,
                  "conscious":"Cần Thơ"
                }
                """.formatted(question);
        String aiResult = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
//        System.out.println("AI RESULT = " + aiResult);
        try {
            // Xử lý trường hợp AI trả ```json
            aiResult = aiResult
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(aiResult);
            String serviceName =
                    json.has("serviceName")
                            ? json.get("serviceName").asText(null)
                            : null;
            String district =
                    json.has("district")
                            ? json.get("district").asText(null)
                            : null;
            String ward =
                    json.has("ward")
                            ? json.get("ward").asText(null)
                            : null;
            String conscious =
                    json.has("conscious")
                            ? json.get("conscious").asText(null)
                            : null;
            // Nếu AI không lấy được service thì lấy từ câu hỏi
            if (serviceName == null || serviceName.trim().isEmpty()) {
                serviceName = question
                        .replace("tôi cần tìm thợ", "")
                        .replace("tìm thợ", "")
                        .replace("cần tìm thợ", "")
                        .trim();
            }
            if (serviceName.isEmpty()) {
                return """
                        Vui lòng cho biết dịch vụ cần tìm thợ.
                        
                        Ví dụ:
                        - Tìm thợ sửa điện
                        - Tìm thợ sửa máy lạnh
                        """;
            }

            // Normalize vị trí
            if (district != null) {
                district = district
                        .replace("quận", "")
                        .replace("huyện", "")
                        .trim();
            }
            if (ward != null) {
                ward = ward
                        .replace("phường", "")
                        .replace("xã", "")
                        .trim();
            }
            if (conscious != null) {
                conscious = conscious
                        .replace("tỉnh", "")
                        .replace("thành phố", "")
                        .trim();
            }
            // Tìm dịch vụ
            ServiceEntity serviceEntity =
                    serviceRepository.findByKeyword(serviceName);
            if (serviceEntity == null) {
                return "Không tìm thấy dịch vụ: " + serviceName;
            }
            List<TechnicianEntity> technicians;
            // Không có vị trí
            if (district == null
                    && ward == null
                    && conscious == null) {
                technicians =
                        technicianRepository
                                .findByServiceEntities(serviceEntity);

            } else {
                technicians =
                        technicianRepository
                                .searchTechnicianByLocation(
                                        serviceEntity,
                                        district,
                                        ward,
                                        conscious
                                );
            }
            if (technicians.isEmpty()) {
                return """
                        Hiện tại chưa tìm thấy thợ phù hợp.
                        Bạn có thể:
                        - Thử khu vực khác
                        - Kiểm tra lại tên dịch vụ
                        """;
            }
            StringBuilder result = new StringBuilder();
            result.append("Tìm thấy ")
                    .append(technicians.size())
                    .append(" thợ phù hợp:\n\n");
            for (TechnicianEntity technician : technicians) {
                result.append(technician.getFull_name())
                        .append("\n");
                result.append("Dịch vụ: ");
                for (ServiceEntity service :
                        technician.getServiceEntities()) {
                    result.append(service.getNameService())
                            .append(", ");
                }
                result.append("\nKhu vực hoạt động:\n");
                for (LocationEntity location :
                        technician.getLocationEntities()) {
                    result.append("- ")
                            .append(location.getWard())
                            .append(", ")
                            .append(location.getDistrict())
                            .append(", ")
                            .append(location.getConscious())
                            .append("\n");
                }
                result.append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return """
                    Tôi chưa xác định được thông tin tìm thợ.
                    Vui lòng nhập:
                    - Dịch vụ cần sửa
                    - Khu vực cần tìm thợ (nếu có)
                    """;
        }
    }

    private String consultDeviceGuide(String question) {

        if (question == null || question.trim().isEmpty()) {
            return """
                    Tôi có thể tư vấn:
                    
                    - Nên sửa hay thay mới
                    - Thiết bị nào phù hợp
                    - Chi phí dự kiến
                    - Cách sử dụng tiết kiệm điện
                    
                    Ví dụ:
                    - Máy lạnh 10 năm có nên sửa không?
                    - Nên mua máy giặt cửa ngang hay cửa trên?
                    - Tủ lạnh inverter có tiết kiệm điện không?
                    - Điều hòa bao nhiêu HP phù hợp phòng 20m²?
                    """;
        }

        String prompt = """
                Bạn là chuyên gia tư vấn thiết bị điện gia dụng.
                
                Người dùng hỏi:
                "%s"
                
                Hãy trả lời theo các nguyên tắc:
                
                1. Phân tích đúng nhu cầu của người dùng.
                2. Nếu người dùng hỏi có nên sửa hay thay mới:
                   - So sánh ưu và nhược điểm.
                   - Đưa ra khuyến nghị hợp lý.
                3. Nếu người dùng hỏi chọn thiết bị:
                   - Đề xuất loại thiết bị phù hợp.
                   - Giải thích ngắn gọn lý do.
                4. Nếu người dùng hỏi về chi phí:
                   - Đưa ra mức chi phí dự kiến (ước lượng).
                5. Nếu người dùng hỏi cách sử dụng:
                   - Đưa ra các mẹo sử dụng an toàn và tiết kiệm điện.
                
                Trả lời bằng tiếng Việt.
                Ngắn gọn, rõ ràng, dễ hiểu và thân thiện.
                """.formatted(question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String checkBookingGuide(String question, String authorization) {

        // Kiểm tra đăng nhập
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "Bạn cần đăng nhập để kiểm tra đơn.";
        }

        try {

            String token = authorization.substring(7);

            String email = jwtTokenUtils.getUsernameFromJWT(token);

            UserEntity user = userRepository.findByEmail(email);

            if (user == null || !jwtTokenUtils.validateToken(token, user)) {
                return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
            }

            CustomerEntity customer = customerRepository.findByEmail(email);

            if (customer == null) {
                return "Không tìm thấy thông tin khách hàng.";
            }

            String lowerQuestion = question.toLowerCase().trim();

            if (lowerQuestion.contains("những đơn nào")
                    || lowerQuestion.contains("có những đơn nào")
                    || lowerQuestion.contains("danh sách đơn")
                    || lowerQuestion.contains("các đơn của tôi")
                    || lowerQuestion.contains("đơn nào của tôi")
                    || lowerQuestion.contains("tất cả đơn")
                    || lowerQuestion.contains("đơn của tôi")
                    || lowerQuestion.contains("những yêu cầu nào")
                    || lowerQuestion.contains("có những yêu cầu nào")
                    || lowerQuestion.contains("danh sách yêu cầu")
                    || lowerQuestion.contains("các yêu cầu của tôi")
                    || lowerQuestion.contains("yêu cầu nào của tôi")
                    || lowerQuestion.contains("tất cả yêu cầu")
                    || lowerQuestion.contains("yêu cầu của tôi")) {

                return customerBookingList(customer);
            }
            Pattern pattern = Pattern.compile(
                    "RE[a-zA-Z0-9]{20}"
            );

            Matcher matcher = pattern.matcher(question);

            String bookingCode = null;

            if (matcher.find()) {
                bookingCode = matcher.group();
            }

            // Không có mã đơn
            if (bookingCode == null) {
                return """
                    Vui lòng cung cấp mã đơn sửa chữa để xem chi tiết.
                    
                    Ví dụ:
                    REa82kLm91Xq04zP7nB3
                    
                    Hoặc bạn có thể hỏi:
                    "Tôi có những đơn nào?"
                    để xem danh sách tất cả đơn của bạn.
                    """;
            }

            System.out.println("BOOKING CODE = " + bookingCode);

            RepairRequestEntity repairRequest =
                    repairRequestRepository
                            .findById(bookingCode)
                            .orElse(null);

            if (repairRequest == null) {
                return "Không tìm thấy đơn có mã: " + bookingCode;
            }
            if (!repairRequest.getCustomerEntity()
                    .getId_user()
                    .equals(customer.getId_user())) {

                return "Bạn không có quyền xem đơn này.";
            }
            InvoicesEntity invoice =
                    repairRequest.getInvoicesEntity();
            return repairRequestInfo(
                    repairRequest,
                    invoice
            );

        } catch (Exception e) {
            e.printStackTrace();
            return """
                Không thể đọc thông tin đơn.
                
                Bạn có thể:
                - Hỏi "Tôi có những đơn nào?"
                - Hoặc cung cấp mã đơn để xem chi tiết.
                """;
        }
    }

    private String repairRequestInfo(RepairRequestEntity repairRequest,
                                     InvoicesEntity invoice) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        NumberFormat currencyFormatter =
                NumberFormat.getInstance(new Locale("vi", "VN"));
        StringBuilder result = new StringBuilder();
        result.append("Thông tin đơn sửa chữa\n\n");
        result.append("Mã đơn: ")
                .append(repairRequest.getId_request())
                .append("\n");
        if (repairRequest.getServiceEntity() != null) {
            result.append("Dịch vụ: ")
                    .append(repairRequest.getServiceEntity().getNameService())
                    .append("\n");
        }
        result.append("Mô tả: ")
                .append(repairRequest.getDescription())
                .append("\n");
        result.append("Ngày đặt lịch: ")
                .append(repairRequest.getCreated_at().format(formatter))
                .append("\n");
        if (repairRequest.getTechnicianEntity() != null) {
            result.append("Kỹ thuật viên: ")
                    .append(repairRequest.getTechnicianEntity().getFull_name())
                    .append("\n");
        }
        result.append("Trạng thái: ")
                .append(repairRequest.getStatusEntity().getNameStatus())
                .append("\n");
        // Có hóa đơn thì hiện thêm
        if (invoice != null) {
            result.append("\n-----------------\n");
            result.append("Thông tin hóa đơn\n\n");
            result.append("Mã hóa đơn: ")
                    .append(invoice.getId_invoices())
                    .append("\n");
            result.append("Ngày tạo hóa đơn: ")
                    .append(invoice.getCreated_at().format(formatter))
                    .append("\n");
            result.append("Tổng tiền: ")
                    .append(currencyFormatter.format(invoice.getTotal_amount()))
                    .append(" VNĐ")
                    .append("\n");
            if (invoice.getStatusEntity() != null) {
                result.append("Trạng thái: ")
                        .append(invoice.getStatusEntity().getNameStatus())
                        .append("\n");
            }
        } else {
            result.append("\nĐơn này chưa có hóa đơn.\n");
        }

        return result.toString();
    }

    private String customerBookingList(CustomerEntity customer) {
        List<RepairRequestEntity> requests =
                repairRequestRepository.findByCustomerEntity(customer);
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        if (requests.isEmpty()) {
            return "Bạn chưa có đơn sửa chữa nào.";
        }
        StringBuilder result = new StringBuilder();
        result.append("Danh sách đơn sửa chữa của bạn\n\n");
        for (RepairRequestEntity request : requests) {
            result.append("Mã đơn: ")
                    .append(request.getId_request())
                    .append("\n");
            if (request.getServiceEntity() != null) {
                result.append("Dịch vụ: ")
                        .append(request.getServiceEntity().getNameService())
                        .append("\n");
            }
            result.append("Ngày đặt: ")
                    .append(request.getCreated_at().format(formatter))
                    .append("\n");
            // trạng thái đơn
            if (request.getStatusEntity() != null) {
                result.append("Trạng thái: ")
                        .append(request.getStatusEntity().getNameStatus())
                        .append("\n");
            } else {
                result.append("Trạng thái: Chưa cập nhật\n");
            }
            if (request.getTechnicianEntity() != null) {
                result.append("Kỹ thuật viên: ")
                        .append(request.getTechnicianEntity().getFull_name())
                        .append("\n");
            }
            result.append("-----------------\n");
        }
        return result.toString();
    }

    private String greetingPrompt() {
        return chatClient.prompt()
                .user("""
                        Bạn là trợ lý AI của KingTech.
                        
                        Khi người dùng chào hỏi (Xin chào, Hello, Hi, Chào bạn...):
                        
                        - Chào lại một cách thân thiện.
                        - Giới thiệu ngắn gọn bạn là trợ lý AI của KingTech.
                        - Nếu người dùng hỏi:
                          + "Bạn là ai?"
                          + "Ai tạo ra bạn?"
                          + "Bạn tên gì?"
                          + "Bạn của công ty nào?"
                          thì hãy trả lời:
                        
                          "Tôi là trợ lý AI của KingTech, được phát triển để hỗ trợ khách hàng tư vấn và đặt dịch vụ sửa chữa điện lạnh, điện gia dụng."
                        
                        Sau đó giới thiệu các khả năng hỗ trợ như:
                        - Tìm dịch vụ sửa chữa
                        - Báo giá
                        - Hướng dẫn sửa lỗi đơn giản
                        - Giải thích mã lỗi
                        - Bảo dưỡng thiết bị
                        - Đặt, đổi, hủy lịch sửa
                        - Kiểm tra đơn hàng
                        - Kiểm tra bảo hành
                        - Tìm thợ gần nhất
                        - Tư vấn nên sửa hay thay mới
                        - Hỗ trợ thanh toán
                        
                        Trả lời tự nhiên, ngắn gọn, không dùng markdown.
                        """)
                .call()
                .content();
    }

    private String unknownPrompt(String question) {
        return chatClient.prompt()
                .user("""
                        Bạn là trợ lý AI của KingTech.
                        
                        Người dùng vừa hỏi:
                        "%s"
                        
                        Bạn không xác định được chính xác yêu cầu.
                        
                        Hãy:
                        - Xin lỗi vì chưa hiểu ý.
                        - Đề nghị người dùng mô tả rõ hơn.
                        - Đưa ra một vài ví dụ phù hợp như:
                          • Điều hòa không mát
                          • Máy giặt báo lỗi IE
                          • Giá sửa tủ lạnh
                          • Đặt lịch sửa máy lạnh
                          • Hủy đơn
                          • Đổi lịch sửa
                          • Kiểm tra đơn hàng
                          • Có bảo hành không
                          • Có phục vụ tại khu vực của tôi không
                          • Nên sửa hay mua mới
                        
                        Nếu người dùng hỏi bạn là ai thì trả lời:
                        "Tôi là trợ lý AI của KingTech, hỗ trợ tư vấn và tiếp nhận yêu cầu sửa chữa thiết bị điện lạnh, điện gia dụng."
                        
                        Trả lời tự nhiên, ngắn gọn, không dùng markdown.
                        """.formatted(question))
                .call()
                .content();
    }

    private String priceServiceGuide(String question) {

        String keyword = extractKeyword(question);

        if (keyword.isBlank()) {
            return """
                Vui lòng cho biết thiết bị cần báo giá.
                
                Ví dụ:
                - Giá bơm gas máy lạnh
                - Chi phí sửa máy giặt
                - Giá vệ sinh điều hòa
                """;
        }


        ServiceEntity service =
                serviceRepository.findByKeyword(keyword);


        if (service == null) {
            return "Tôi chưa tìm thấy dịch vụ phù hợp.";
        }


        String prompt = """
            Bạn là nhân viên tư vấn dịch vụ sửa chữa KingTech.
            
            Khách hàng hỏi:
            "%s"
            
            Dịch vụ:
            %s
            
            Hãy tư vấn:
            - Chi phí tham khảo.
            - Các trường hợp có thể phát sinh.
            - Khuyến nghị đặt lịch kiểm tra nếu cần.
            
            Trả lời ngắn gọn bằng tiếng Việt.
            """.formatted(
                question,
                service.getNameService()
        );


        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String callTechnicianGuide(String question) {

        String prompt = """
            Bạn là trợ lý AI của KingTech chuyên hỗ trợ khách hàng sửa chữa
            điện lạnh và điện gia dụng.

            Người dùng hỏi:
            "%s"

            Hãy trả lời xem trong trường hợp này có nên gọi kỹ thuật viên hay không.

            Trả lời theo cấu trúc:

            Có nên gọi thợ không?
            - Có / Không / Nên gọi nếu...

            Vì sao?
            - Nêu ngắn gọn nguyên nhân.

            Trước khi gọi thợ có thể kiểm tra:
            1. ...
            2. ...
            3. ...

            Khi nào cần gọi thợ ngay:
            - ...
            - ...

            Lưu ý:
            - Không hướng dẫn người dùng tháo các bộ phận điện áp cao.
            - Không hướng dẫn sửa máy nén, bo mạch hoặc hệ thống gas.
            - Nếu có mùi khét, khói, chập điện hoặc nguy cơ cháy nổ thì
              khuyên người dùng ngắt nguồn điện và gọi kỹ thuật viên.

            Trả lời bằng tiếng Việt, rõ ràng, dễ hiểu.
            """.formatted(question);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception ex) {
            return """
                Nếu thiết bị đã được kiểm tra các bước cơ bản nhưng vẫn không hoạt động,
                bạn nên gọi kỹ thuật viên KingTech để kiểm tra.

                Nếu có mùi khét, khói, chập điện hoặc dấu hiệu nguy hiểm,
                hãy ngắt nguồn điện và gọi kỹ thuật viên ngay.
                """;
        }
    }
}
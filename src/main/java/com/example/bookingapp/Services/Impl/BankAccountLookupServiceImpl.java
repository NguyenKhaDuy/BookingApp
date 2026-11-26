
package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Services.BankAccountLookupService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class BankAccountLookupServiceImpl
        implements BankAccountLookupService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${bankgateway.url:https://api.bankgateway.pro/api/v1/lookup}")
    private String lookupUrl;

    @Value("${bankgateway.api-key:}")
    private String apiKey;

    public BankAccountLookupServiceImpl(
            RestClient.Builder builder,
            ObjectMapper objectMapper) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = builder
                .requestFactory(factory)
                .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public String lookupAccount(
            String bankCode,
            String accountNumber) {

        if (bankCode == null ||
                !bankCode.trim().matches("[a-zA-Z0-9_-]{2,30}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã ngân hàng không hợp lệ"
            );
        }

        if (accountNumber == null ||
                !accountNumber.replaceAll("\\s+", "")
                        .matches("\\d{4,25}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tài khoản không hợp lệ"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình BankGateway API Key"
            );
        }

        String normalizedBankCode =
                bankCode.trim().toUpperCase(Locale.ROOT);

        String normalizedAccount =
                accountNumber.replaceAll("\\s+", "");

        Map<String, String> body = Map.of(
                "bankCode", normalizedBankCode,
                "accountNumber", normalizedAccount
        );

        String idempotencyKey = UUID.randomUUID().toString();

        try {
            System.out.println(
                    "[BANKGATEWAY] Starting lookup"
            );

            JsonNode response = restClient.post()
                    .uri(lookupUrl)
                    .header("X-API-Key", apiKey)
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null ||
                    !response.path("success").asBoolean(false)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "BankGateway không xác nhận tra cứu thành công"
                );
            }

            JsonNode data = response.path("data");

            String name = data.path("accountName")
                    .asText("")
                    .trim();



            if (name.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "BankGateway không trả về tên chủ tài khoản"
                );
            }

            // Kiểm tra phản hồi có khớp tài khoản yêu cầu.
            String returnedBank =
                    data.path("bankCode").asText("");

            String returnedAccount =
                    data.path("accountNumber").asText("");

            if (!normalizedBankCode.equalsIgnoreCase(returnedBank)
                    || !normalizedAccount.equals(returnedAccount)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Thông tin phản hồi BankGateway không khớp yêu cầu"
                );
            }

            System.out.println(
                    "[BANKGATEWAY] Lookup successful"
            );

            return name;

        } catch (RestClientResponseException e) {

            int status = e.getStatusCode().value();

            System.err.println(
                    "[BANKGATEWAY] HTTP STATUS: " + status
            );

            String errorCode = "";

            try {
                JsonNode error = objectMapper.readTree(
                        e.getResponseBodyAsString()
                );

                errorCode = error.path("error")
                        .path("code")
                        .asText("");

                if (errorCode.isBlank()) {
                    errorCode = error.path("code").asText("");
                }

            } catch (Exception ignored) {
                // Không log dữ liệu tài khoản.
            }

            System.err.println(
                    "[BANKGATEWAY] ERROR CODE: " + errorCode
            );

            if (status == 404 &&
                    "NOT_FOUND".equalsIgnoreCase(errorCode)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản ngân hàng"
                );
            }

            if (status == 400 || status == 422) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Thông tin tra cứu ngân hàng không hợp lệ"
                );
            }

            if (status == 401 || status == 403) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "BankGateway từ chối API Key hoặc quyền truy cập"
                );
            }

            if (status == 402) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Tài khoản BankGateway không đủ hạn mức"
                );
            }

            if (status == 429) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Quá nhiều yêu cầu tra cứu"
                );
            }

            if (status == 502 || status == 503 || status == 504) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Hệ thống tra cứu ngân hàng tạm thời không khả dụng"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "BankGateway trả về HTTP " + status
            );

        } catch (ResourceAccessException e) {

            System.err.println(
                    "[BANKGATEWAY] CONNECTION ERROR: " +
                            e.getClass().getSimpleName()
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể kết nối đến BankGateway",
                    e
            );

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {

            System.err.println(
                    "[BANKGATEWAY] UNEXPECTED ERROR: " +
                            e.getClass().getSimpleName()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Lỗi xử lý phản hồi BankGateway",
                    e
            );
        }
    }
}

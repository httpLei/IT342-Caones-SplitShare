package edu.cit.caones.splitshare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;
    private String timestamp;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private String code;
        private String message;
        private Object details;
    }

    // Convenience factory methods

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> fail(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(Instant.now().toString())
                .build();
    }
}

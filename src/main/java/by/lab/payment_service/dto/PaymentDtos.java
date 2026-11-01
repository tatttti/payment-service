package by.lab.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PaymentDtos {

    public record PaymentRequest(
        @NotBlank String firmId,
        @NotBlank String serviceName,
        @NotBlank String methodName,
        @NotNull LocalDateTime dateFrom,
        @NotNull LocalDateTime dateTo
    ) {}

    public record PaymentResult(
        boolean success,
        String message,
        String token,
        Double price,
        LocalDateTime validFrom,
        LocalDateTime validTo
    ) {}

    public record TokenNotification(
        String token,
        String methodName,
        LocalDateTime validFrom,
        LocalDateTime validTo
    ) {}
}
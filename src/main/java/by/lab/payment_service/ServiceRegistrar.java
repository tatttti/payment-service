package by.lab.payment_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class ServiceRegistrar implements ApplicationRunner {

    private final RestTemplate http = new RestTemplate();

    @Value("${registry.url}")
    private String registryUrl;

    @Value("${service.base-url:https://payment-service-arvz.onrender.com}")
    private String myBaseUrl;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, Object> body = Map.of(
                "name", "PaymentService",
                "baseUrl", myBaseUrl,
                "description", "Сервис оплаты методов прикладных сервисов",
                "methods", List.of(
                    Map.of(
                        "name", "Pay",
                        "httpMethod", "POST",
                        "path", "/api/payment/pay",
                        "description", "Оплатить доступ к методу",
                        "paid", false,
                        "parameters", List.of()
                    )
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = http.postForEntity(
                registryUrl + "/api/services", entity, String.class);

            System.out.println(">>> [PAYMENT] Регистрация в Регистре: "
                + resp.getStatusCode() + " " + resp.getBody());
        } catch (Exception ex) {
            System.out.println(">>> [PAYMENT] Не удалось зарегистрироваться: " + ex.getMessage());
        }
    }
}
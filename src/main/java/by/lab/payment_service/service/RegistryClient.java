package by.lab.payment_service.service;

import by.lab.payment_service.dto.PaymentDtos.TokenNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RegistryClient {

    private final RestTemplate http = new RestTemplate();
    private final String registryUrl;

    public RegistryClient(@Value("${registry.url}") String registryUrl) {
        this.registryUrl = registryUrl;
    }

    public record CheckResult(boolean exists, String baseUrl, Double price) {}

    public record MethodInfo(String name, String httpMethod, String path,
                             String description, Double price) {}

    public record ServiceInfo(String name, String baseUrl, String description,
                              List<MethodInfo> methods) {}

    // ===== Проверка метода в Регистре =====
    public CheckResult checkMethod(String serviceName, String methodName) {
        try {
            String url = registryUrl + "/api/services/" + serviceName
                    + "/methods/" + methodName;
            ResponseEntity<String> resp = http.getForEntity(url, String.class);

            if (resp.getStatusCode() == HttpStatus.NOT_FOUND) return new CheckResult(false, null, null);
            if (!resp.getStatusCode().is2xxSuccessful()) return new CheckResult(false, null, null);

            Double price = extractPrice(resp.getBody());

            String svcUrl = registryUrl + "/api/services/" + serviceName;
            ResponseEntity<String> svcResp = http.getForEntity(svcUrl, String.class);
            if (!svcResp.getStatusCode().is2xxSuccessful()) return new CheckResult(false, null, price);

            String baseUrl = extractStringField(svcResp.getBody(), "baseUrl");
            return new CheckResult(true, baseUrl, price);
        } catch (Exception ex) {
            System.out.println(">>> [PAYMENT] Ошибка проверки в Регистре: " + ex.getMessage());
            return new CheckResult(false, null, null);
        }
    }

    // ===== Список сервисов из Регистра (для оснастки) =====
    @SuppressWarnings("unchecked")
    public List<ServiceInfo> listServices() {
        try {
            String url = registryUrl + "/api/services";
            ResponseEntity<List> resp = http.getForEntity(url, List.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return List.of();
            }

            List<ServiceInfo> result = new ArrayList<>();
            for (Object item : resp.getBody()) {
                Map<String, Object> svc = (Map<String, Object>) item;
                String name = String.valueOf(svc.getOrDefault("name", ""));
                String baseUrl = String.valueOf(svc.getOrDefault("baseUrl", ""));
                String description = svc.get("description") == null
                        ? "" : String.valueOf(svc.get("description"));

                List<MethodInfo> methods = new ArrayList<>();
                Object methodsObj = svc.get("methods");
                if (methodsObj instanceof List<?> methodList) {
                    for (Object mo : methodList) {
                        Map<String, Object> m = (Map<String, Object>) mo;
                        Double price = null;
                        Object p = m.get("price");
                        if (p instanceof Number num) price = num.doubleValue();

                        methods.add(new MethodInfo(
                            String.valueOf(m.getOrDefault("name", "")),
                            String.valueOf(m.getOrDefault("httpMethod", "GET")),
                            String.valueOf(m.getOrDefault("path", "/")),
                            m.get("description") == null ? "" : String.valueOf(m.get("description")),
                            price
                        ));
                    }
                }
                result.add(new ServiceInfo(name, baseUrl, description, methods));
            }
            return result;
        } catch (Exception ex) {
            System.out.println(">>> [PAYMENT] Ошибка загрузки списка из Регистра: " + ex.getMessage());
            return List.of();
        }
    }

    // ===== Отправка токена в прикладной сервис =====
    public boolean sendToken(String baseUrl, TokenNotification notif) {
        try {
            String url = baseUrl.replaceAll("/$", "") + "/internal/payment-token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TokenNotification> entity = new HttpEntity<>(notif, headers);

            ResponseEntity<String> resp = http.postForEntity(url, entity, String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            System.out.println(">>> [PAYMENT] Ошибка отправки токена: " + ex.getMessage());
            return false;
        }
    }

    // ===== Утилиты разбора JSON =====
    private Double extractPrice(String json) {
        if (json == null) return null;
        int i = json.indexOf("\"price\"");
        if (i < 0) return null;
        int colon = json.indexOf(':', i);
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() &&
               (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' ||
                json.charAt(end) == '-')) end++;
        try { return Double.parseDouble(json.substring(start, end)); }
        catch (Exception e) { return null; }
    }

    private String extractStringField(String json, String field) {
        if (json == null) return null;
        int i = json.indexOf("\"" + field + "\"");
        if (i < 0) return null;
        int colon = json.indexOf(':', i);
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }
}
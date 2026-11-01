package by.lab.payment_service.service;

import by.lab.payment_service.dto.PaymentDtos.*;
import by.lab.payment_service.model.Payment;
import by.lab.payment_service.repo.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final RegistryClient registry;

    public PaymentService(PaymentRepository repo, RegistryClient registry) {
        this.repo = repo;
        this.registry = registry;
    }

    @Transactional
    public PaymentResult pay(PaymentRequest req) {
        if (req.dateFrom().isAfter(req.dateTo())) {
            return new PaymentResult(false, "Некорректный интервал", null, null, null, null);
        }
        if (req.dateTo().isBefore(LocalDateTime.now())) {
            return new PaymentResult(false, "Интервал уже в прошлом", null, null, null, null);
        }

        var check = registry.checkMethod(req.serviceName(), req.methodName());
        if (!check.exists() || check.baseUrl() == null) {
            return new PaymentResult(false, "Метод не найден в Регистре", null, null, null, null);
        }
        System.out.println(">>> [PAYMENT] Метод найден: baseUrl=" + check.baseUrl()
                + ", price=" + check.price());

        String token = UUID.randomUUID().toString().replace("-", "");

        Payment p = new Payment();
        p.setToken(token);
        p.setFirmId(req.firmId());
        p.setServiceName(req.serviceName());
        p.setMethodName(req.methodName());
        p.setValidFrom(req.dateFrom());
        p.setValidTo(req.dateTo());
        p.setCreatedAt(LocalDateTime.now());
        p.setPrice(check.price());
        repo.save(p);

        boolean sent = registry.sendToken(check.baseUrl(),
            new TokenNotification(token, req.methodName(), req.dateFrom(), req.dateTo()));

        if (!sent) {
            return new PaymentResult(false,
                "Оплата принята, но сервис недоступен. Повторите позже.",
                token, check.price(), req.dateFrom(), req.dateTo());
        }

        return new PaymentResult(true, "Оплата принята",
            token, check.price(), req.dateFrom(), req.dateTo());
    }

    @Transactional(readOnly = true)
    public Payment find(String token) {
        return repo.findById(token).orElse(null);
    }

    public List<RegistryClient.ServiceInfo> listServices() {
        return registry.listServices();
    }
}
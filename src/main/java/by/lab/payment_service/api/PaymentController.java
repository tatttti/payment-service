package by.lab.payment_service.api;

import by.lab.payment_service.dto.PaymentDtos.*;
import by.lab.payment_service.model.Payment;
import by.lab.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/pay")
    public ResponseEntity<PaymentResult> pay(@Valid @RequestBody PaymentRequest req) {
        System.out.println(">>> [PAYMENT] Pay: firm=" + req.firmId()
                + ", service=" + req.serviceName()
                + ", method=" + req.methodName());
        PaymentResult result = service.pay(req);
        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @GetMapping("/check")
    public ResponseEntity<Payment> check(@RequestParam String token) {
        Payment p = service.find(token);
        return p == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(p);
    }

    /** Список сервисов и методов из Регистра (для оснастки). */
    @GetMapping("/services")
    public ResponseEntity<?> listServices() {
        return ResponseEntity.ok(service.listServices());
    }
    
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        return ResponseEntity.ok(Map.of(
            "service", "PaymentService",
            "version", "1.0",
            "description", "Сервис оплаты методов прикладных сервисов",
            "status", "running"
        ));
    }
}
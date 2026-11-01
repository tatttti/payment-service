package by.lab.payment_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(length = 64)
    private String token;

    @Column(name = "firm_id", nullable = false, length = 100)
    private String firmId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(precision = 12)
    private Double price;

    public Payment() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getFirmId() { return firmId; }
    public void setFirmId(String firmId) { this.firmId = firmId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
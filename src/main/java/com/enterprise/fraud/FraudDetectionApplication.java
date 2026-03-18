package com.enterprise.fraud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Fraud Detection System API",
        version = "1.0.0",
        description = "Enterprise-grade credit card fraud detection system with ML-powered analysis",
        contact = @Contact(name = "Fraud Detection Team", email = "fraud-detection@enterprise.com"),
        license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")
    )
)
public class FraudDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }
}

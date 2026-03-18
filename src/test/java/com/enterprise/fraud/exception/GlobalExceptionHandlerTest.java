package com.enterprise.fraud.exception;

import com.enterprise.fraud.dto.ApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.fraud.repository.TransactionRepository;
import com.enterprise.fraud.repository.FraudAlertRepository;
import com.enterprise.fraud.service.ScoringService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler ensuring proper error response formats.
 */
@WebMvcTest
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private FraudAlertRepository fraudAlertRepository;

    @MockBean
    private ScoringService scoringService;

    @Test
    @DisplayName("Should return 404 for non-existent transaction")
    void resourceNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @DisplayName("Should return 400 for invalid analyze request")
    void validationError_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

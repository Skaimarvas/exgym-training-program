package com.exgym.training.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.exgym.training.util.TransactionContext;

class LoggingInterceptorTest {

    private LoggingInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        TransactionContext.clear();
    }

    @Test
    void preHandle_generatesTransactionIdWhenMissing() {
        request.setMethod("GET");
        request.setRequestURI("/api/training/types");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNotNull(TransactionContext.getTransactionId());
        assertNotNull(response.getHeader("X-Transaction-Id"));
    }

    @Test
    void preHandle_usesProvidedTransactionId() {
        request.addHeader("X-Transaction-Id", "txn-abc-123");
        request.setMethod("POST");
        request.setRequestURI("/api/trainee/register");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("txn-abc-123", TransactionContext.getTransactionId());
        assertEquals("txn-abc-123", response.getHeader("X-Transaction-Id"));
    }

    @Test
    void afterCompletion_clearsTransactionContext() {
        request.setMethod("GET");
        request.setRequestURI("/api/user/login");
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(TransactionContext.getTransactionId());
    }

    @Test
    void afterCompletion_handlesExceptionPath() {
        request.setMethod("DELETE");
        request.setRequestURI("/api/trainee/profile");
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), new RuntimeException("boom"));

        assertNull(TransactionContext.getTransactionId());
    }
}

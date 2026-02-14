package com.exgym.training.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.exgym.training.util.TransactionContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or retrieve transaction ID
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }
        
        // Set transaction ID in context
        TransactionContext.setTransactionId(transactionId);
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        
        // Add to response header
        response.addHeader(TRANSACTION_ID_HEADER, transactionId);
        
        // Log request details
        logger.info("Incoming request - Method: {}, URI: {}, TransactionId: {}", 
                request.getMethod(), request.getRequestURI(), transactionId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Log response details
        logger.info("Response - Status: {}, TransactionId: {}", 
                response.getStatus(), TransactionContext.getTransactionId());
        
        // Clear context
        TransactionContext.clear();
        MDC.remove(TRANSACTION_ID_MDC_KEY);
    }
}

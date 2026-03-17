package com.exgym.training.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class TransactionContextTest {

    @BeforeEach
    void setUp() {
        // Clear context before each test
        TransactionContext.clear();
    }

    @Test
    void testSetAndGetTransactionId() {
        // Setup
        String transactionId = "txn-123-456-789";

        // Execute
        TransactionContext.setTransactionId(transactionId);
        String retrieved = TransactionContext.getTransactionId();

        // Verify
        assertEquals(transactionId, retrieved);
    }

    @Test
    void testGetTransactionIdReturnsNullWhenNotSet() {
        // Execute & Verify
        assertNull(TransactionContext.getTransactionId());
    }

    @Test
    void testClearTransactionId() {
        // Setup
        TransactionContext.setTransactionId("txn-123");

        // Execute
        TransactionContext.clear();

        // Verify
        assertNull(TransactionContext.getTransactionId());
    }

    @Test
    void testSetTransactionIdMultipleTimes() {
        // Setup & Execute
        TransactionContext.setTransactionId("txn-first");
        assertEquals("txn-first", TransactionContext.getTransactionId());

        TransactionContext.setTransactionId("txn-second");
        assertEquals("txn-second", TransactionContext.getTransactionId());
    }

    @Test
    void testTransactionContextIsolation() {
        // Setup - simulating different threads would require actual threading
        // For single thread, verify state transitions
        TransactionContext.setTransactionId("txn-1");
        assertEquals("txn-1", TransactionContext.getTransactionId());

        TransactionContext.clear();
        assertNull(TransactionContext.getTransactionId());

        TransactionContext.setTransactionId("txn-2");
        assertEquals("txn-2", TransactionContext.getTransactionId());
    }

    @Test
    void testTransactionIdWithSpecialCharacters() {
        // Setup
        String transactionId = "txn-2026-02-28T10:30:45.123Z";

        // Execute
        TransactionContext.setTransactionId(transactionId);

        // Verify
        assertEquals(transactionId, TransactionContext.getTransactionId());
    }

    @Test
    void testTransactionIdWithUUID() {
        // Setup
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // Execute
        TransactionContext.setTransactionId(uuid);

        // Verify
        assertEquals(uuid, TransactionContext.getTransactionId());
    }

    @Test
    void testTransactionContextCanBeInstantiated() {
        TransactionContext context = new TransactionContext();
        assertNotNull(context);
    }
}

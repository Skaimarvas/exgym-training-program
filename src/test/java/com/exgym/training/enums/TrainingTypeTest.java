package com.exgym.training.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TrainingTypeTest {

    @Test
    void testTrainingTypeEnumValuesExist() {
        // Verify primary enum constants are defined
        assertNotNull(TrainingType.CARDIO);
        assertNotNull(TrainingType.STRENGTH);
        assertNotNull(TrainingType.YOGA);
        assertNotNull(TrainingType.PILATES);
        assertNotNull(TrainingType.HIIT);
    }

    @Test
    void testTrainingTypeEnumNames() {
        // Verify enum names
        assertEquals("CARDIO", TrainingType.CARDIO.name());
        assertEquals("STRENGTH", TrainingType.STRENGTH.name());
        assertEquals("YOGA", TrainingType.YOGA.name());
        assertEquals("PILATES", TrainingType.PILATES.name());
        assertEquals("HIIT", TrainingType.HIIT.name());
    }

    @Test
    void testTrainingTypeOrdinals() {
        // Verify enum ordinals for known constants
        assertEquals(0, TrainingType.CARDIO.ordinal());
        assertEquals(1, TrainingType.STRENGTH.ordinal());
        // YOGA should be ordinal 12
        assertEquals(12, TrainingType.YOGA.ordinal());
    }

    @Test
    void testTrainingTypeValueOf() {
        // Verify valueOf method works
        assertEquals(TrainingType.CARDIO, TrainingType.valueOf("CARDIO"));
        assertEquals(TrainingType.STRENGTH, TrainingType.valueOf("STRENGTH"));
        assertEquals(TrainingType.YOGA, TrainingType.valueOf("YOGA"));
        assertEquals(TrainingType.PILATES, TrainingType.valueOf("PILATES"));
        assertEquals(TrainingType.HIIT, TrainingType.valueOf("HIIT"));
    }

    @Test
    void testTrainingTypeValuesCount() {
        // Verify values() returns all constants
        TrainingType[] values = TrainingType.values();
        assertEquals(15, values.length);
    }

    @Test
    void testTrainingTypeComparison() {
        // Verify enum comparisons work
        assertTrue(TrainingType.CARDIO.equals(TrainingType.CARDIO));
        assertFalse(TrainingType.CARDIO.equals(TrainingType.STRENGTH));
        assertNotEquals(TrainingType.YOGA, TrainingType.PILATES);
    }

    @Test
    void testTrainingTypeEnumIsComparable() {
        // Verify enums can be compared
        assertTrue(TrainingType.CARDIO.compareTo(TrainingType.CARDIO) == 0);
        assertTrue(TrainingType.CARDIO.compareTo(TrainingType.STRENGTH) < 0);
        assertTrue(TrainingType.STRENGTH.compareTo(TrainingType.CARDIO) > 0);
    }

    @Test
    void testTrainingTypeEnumInstanceof() {
        // Verify enum instances are of correct type
        assertInstanceOf(TrainingType.class, TrainingType.CARDIO);
        assertInstanceOf(TrainingType.class, TrainingType.STRENGTH);
        assertInstanceOf(Enum.class, TrainingType.CARDIO);
    }
}


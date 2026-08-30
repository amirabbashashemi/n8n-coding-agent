package com.example.calculator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringCalculatorTest {

    private final StringCalculator calculator = new StringCalculator(); // Assuming StringCalculator class exists

    @Test
    void testAdd_EmptyString() {
        assertEquals(0, calculator.add(""));
    }

    @Test
    void testAdd_SingleNumber() {
        assertEquals(5, calculator.add("5"));
    }

    @Test
    void testAdd_MultipleNumbers() {
        assertEquals(6, calculator.add("1,2,3"));
    }

    @Test
    void testAdd_NegativeNumbers() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("1,-2,3");
        });
        assertEquals("Negative numbers are not allowed", exception.getMessage());
    }

    @Test
    void testAdd_InvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("1,2,x");
        });
        assertEquals("Invalid input", exception.getMessage());
    }
}
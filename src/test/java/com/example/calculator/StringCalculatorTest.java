package com.example.calculator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringCalculatorTest {
    private final StringCalculator calculator = new StringCalculator();

    @Test
    void addEmptyStringReturnsZero() {
        assertEquals(0, calculator.add(""));
    }

    @Test
    void addSingleNumberReturnsNumber() {
        assertEquals(1, calculator.add("1"));
    }

    @Test
    void addTwoNumbersReturnsSum() {
        assertEquals(3, calculator.add("1,2"));
    }

    @Test
    void addMultipleNumbersReturnsSum() {
        assertEquals(6, calculator.add("1,2,3"));
    }

    @Test
    void addNumbersWithNewLinesReturnsSum() {
        assertEquals(6, calculator.add("1\n2,3"));
    }

    @Test
    void addNegativeNumberThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("1,-2,3");
        });
        assertEquals("negatives not allowed: [-2]", exception.getMessage());
    }
}
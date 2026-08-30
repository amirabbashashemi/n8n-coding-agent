import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

class StringCalculator {

    public int add(String numbers) {
        if (numbers.isEmpty()) return 0;
        String[] parsedNumbers = numbers.split(",");
        return sum(parsedNumbers);
    }

    private int sum(String[] numbers) {
        int total = 0;
        for (String number : numbers) {
            int value = Integer.parseInt(number);
            if (value < 0) throw new IllegalArgumentException("Negative numbers not allowed: " + number);
            total += value;
        }
        return total;
    }
}

class StringCalculatorTest {

    @Test
    void testAddEmptyString() {
        StringCalculator calculator = new StringCalculator();
        assertEquals(0, calculator.add(""));
    }

    @Test
    void testAddSingleNumber() {
        StringCalculator calculator = new StringCalculator();
        assertEquals(5, calculator.add("5"));
    }

    @Test
    void testAddMultipleNumbers() {
        StringCalculator calculator = new StringCalculator();
        assertEquals(15, calculator.add("5,10"));
    }

    @Test
    void testAddHandlesNegativeNumbers() {
        StringCalculator calculator = new StringCalculator();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("5,-2,3");
        });
        assertTrue(thrown.getMessage().contains("Negative numbers not allowed"));
    }
} 

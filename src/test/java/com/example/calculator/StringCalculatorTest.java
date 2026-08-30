import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StringCalculatorTest {

    @Test
    void add_EmptyString_ReturnsZero() {
        assertEquals(0, StringCalculator.add(""));
    }

    @Test
    void add_SingleNumber_ReturnsTheNumber() {
        assertEquals(5, StringCalculator.add("5"));
    }

    @Test
    void add_TwoNumbers_ReturnsTheirSum() {
        assertEquals(7, StringCalculator.add("3,4"));
    }

    @Test
    void add_MultipleNumbers_ReturnsTheirSum() {
        assertEquals(15, StringCalculator.add("1,2,3,4,5"));
    }

    @Test
    void add_NewLineAsDelimiter_ReturnsSum() {
        assertEquals(6, StringCalculator.add("1\n2,3"));
    }

    @Test
    void add_NegativeNumber_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StringCalculator.add("1,-2,3");
        });
        assertEquals("Negative numbers not allowed: -2", exception.getMessage());
    }

    @Test
    void add_MultipleNegativeNumbers_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StringCalculator.add("-1,-2,-3");
        });
        assertEquals("Negative numbers not allowed: -1, -2, -3", exception.getMessage());
    }
}
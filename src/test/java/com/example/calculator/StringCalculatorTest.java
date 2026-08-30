import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringCalculatorTest {

    @Test
    public void testAddEmptyString() {
        assertEquals(0, StringCalculator.add(""));
    }

    @Test
    public void testAddSingleNumber() {
        assertEquals(1, StringCalculator.add("1"));
        assertEquals(5, StringCalculator.add("5"));
    }

    @Test
    public void testAddTwoNumbers() {
        assertEquals(3, StringCalculator.add("1,2"));
        assertEquals(15, StringCalculator.add("10,5"));
    }

    @Test
    public void testAddMultipleNumbers() {
        assertEquals(6, StringCalculator.add("1,2,3"));
        assertEquals(21, StringCalculator.add("1,2,3,5,10"));
    }

    @Test
    public void testAddNewLineAsDelimiter() {
        assertEquals(6, StringCalculator.add("1\n2,3"));
    }

    @Test
    public void testAddNegativeNumbers() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StringCalculator.add("1,-2,3");
        });
        assertEquals("Negatives not allowed: -2", exception.getMessage());
    }
}
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class StringCalculatorTest {

    @Test
    public void testAddMultipleNumbers() {
        StringCalculator calculator = new StringCalculator();
        assertEquals(6, calculator.add("1,2,3");
        assertEquals(10, calculator.add("4,5,1");
    }

    @Test
    public void testAddEmptyString() {
        StringCalculator calculator = new StringCalculator();
        assertEquals(0, calculator.add(""));
    }

    @Test
    public void testAddNonNumericInput() {
        StringCalculator calculator = new StringCalculator();
        Exception exception = assertThrows(NumberFormatException.class, () -> {
            calculator.add("1,a");
        });
        assertEquals("For input string: \"a\"", exception.getMessage());
    }
}
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringCalculator {
    public int add(String numbers) {
        if (numbers.isEmpty()) return 0;
        String[] numArray = numbers.split("[,
]"); // supports comma and newline\n as separators
        int sum = 0;
        for (String num : numArray) {
            sum += Integer.parseInt(num);
        }
        return sum;
    }
}

public class StringCalculatorTest {

    private final StringCalculator calculator = new StringCalculator();

    @Test
    void testAddEmptyString() {
        assertEquals(0, calculator.add(""));
    }

    @Test
    void testAddSingleNumber() {
        assertEquals(1, calculator.add("1"));
        assertEquals(5, calculator.add("5"));
    }

    @Test
    void testAddMultipleNumbers() {
        assertEquals(3, calculator.add("1,2"));
        assertEquals(10, calculator.add("1,2,3,4"));
    }

    @Test
    void testAddWithNewLine() {
        assertEquals(6, calculator.add("1\n2,3"));
        assertEquals(10, calculator.add("1\n2\n3,4"));
    }
}

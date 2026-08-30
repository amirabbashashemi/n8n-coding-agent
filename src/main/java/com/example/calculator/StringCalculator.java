package com.example.calculator;

public class StringCalculator {

    public int add(String numbers) {
        // If the input string is empty, return 0
        if (numbers.isEmpty()) {
            return 0;
        }

        // Split the input by comma or new line
        String[] tokens = numbers.split("[,
]");
        int sum = 0;

        for (String token : tokens) {
            // Try to parse each token as an integer
            try {
                sum += Integer.parseInt(token.trim());
            } catch (NumberFormatException e) {
                // Handle invalid inputs by throwing an exception
                throw new IllegalArgumentException("Invalid input: " + token);
            }
        }

        return sum;
    }
}
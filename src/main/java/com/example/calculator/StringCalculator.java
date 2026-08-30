package com.example.calculator;

public class StringCalculator {
    public int add(String numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }
        String[] nums = numbers.split("[,
]");
        int sum = 0;
        for (String num : nums) {
            sum += Integer.parseInt(num.trim());
        }
        return sum;
    }
}
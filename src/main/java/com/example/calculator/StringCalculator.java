package com.example.calculator;

public class StringCalculator {
    public int add(String numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }

        String[] tokens = numbers.split(",|\n");
        int sum = 0;

        for (String number : tokens) {
            int num = Integer.parseInt(number.trim());
            if (num <= 1000) {
                sum += num;
            }
        }

        return sum;
    }
}
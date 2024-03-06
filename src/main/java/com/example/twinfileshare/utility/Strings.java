package com.example.twinfileshare.utility;

public class Strings {

    public static boolean isEmptyOrWhitespace(String str) {
        return str.isEmpty() || str.isBlank();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}

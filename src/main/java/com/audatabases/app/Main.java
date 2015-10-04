package com.audatabases.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    
    private static String version = "AUDB 0.0.1";

    static {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MMM/dd HH:mm");
        Date date = new Date();
        version += dateFormat.format(date); 
    }

    public static void main(String[] args) {
        System.out.println(version);
    }

}

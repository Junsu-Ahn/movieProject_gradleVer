package org.example;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
@Getter
@Setter
public class Util {
    public static String getNowDateStr() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date now = new Date();
        return formatter.format(now);
    }
}

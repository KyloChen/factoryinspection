package com.loohos.factoryinspection.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtils {
    public static int comparingMaxAlarmLevel(int a, int b, int c) {
        if(a > b){
            if(a > c)
            {
                return a;
            }else {
                return c;
            }
        }else {
            if(b > c){
                return b;
            }else {
                return c;
            }
        }
    }

    public static Date addDays(Date date, int times){
        try{
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(date);
            rightNow.add(Calendar.DAY_OF_MONTH, times);
            Date dt1 = rightNow.getTime();
            return dt1;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}

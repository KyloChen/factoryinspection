package com.loohos.factoryinspection.enumeration;

public enum SensorType {
    TOP_TEMP_SENSOR{
        public String getDescription(){return "上层温度传感器";}
    },
    MID_TEMP_SENSOR{
        public String getDescription(){return "中层温度传感器";}
    },
    BOT_TEMP_SENSOR{
        public String getDescription(){return "下层温度传感器";}
    };
    public abstract String getDescription();
}

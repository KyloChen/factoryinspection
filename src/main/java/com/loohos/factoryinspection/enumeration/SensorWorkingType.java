package com.loohos.factoryinspection.enumeration;

public enum SensorWorkingType {
    SENSOR_IS_ANOMALY{
        public String getDescription(){return "设备异常";}
    },
    SENSOR_IS_DELETE{
        public String getDescription(){return "已删除";}
    },
    SENSOR_IS_WORKING{
        public String getDescription(){return "运行正常";}
    };

    public abstract String getDescription();
}

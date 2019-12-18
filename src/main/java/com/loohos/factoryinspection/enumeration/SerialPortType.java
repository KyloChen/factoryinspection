package com.loohos.factoryinspection.enumeration;

public enum SerialPortType {
    COM1{
        public String getDescription(){return "COM1";}
    },
    COM2{
        public String getDescription(){return "COM2";}
    },
    COM3{
        public String getDescription(){return "COM3";}
    },
    COM4{
        public String getDescription(){return "COM4";}
    },
    COM5{
        public String getDescription(){return "COM5";}
    },
    COM6{
        public String getDescription(){return "COM6";}
    },
    COM7{
        public String getDescription(){return "COM7";}
    },
    COM8{
        public String getDescription(){return "COM8";}
    },
    COM9{
        public String getDescription(){return "COM9";}
    },
    COM10{
        public String getDescription(){return "COM10";}
    };
    public abstract String getDescription();
}

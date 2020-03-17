package com.loohos.factoryinspection.enumeration;

public enum CameraBrand {
    HikVision{
        public String getDescription(){
            return "HikVision";
        }
    },
    DahuaTech{
        public String getDescription(){
            return "DahuaTech";
        }
    };
    public abstract String getDescription();
}

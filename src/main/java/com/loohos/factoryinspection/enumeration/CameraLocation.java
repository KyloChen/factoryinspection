package com.loohos.factoryinspection.enumeration;

public enum CameraLocation {
    PLANT_CAMERA{
        public String getDescription(){
            return "plantCamera";
        }
    },
    PIT_CAMERA{
        public String getDescription(){
            return "pitCamera";
        }
    };
    public abstract String getDescription();
}

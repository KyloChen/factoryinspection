package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Sensor;

public class CellarSensor {
    private Cellar cellar;
    private Sensor sensor;

    public Cellar getCellar() {
        return cellar;
    }

    public void setCellar(Cellar cellar) {
        this.cellar = cellar;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }
}

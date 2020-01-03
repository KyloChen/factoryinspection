package com.loohos.factoryinspection.model.formbean;

import java.util.Date;
import java.util.List;

public class QueryHistoryResult {
    private List<Double> hisTopTemp;
    private List<Double> hisMidTemp;
    private List<Double> hisBotTemp;
    private List<Date> hisCreatedTime;

    public List<Double> getHisTopTemp() {
        return hisTopTemp;
    }

    public void setHisTopTemp(List<Double> hisTopTemp) {
        this.hisTopTemp = hisTopTemp;
    }

    public List<Double> getHisMidTemp() {
        return hisMidTemp;
    }

    public void setHisMidTemp(List<Double> hisMidTemp) {
        this.hisMidTemp = hisMidTemp;
    }

    public List<Double> getHisBotTemp() {
        return hisBotTemp;
    }

    public void setHisBotTemp(List<Double> hisBotTemp) {
        this.hisBotTemp = hisBotTemp;
    }

    public List<Date> getHisCreatedTime() {
        return hisCreatedTime;
    }

    public void setHisCreatedTime(List<Date> hisCreatedTime) {
        this.hisCreatedTime = hisCreatedTime;
    }
}

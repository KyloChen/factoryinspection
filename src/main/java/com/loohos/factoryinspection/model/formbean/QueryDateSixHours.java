package com.loohos.factoryinspection.model.formbean;

import java.util.Date;
import java.util.List;

public class QueryDateSixHours {
    private List<Double> rangeTopTemp;
    private List<Double> rangeMidTemp;
    private List<Double> rangeBotTemp;
    private List<Date> rangeCreatedTime;

    public List<Double> getRangeTopTemp() {
        return rangeTopTemp;
    }

    public void setRangeTopTemp(List<Double> rangeTopTemp) {
        this.rangeTopTemp = rangeTopTemp;
    }

    public List<Double> getRangeMidTemp() {
        return rangeMidTemp;
    }

    public void setRangeMidTemp(List<Double> rangeMidTemp) {
        this.rangeMidTemp = rangeMidTemp;
    }

    public List<Double> getRangeBotTemp() {
        return rangeBotTemp;
    }

    public void setRangeBotTemp(List<Double> rangeBotTemp) {
        this.rangeBotTemp = rangeBotTemp;
    }

    public List<Date> getRangeCreatedTime() {
        return rangeCreatedTime;
    }

    public void setRangeCreatedTime(List<Date> rangeCreatedTime) {
        this.rangeCreatedTime = rangeCreatedTime;
    }
}

package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.formbean.BaseFormBean;
import com.loohos.factoryinspection.model.local.Terminal;

public class TerminalGroup extends BaseFormBean {
    private Terminal terminal;
    private double topTemp;
    private double midTemp;
    private double botTemp;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public double getTopTemp() {
        return topTemp;
    }

    public void setTopTemp(double topTemp) {
        this.topTemp = topTemp;
    }

    public double getMidTemp() {
        return midTemp;
    }

    public void setMidTemp(double midTemp) {
        this.midTemp = midTemp;
    }

    public double getBotTemp() {
        return botTemp;
    }

    public void setBotTemp(double botTemp) {
        this.botTemp = botTemp;
    }
}

package com.loohos.factoryinspection.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;
import com.loohos.factoryinspection.service.FactoryService;
import com.loohos.factoryinspection.service.TerminalService;
import com.loohos.factoryinspection.service.TerminalValueSensorService;
import com.loohos.factoryinspection.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LocalDataSendingComponent {
    @Value("${com.loohos.machineType}")
    private String machineType;
    @Value("${com.loohos.serverUrl}")
            private String serverUrl;
    @Value("${com.loohos.loaduploadvalue}")
            private String loaduploadvalue;
    Logger logger = LoggerFactory.getLogger(LocalDataSendingComponent.class);
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;

    //本地数据发送服务 每隔一定时间 将本地数据上传至服务器

    @PostConstruct
    public void initLocalConfig(){
        System.out.println("sending component success , localConfig is " + "------------ " + machineType + "--------------");

    }

    @Scheduled(cron = "10 */5 * * * ?")
//    @Scheduled(cron = "10 * * * * ?")
    public void uploadTerminalValue(){
        String localConfig = machineType;
        if(!localConfig.equals("local")){
            return;
        }
        logger.info("Start send a terminal value");
        String retInfo = "";
        List<TerminalValueSensor> sensors = getLatestTerminal();
        if(sensors.isEmpty()) return;
//        Gson gson = new Gson();
//        JsonObject deviceJson = gson.fromJson(gson.toJson(sensor), JsonObject.class);
        JSONArray deviceJson = (JSONArray) JSONArray.toJSON(sensors);
        logger.info("jsonarray:   "+deviceJson);
        String centerUrl = serverUrl+loaduploadvalue;
        Map<String, String> headParams = new HashMap<>();
        headParams.put("Content-type", "application/json; charset=utf-8");
        headParams.put("SessionId", HttpClientUtils.getSessionId());
        retInfo = HttpClientUtils.getInstance().doPostWithJsonArray(centerUrl, headParams, deviceJson);
        if(retInfo.trim().equals("")){
            logger.error("Upload to center server failed. ");
        }else {
            logger.info(">>>>>>>>>>>>> retinfo is : " + retInfo);
        }
        logger.info("End upload info");

    }

    private List<TerminalValueSensor> getLatestTerminal(){
        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        List<TerminalValueSensor> sensors = new ArrayList<>();
        for(Terminal terminal : terminals)
        {
            TerminalValueSensor sensor = terminalValueSensorService.getLatestSensorByTerminalId(terminal);
            sensor.setSensorId(HttpClientUtils.getUUID());
            sensors.add(sensor);
        }
        System.out.println(sensors);
        return sensors;
    }

}

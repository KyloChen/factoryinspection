package com.loohos.factoryinspection.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.loohos.factoryinspection.model.formbean.TerminalGroup;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.server.ServerTerminalValueSensor;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping(value = "serverfactory")
public class ServerFactoryController {
    Logger logger = LoggerFactory.getLogger(FactoryController.class);
    SerialCommThread commThread = new SerialCommThread();
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;
    @Resource(name = "serverTerminalValueSensorServiceImpl") private ServerTerminalValueSensorService serverTerminalValueSensorService;

    @RequestMapping(value = "/loaduploadvalue")
    @ResponseBody
    public Map<String, String> localUploadvalue(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestBody String sensorInfo,
                                           ModelMap model){
        logger.info("Uploaded device info is: " + sensorInfo);
        Map<String, String> retMap = new HashMap<>();
        if(sensorInfo.trim().length() < 1) {
            retMap.put("code", "ERROR");
            return retMap;
        }

        List<ServerTerminalValueSensor> lastSensor = getLatestTerminal();

        JSONArray sensorJsonArray = JSONArray.parseArray(sensorInfo);
        for(int i = 0; i< sensorJsonArray.size(); i++){
            JSONObject sensorJson = (JSONObject) sensorJsonArray.get(i);
            ServerTerminalValueSensor sensor = JSON.toJavaObject(sensorJson,ServerTerminalValueSensor.class);
            if(lastSensor.get(i) == null){
                logger.info("服务器无数据，第一次上传");
                serverTerminalValueSensorService.save(sensor);
            }
            else if(lastSensor.get(i).getCreatedTime().getTime() == sensor.getCreatedTime().getTime())
            {
                logger.info("本次设备值为最新，不需更新至服务器");
                retMap.put("code", "duplicated");
                return retMap;
            }
            else {
                logger.info("上传最新数据成功");
                serverTerminalValueSensorService.save(sensor);
            }
        }
        retMap.put("code", "ok");
        return retMap;
    }

    private List<ServerTerminalValueSensor> getLatestTerminal(){
        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        List<ServerTerminalValueSensor> sensors = new ArrayList<>();
        for(Terminal terminal : terminals)
        {
            ServerTerminalValueSensor sensor = serverTerminalValueSensorService.getLatestSensorByTerminalId(terminal);
            sensors.add(sensor);
        }
        System.out.println(sensors);
        return sensors;
    }

    @RequestMapping(value = "/loaduploadterminal")
    @ResponseBody
    public Map<String, String> localUploadterminal(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestBody String sensorInfo,
                                           ModelMap model) {
        logger.info(">>>>>>>>>>>>>>>>> sensorinfo is : " + sensorInfo);
        Map<String, String> retMap = new HashMap<>();
        if(sensorInfo.trim().length() < 1) {
            retMap.put("code", "ERROR");
            return retMap;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        Terminal terminal = JSON.toJavaObject(jsonObject, Terminal.class);
        if(terminal!=null){
            terminalService.save(terminal);
            retMap.put("code", "ok");
            return retMap;
        }else {
            retMap.put("code", "error");
            return retMap;
        }

    }
   /**
     *厂房地图
     * */
    @RequestMapping(value = "/factorymap")
    public String helloworld(ModelMap model){
        List<Factory> factoryList = factoryService.getScrollData().getResultList();
        if(factoryList == null){
            model.put("factories", "no factory");
            logger.info("no factories1");
            return "factory/factorymap";
        }else{
            model.put("factories", factoryList);
            logger.info("gotta factories");
            return "factory/factorymap";
        }
    }

    /**
     *厂房内部
     * */
    @RequestMapping(value = "/serverfactoryinside")
    public String factoryinside(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam String factoryId,
                                ModelMap model){
        //查厂区管辖的所有终端，组装终端与其最新一条数据
        Factory factory = factoryService.getFactoryById(factoryId);
        request.getSession().setAttribute("factoryId", factoryId);
        List<Terminal> terminals = terminalService.getTerminalsByFactory(factory);
        System.out.println(terminals);
        List<TerminalGroup> temps = new ArrayList<>();
        if(terminals == null){
            model.put("factory", factory);
            return "factory/serverfactoryinside";
        }
        for (Terminal terminal : terminals){
            logger.info("server temp added");
            double topTemp = serverTerminalValueSensorService.getLatestTopTempByTerminal(terminal);
            double midTemp = serverTerminalValueSensorService.getLatestMidTempByTerminal(terminal);
            double botTemp = serverTerminalValueSensorService.getLatestBotTempByTerminal(terminal);
            TerminalGroup temp = new TerminalGroup();
            temp.setTerminal(terminal);
            temp.setTopTemp(topTemp);
            temp.setMidTemp(midTemp);
            temp.setBotTemp(botTemp);
            temps.add(temp);
        }
        model.put("factory", factory);
        model.put("sensors",temps);
        System.out.println(temps);
        return "factory/serverfactoryinside";
    }



    /**
     *厂房图表
     * */
    @RequestMapping(value = "/serverfactorygraph")
    public String factoryGraph(
            @RequestParam String terminalCode,
            ModelMap model)
    {
        Terminal terminalId = terminalService.getIdByCode(terminalCode);
        List<ServerTerminalValueSensor> sensors = serverTerminalValueSensorService.getSensorsByTerminalAndDateASC(terminalId);
        model.put("terminalCode", terminalCode);
        List<Double> topTemps = new ArrayList<>();
        List<Double> midTemps = new ArrayList<>();
        List<Double> botTemps = new ArrayList<>();
        List<Date> dates = new ArrayList<>();
        if(sensors == null){
            model.put("topTemps", "no date");
            model.put("midTemps", "no date");
            model.put("botTemps", "no date");
            model.put("dates", "no date");
            return "factory/serverfactorygraph";
        }else{
            for (ServerTerminalValueSensor sensor: sensors) {
                logger.info("got daily serversensor.");
                topTemps.add(sensor.getTopTemp());
                midTemps.add(sensor.getMidTemp());
                botTemps.add(sensor.getBotTemp());
                dates.add(sensor.getCreatedTime());
            }
            Gson gson = new Gson();
            String retString = gson.toJson(topTemps);
            model.put("topTemps", retString);

            Gson gson1 = new Gson();
            String retString1 = gson1.toJson(midTemps);
            model.put("midTemps", retString1);

            Gson gson2 = new Gson();
            String retString2 = gson2.toJson(botTemps);
            model.put("botTemps", retString2);

            Gson gson3 = new Gson();
            String retString3 = gson3.toJson(dates);
            model.put("dates", retString3);
        }
        List<ServerTerminalValueSensor> historySensors = serverTerminalValueSensorService.getHistorySensorsByTerminal(terminalId);
        if(historySensors == null){
            model.put("hisTopTemps", "no date");
            model.put("hisMidTemps", "no date");
            model.put("hisBotTemps", "no date");
            model.put("hisDates", "no date");
            return "factory/serverfactorygraph";
        }else {
            List<Double> hisTopTemps = new ArrayList<>();
            List<Double> hisMidTemps = new ArrayList<>();
            List<Double> hisBotTemps = new ArrayList<>();
            List<Date> hisDates = new ArrayList<>();
            for(ServerTerminalValueSensor sensor: historySensors)
            {
                logger.info("got history serversensor.");
                hisTopTemps.add(sensor.getTopTemp());
                hisMidTemps.add(sensor.getMidTemp());
                hisBotTemps.add(sensor.getBotTemp());
                hisDates.add(sensor.getCreatedTime());
            }
            Gson gson4 = new Gson();
            String retString4 = gson4.toJson(hisTopTemps);
            model.put("hisTopTemps", retString4);

            Gson gson5 = new Gson();
            String retString5 = gson5.toJson(hisMidTemps);
            model.put("hisMidTemps", retString5);

            Gson gson6 = new Gson();
            String retString6 = gson6.toJson(hisBotTemps);
            model.put("hisBotTemps", retString6);

            Gson gson7 = new Gson();
            String retString7 = gson7.toJson(hisDates);
            model.put("hisDates", retString7);
        }
        return "factory/serverfactorygraph";
    }
}

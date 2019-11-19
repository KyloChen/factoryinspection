package com.loohos.factoryinspection.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.model.formbean.QueryDateRange;
import com.loohos.factoryinspection.model.formbean.QueryDateRangeResult;
import com.loohos.factoryinspection.model.formbean.TerminalGroup;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "serverfactory")
public class ServerFactoryController {
    Logger logger = LoggerFactory.getLogger(FactoryController.class);
    SerialCommThread commThread = new SerialCommThread();
    @Resource(name = "configAlarmLevelServiceImpl") private ConfigAlarmLevelService configAlarmLevelService;
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
        //最新且已启用的sensor
        JSONArray sensorJsonArray = JSONArray.parseArray(sensorInfo);
        for(int i = 0; i< sensorJsonArray.size(); i++){
                JSONObject sensorJson = (JSONObject) sensorJsonArray.get(i);
                ServerTerminalValueSensor sensor = JSON.toJavaObject(sensorJson,ServerTerminalValueSensor.class);
                logger.info("上传最新数据成功");
                serverTerminalValueSensorService.save(sensor);
        }
        retMap.put("code", "ok");
        return retMap;
    }

    @RequestMapping(value = "/loaduploadthreshold")
    @ResponseBody
    public Map<String, String> loadUploadThreshold(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestBody String thresholdInfo,
                                                ModelMap model){
        logger.info("Uploaded threshold is: " + thresholdInfo);
        Map<String, String> retMap = new HashMap<>();
        if(thresholdInfo.trim().length() < 1) {
            retMap.put("code", "threshold added error");
            return retMap;
        }
        JSONObject jsonObject = JSON.parseObject(thresholdInfo);
        ConfigAlarmLevel alarmLevel = JSON.toJavaObject(jsonObject, ConfigAlarmLevel.class);
        if(alarmLevel!=null){
            configAlarmLevelService.save(alarmLevel);
            retMap.put("code", "threshold added ok");
            return retMap;
        }else {
            retMap.put("code", "threshold added error");
            return retMap;
        }
    }

    @RequestMapping(value = "/loaduploaddeletethreshold")
    @ResponseBody
    public Map<String, String> loadUploadDeleteThreshold(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @RequestBody String thresholdInfo,
                                                   ModelMap model){
        logger.info("Delete Uploaded threshold is: " + thresholdInfo);
        Map<String, String> retMap = new HashMap<>();
        if(thresholdInfo.trim().length() < 1) {
            retMap.put("code", "threshold added error");
            return retMap;
        }
        JSONObject jsonObject = JSON.parseObject(thresholdInfo);
        if(jsonObject.get("operate").toString().equals("deleteThreshold"))
        {
            String alarmId = jsonObject.get("alarmId").toString();
            configAlarmLevelService.delete(alarmId);
            logger.info("Server threshold delete success");
            retMap.put("code", "server delete threshold success");
        }
        return retMap;
    }
//
//    private List<ServerTerminalValueSensor> getLatestTerminal(){
//        //返回最新且已启用的terminalValue
//        List<Terminal> terminals = terminalService.getScrollData().getResultList();
//        List<ServerTerminalValueSensor> sensors = new ArrayList<>();
//        for(Terminal terminal : terminals){
//            if(!terminal.getInUsing())
//            {
//                logger.info("服务器设备已禁用");
//                continue;
//            }
//                ServerTerminalValueSensor sensor = serverTerminalValueSensorService.getLatestSensorByTerminalId(terminal);
//                sensors.add(sensor);
//        }
//        System.out.println(sensors);
//        return sensors;
//    }

    @RequestMapping(value = "/loaduploadterminal")
    @ResponseBody
    public Map<String, String> localUploadterminal(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestBody String sensorInfo,
                                           ModelMap model) {
        logger.info(">>>>>>>>>>>>>>>>> sensorinfo is : " + sensorInfo);
        Map<String, String> retMap = new HashMap<>();
        if(sensorInfo.trim().length() < 1) {
            retMap.put("code", "terminal added error");
            return retMap;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        Terminal terminal = JSON.toJavaObject(jsonObject, Terminal.class);
        if(terminal!=null){
//            terminalService.save(terminal);
            retMap.put("code", "terminal added ok");
            return retMap;
        }else {
            retMap.put("code", "terminal added error");
            return retMap;
        }

    }

    @RequestMapping(value = "/localUploaddeleteterminal")
    @ResponseBody
    public Map<String, String> localUploaddeleteterminal(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @RequestBody String sensorInfo,
                                                   ModelMap model) {
        logger.info(">>>>>>>>>>>>>>>>> sensorinfo is : " + sensorInfo);
        Map<String, String> retMap = new HashMap<>();
        if(sensorInfo.trim().length() < 1) {
            retMap.put("code", "terminal added error");
            return retMap;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        if(jsonObject.get("operate").toString().equals("deleteTerminal"))
        {
            Terminal terminal = terminalService.getTerminalById(jsonObject.get("terminalId").toString());
            terminal.setInUsing(false);
            terminalService.update(terminal);
            logger.info("Server terminal delete success");
            retMap.put("code", "server delete terminal success");
        }
        return retMap;
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
            String batteryState = serverTerminalValueSensorService.getBatteryStateByTerminal(terminal);
            int topAlarmLevel = serverTerminalValueSensorService.getLatestTopAlarmLevelByTerminal(terminal);
            int midAlarmLevel = serverTerminalValueSensorService.getLatestMidAlarmLevelByTerminal(terminal);
            int botAlarmLevel = serverTerminalValueSensorService.getLatestBotAlarmLevelByTerminal(terminal);
            TerminalGroup temp = new TerminalGroup();
            temp.setTerminal(terminal);
            temp.setTopTemp(topTemp);
            temp.setMidTemp(midTemp);
            temp.setTopAlarmLevel(topAlarmLevel);
            temp.setMidAlarmLevel(midAlarmLevel);
            temp.setBotAlarmLevel(botAlarmLevel);
            temp.setBatteryState(batteryState);
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
                if(
                        (sensor.getTopTemp()==0 || sensor.getTopTemp() == 3276.7)
                                || (sensor.getMidTemp()==0 || sensor.getMidTemp() == 3276.7)
                                || (sensor.getBotTemp() == 0 || sensor.getBotTemp() == 3276.7)
                        )
                {
                    logger.info("异常数据");
                    continue;
                }
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
                if(
                        (sensor.getTopTemp()==0 || sensor.getTopTemp() == 3276.7)
                                || (sensor.getMidTemp()==0 || sensor.getMidTemp() == 3276.7)
                                || (sensor.getBotTemp() == 0 || sensor.getBotTemp() == 3276.7)
                        )
                {
                    logger.info("异常数据");
                    continue;
                }
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


    /**
     *根据日期查询
     * */
    @RequestMapping(value = "/queryByDateRange")
    @ResponseBody
    public String queryByDateRange(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @ModelAttribute QueryDateRange inputData,
                                   ModelMap model){
        System.out.println(inputData.getStartDate() + " , " + inputData.getEndDate());
        String terminalCode = inputData.getTerminalCode();
        //get id by code and inUsing
        Terminal terminalId = terminalService.getIdByCode(terminalCode);
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = format1.parse(inputData.getStartDate());
            endDate = format1.parse(inputData.getEndDate());
            System.out.println(startDate + " . " + endDate);
            List<ServerTerminalValueSensor> sensorList = serverTerminalValueSensorService.getHistoryByDateRange(terminalId, startDate, endDate);
            List<Double> historyTopTemps = new ArrayList<>();
            List<Double> historyMidTemps = new ArrayList<>();
            List<Double> historyBotTemps = new ArrayList<>();
            List<Date> historyDates = new ArrayList<>();
            for (ServerTerminalValueSensor serverHistorySensor: sensorList) {
                if(
                        (serverHistorySensor.getTopTemp()==0 || serverHistorySensor.getTopTemp() == 3276.7)
                                || (serverHistorySensor.getMidTemp()==0 || serverHistorySensor.getMidTemp() == 3276.7)
                                || (serverHistorySensor.getBotTemp() == 0 || serverHistorySensor.getBotTemp() == 3276.7)
                        )
                {
                    logger.info("异常数据1");
                    continue;
                }
                historyTopTemps.add(serverHistorySensor.getTopTemp());
                historyMidTemps.add(serverHistorySensor.getMidTemp());
                historyBotTemps.add(serverHistorySensor.getBotTemp());
                historyDates.add(serverHistorySensor.getCreatedTime());
            }

            QueryDateRangeResult rangeResult = new QueryDateRangeResult();
            rangeResult.setRangeTopTemp(historyTopTemps);
            rangeResult.setRangeMidTemp(historyMidTemps);
            rangeResult.setRangeBotTemp(historyBotTemps);
            rangeResult.setRangeCreatedDate(historyDates);
            Gson gson = new Gson();
            String retString = gson.toJson(rangeResult);
            return retString;
        } catch (ParseException e) {
            e.printStackTrace();
            return "获取历史数据错误";
        }
    }
}
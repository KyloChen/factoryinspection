package com.loohos.factoryinspection.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.model.formbean.QueryDateRange;
import com.loohos.factoryinspection.model.formbean.QueryDateRangeResult;
import com.loohos.factoryinspection.model.local.LocalSite;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.formbean.TerminalGroup;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;
import com.loohos.factoryinspection.service.ConfigAlarmLevelService;
import com.loohos.factoryinspection.service.FactoryService;
import com.loohos.factoryinspection.service.TerminalService;
import com.loohos.factoryinspection.service.TerminalValueSensorService;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.HttpClientUtils;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.BlockingQueue;

@Controller
@RequestMapping(value = "/factory")
public class FactoryController {
    Logger logger = LoggerFactory.getLogger(FactoryController.class);
    SerialCommThread commThread = new SerialCommThread();
    @Value("${com.loohos.machineType}")
    private String machineType;
    @Value("${com.loohos.serverUrl}")
    private String serverUrl;
    @Value("${com.loohos.loaduploadterminal}")
    private String loaduploadterminal;
    @Value("${com.loohos.loaduploaddeleteterminal}")
    private String loaduploaddeleteterminal;
    @Value("${com.loohos.loaduploadthreshold}")
    private String loaduploadthreshold;
    @Value("${com.loohos.loaduploaddeletethreshold}")
    private String loaduploaddeletethreshold;
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;
    @Resource(name = "configAlarmLevelServiceImpl") private ConfigAlarmLevelService configAlarmLevelService;
    /**
     *厂房地图
     * */
    @RequestMapping(value = "/factorymap")
    public String helloworld(ModelMap model){
        List<Factory> factoryList = factoryService.getScrollData().getResultList();
        if(factoryList == null){
            model.put("factories", "no factory");
            logger.info("no factories");
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
    @RequestMapping(value = "/factoryinside")
    public String factoryinside(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam String factoryId,
                                ModelMap model){
        //查厂区管辖的所有终端，组装终端与其最新一条数据
        Factory factory = factoryService.getFactoryById(factoryId);
        request.getSession().setAttribute("factoryId", factoryId);
        //通过factory取terminal，terminal需要inUsing为true的
        List<Terminal> terminals = terminalService.getTerminalsByFactory(factory);
        System.out.println(terminals);
        List<TerminalGroup> temps = new ArrayList<>();
        if(terminals == null){
            model.put("factory", factory);
            return "factory/factoryinside";
        }
        for (Terminal terminal : terminals){
            double topTemp = terminalValueSensorService.getLatestTopTempByTerminal(terminal);
            double midTemp = terminalValueSensorService.getLatestMidTempByTerminal(terminal);
            double botTemp = terminalValueSensorService.getLatestBotTempByTerminal(terminal);
            String batteryState = terminalValueSensorService.getBatteryStateByTerminal(terminal);
            int topAlarmLevel = terminalValueSensorService.getLatestTopAlarmLevelByTerminal(terminal);
            int midAlarmLevel = terminalValueSensorService.getLatestMidAlarmLevelByTerminal(terminal);
            int botAlarmLevel = terminalValueSensorService.getLatestBotAlarmLevelByTerminal(terminal);
            TerminalGroup temp = new TerminalGroup();
            temp.setTerminal(terminal);
            temp.setTopTemp(topTemp);
            temp.setTopAlarmLevel(topAlarmLevel);
            temp.setMidAlarmLevel(midAlarmLevel);
            temp.setBotAlarmLevel(botAlarmLevel);
            temp.setMidTemp(midTemp);
            temp.setBotTemp(botTemp);
            temp.setBatteryState(batteryState);
            temps.add(temp);
        }
        model.put("factory", factory);
        model.put("sensors",temps);
        System.out.println(temps);
        return "factory/factoryinside";
    }

    /**
     *厂房图表
     * */
    @RequestMapping(value = "/factorygraph")
    public String factoryGraph(
            @RequestParam String terminalCode,
            ModelMap model)
    {
        //get id by code and inUsing
        Terminal terminalId = terminalService.getIdByCode(terminalCode);
        List<TerminalValueSensor> sensors = terminalValueSensorService.getSensorsByTerminalAndDateASC(terminalId);
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
            return "factory/factorygraph";
        }else{
            for (TerminalValueSensor sensor: sensors) {
                logger.info("got daily sensor.");
                //TODO 目前任一异常即处理 如果只有一个或两个部位异常呢？
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
        List<TerminalValueSensor> historySensors = terminalValueSensorService.getHistorySensorsByTerminal(terminalId);
        if(historySensors == null){
            model.put("hisTopTemps", "no date");
            model.put("hisMidTemps", "no date");
            model.put("hisBotTemps", "no date");
            model.put("hisDates", "no date");
            return "factory/factorygraph";
        }else {
            List<Double> hisTopTemps = new ArrayList<>();
            List<Double> hisMidTemps = new ArrayList<>();
            List<Double> hisBotTemps = new ArrayList<>();
            List<Date> hisDates = new ArrayList<>();
            for(TerminalValueSensor sensor: historySensors)
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
                logger.info("got history sensor.");
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
        return "factory/factorygraph";
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
            List<TerminalValueSensor> sensorList = terminalValueSensorService.getHistoryByDateRange(terminalId, startDate, endDate);
            List<Double> historyTopTemps = new ArrayList<>();
            List<Double> historyMidTemps = new ArrayList<>();
            List<Double> historyBotTemps = new ArrayList<>();
            List<Date> historyDates = new ArrayList<>();
            for (TerminalValueSensor historySensor: sensorList) {
                if(
                        (historySensor.getTopTemp()==0 || historySensor.getTopTemp() == 3276.7)
                                || (historySensor.getMidTemp()==0 || historySensor.getMidTemp() == 3276.7)
                                || (historySensor.getBotTemp() == 0 || historySensor.getBotTemp() == 3276.7)
                        )
                {
                    logger.info("异常数据");
                    continue;
                }
                historyTopTemps.add(historySensor.getTopTemp());
                historyMidTemps.add(historySensor.getMidTemp());
                historyBotTemps.add(historySensor.getBotTemp());
                historyDates.add(historySensor.getCreatedTime());
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

    /**
     *添加终端信息
     * */
    @RequestMapping(value = "/addTerminal")
    @ResponseBody
    public String addTerminal(@ModelAttribute Terminal inputData){
        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        for(Terminal terminal: terminals){
            if(terminal.getTerminalCode().equals(inputData.getTerminalCode()))
            {
                if(terminal.getInUsing()){
                    return null;
                }
            }
        }

        if(commThread.isAlive()){
            return getTerminalLoc(inputData);
        }else{
            boolean started = commThread.startCommPort("COM3", 9600, 8, 1, 0);
            if(started){
                commThread.start();
                return getTerminalLoc(inputData);
            }else{
                return getTerminalLoc(inputData);
            }
        }
    }


    /**
     * 保存终端
     */
    @RequestMapping(value = "/saveTerminal")
    @ResponseBody
    public String saveTerminal(HttpServletRequest request,
                               HttpServletResponse response,
                               @ModelAttribute Terminal inputData,
                               ModelMap model){
        System.out.println(inputData);
        inputData.setCreatedTime(new Date());
        Factory factory = factoryService.getFactoryById(request.getSession().getAttribute("factoryId").toString());
        inputData.setFactoryId(factory);
        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        for(Terminal terminal : terminals){
            if(!terminal.getInUsing())
            {
                logger.info("设备已禁用");
                continue;
            }
            if(inputData.getTerminalCode().equals(terminal.getTerminalCode()))
            {
                return "设备编号已存在，添加设备失败";
            }
        }
        JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
        logger.info("jsonarray:   "+terminalJson);
        inputData.setTerminalId(HttpClientUtils.getUUID());
        terminalService.save(inputData);
        String centerUrl = serverUrl+loaduploadterminal;
        String retInfo = sendToServer(terminalJson, centerUrl);
        logger.info(">>>>>>>retinfo is : " + retInfo);
        return "add success!";
    }


    /**
     *删除终端
     * */
    @RequestMapping(value = "/deleteTerminal")
    @ResponseBody
    public String deleteTerminal(@ModelAttribute Terminal inputData){
        String terminalId = inputData.getTerminalId();
        Terminal terminal = terminalService.getTerminalById(terminalId);
        logger.info(terminal.toString());
        try{
            terminal.setInUsing(false);
            terminalService.update(terminal);
            String centerUrl = serverUrl+loaduploaddeleteterminal;
            JSONObject deleteTerminalJson = new JSONObject();
            deleteTerminalJson.put("operate", "deleteTerminal");
            deleteTerminalJson.put("terminalId", terminal.getTerminalId());
            String retInfo = sendToServer(deleteTerminalJson, centerUrl);
            logger.info("删除服务器设备 retinfo is : " + retInfo);
            return "删除设备成功.";
        }catch (Exception e){
            logger.info(e.toString());
            return "删除设备失败.";
        }


    }

    /**
     * 获取设备阈值
     */
    @RequestMapping(value = "getThresholdTable",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String getThresholdTable(ModelMap modelMap){
        List<ConfigAlarmLevel> alarmLevels = configAlarmLevelService.getScrollData().getResultList();
        Collections.sort(alarmLevels, new Comparator<ConfigAlarmLevel>() {
            @Override
            public int compare(ConfigAlarmLevel u1, ConfigAlarmLevel u2) {
                if (u1.getAlarmLevel() > u2.getAlarmLevel()) {
                    return -1;
                }
                if (u1.getAlarmLevel() == u2.getAlarmLevel()) {
                    return 0;
                }
                return 1;
            }
        });
        String retString = JSON.toJSONString(alarmLevels);
        return retString;
    }
    /**
     * 添加阈值
     */
    @RequestMapping(value = "setThreshold",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String setThreshold(@ModelAttribute ConfigAlarmLevel inputData){
        logger.info(inputData.toString());
        //校验范围
        double minValue = inputData.getMinValue();
        double maxValue = inputData.getMaxValue();
        if(maxValue < minValue){
            return "最小值不能超过最大值，请重新设置";
        }
        int minRangeLevel = configAlarmLevelService.getIsOrNotUnderRange(minValue);
        int maxRangeLevel = configAlarmLevelService.getIsOrNotUnderRange(maxValue);
        if(minRangeLevel !=0 && maxRangeLevel != 0){
            return "最大与最小阈值与等级 " + minRangeLevel + " 范围重合，请重新设置";
        }
        else if(minRangeLevel != 0){
            return "最小阈值与等级 " + minRangeLevel + " 范围重合，请重新设置";
        }else if(maxRangeLevel != 0){
            return "最大阈值与等级 " + maxRangeLevel + " 范围重合，请重新设置";
        }

        //校验重复
        List<ConfigAlarmLevel> alarmLevels = configAlarmLevelService.getScrollData().getResultList();
        for(ConfigAlarmLevel alarmLevel : alarmLevels)
        {
            if(alarmLevel.getAlarmLevel() == inputData.getAlarmLevel()){
                return "报警等级已存在，请重新输入";
            }
        }
        //保存
        if (inputData.getAlarmLevel() == 3){
            inputData.setAlarmId(HttpClientUtils.getUUID());
            inputData.setAlarmColor("red");
            JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
            logger.info("jsonarray:   "+terminalJson);
            String centerUrl = serverUrl+loaduploadthreshold;
            String retInfo = sendToServer(terminalJson, centerUrl);
            logger.info(">>>>>>>retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置高危级别成功";
        }else if(inputData.getAlarmLevel() == 2){
            inputData.setAlarmId(HttpClientUtils.getUUID());
            inputData.setAlarmColor("orange");
            JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
            logger.info("jsonarray:   "+terminalJson);
            String centerUrl = serverUrl+loaduploadthreshold;
            String retInfo = sendToServer(terminalJson, centerUrl);
            logger.info(">>>>>>>retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置预警级别成功";
        }else if(inputData.getAlarmLevel() == 1){
            inputData.setAlarmId(HttpClientUtils.getUUID());
            inputData.setAlarmColor("green");
            JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
            logger.info("jsonarray:   "+terminalJson);
            String centerUrl = serverUrl+loaduploadthreshold;
            String retInfo = sendToServer(terminalJson, centerUrl);
            logger.info(">>>>>>>retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置安全级别成功";
        }
        //保存失败
        return "设置阈值失败.";
    }

    /**
     * 删除阈值
     */
    @RequestMapping(value = "deleteThreshold",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String deleteThreshold(@ModelAttribute ConfigAlarmLevel inputData){
        int alarmLevel1 = inputData.getAlarmLevel();
        ConfigAlarmLevel alarmLevel = configAlarmLevelService.getLevelByLevel(alarmLevel1);
        String alarmId = alarmLevel.getAlarmId();
            try{
                configAlarmLevelService.delete(alarmId);
                String centerUrl = serverUrl+loaduploaddeletethreshold;
                JSONObject deleteTerminalJson = new JSONObject();
                deleteTerminalJson.put("operate", "deleteThreshold");
                deleteTerminalJson.put("alarmId", alarmId);
                String retInfo = sendToServer(deleteTerminalJson, centerUrl);
                logger.info("删除服务器设备 retinfo is : " + retInfo);
                return "删除阈值成功.";
            }catch (Exception e){
                logger.info(e.toString());
                return "删除阈值失败.";
            }
    }
    /**
     *485取终端位置信息
     * */
    private String getTerminalLoc(Terminal terminal){
        final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
        final String rsPartitionSendCode = "00"; //分割发送码
        final String rsSufMessagefix = "AB031000000F1904"; //信息层
        final String rsSuffix = "CCCC";
        String strMessage = rsPrefix + terminal.getTerminalCode() + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
//        strMessage = "7E380FB08FD08CC72300AB031000000F1904CCCC";
        byte[] byteMessage = HexUtils.hexStringToByte(strMessage.trim());
        commThread.write(byteMessage);
        logger.info("Rs发送数据：" + HexUtils.bytesToHexString(byteMessage));
        Terminal terminal1 = new Terminal();
        try {
            Thread.sleep(4000);
            BlockingQueue<byte[]> respList = commThread.getResponseQueue();

            StringBuilder sb = new StringBuilder();
            while (respList.size() > 0){
                sb.append(HexUtils.bytesToHexString(respList.take()));
            }

            logger.info("rs接收数据Response：" + sb.toString());

            byte[] respMessage = HexUtils.hexStringToByte(sb.toString());
            logger.info("rs接收数据长度： " + respMessage.length);
            if(respMessage.length != 60){
                logger.info("terminal1 occur a problem, try to save a zero value");
                return null;
            }
            byte[] terminalId = new byte[6];
            System.arraycopy(respMessage, 26, terminalId, 0, 6);
            System.out.println(HexUtils.bytesToHexString(terminalId));
            byte[] msgContainer = new byte[26];
            System.arraycopy(respMessage, 32, msgContainer, 0, 26);
            System.out.println(HexUtils.bytesToHexString(msgContainer));

            byte[] workShopId = new byte[2];
            System.arraycopy(msgContainer, 0, workShopId, 0, 2);
            byte[] turnOutAreaId = new byte[2];
            System.arraycopy(msgContainer, 2, turnOutAreaId, 0, 2);
            byte[] teamId = new byte[2];
            System.arraycopy(msgContainer, 4, teamId, 0, 2);
            byte[] collapseId = new byte[1];
            System.arraycopy(msgContainer, 7, collapseId, 0, 1);
            byte[] rowId = new byte[1];
            System.arraycopy(msgContainer, 8, rowId, 0, 1);
            byte[] cellarId = new byte[1];
            System.arraycopy(msgContainer, 9, cellarId, 0, 1);
            byte[] frenquencePointId = new byte[1];
            System.arraycopy(msgContainer, 10, frenquencePointId, 0, 1);
            byte[] workRateId = new byte[1];
            System.arraycopy(msgContainer, 11, workRateId, 0, 1);
            byte[] periodId = new byte[2];
            System.arraycopy(msgContainer, 12, periodId, 0, 2);
            byte[] workTimeId = new byte[2];
            System.arraycopy(msgContainer, 14, workTimeId, 0, 2);
            byte[] topTemp = new byte[2];
            System.arraycopy(msgContainer, 16, topTemp, 0, 2);
            byte[] midTemp = new byte[2];
            System.arraycopy(msgContainer, 18, midTemp, 0, 2);
            byte[] botTemp = new byte[2];
            System.arraycopy(msgContainer, 20, botTemp, 0, 2);
            byte[] batteryState = new byte[1];
            System.arraycopy(msgContainer, 23, batteryState, 0, 1);
//            byte[] CRC16 = new byte[2];
//            System.arraycopy(msgContainer, 24, CRC16, 0, 2);

            String terminalId1 = HexUtils.bytesToHexString(terminalId);
            Long plant = Long.parseLong(HexUtils.bytesToHexString(workShopId),16);
            Long territory = Long.parseLong(HexUtils.bytesToHexString(turnOutAreaId),16);
            Long team = Long.parseLong(HexUtils.bytesToHexString(teamId),16);
            Long pit = Long.parseLong(HexUtils.bytesToHexString(collapseId),16);
            Long row = Long.parseLong(HexUtils.bytesToHexString(rowId),16);
            Long cellar = Long.parseLong(HexUtils.bytesToHexString(cellarId),16);
            Long frn = Long.parseLong(HexUtils.bytesToHexString(frenquencePointId),16);
            Long power = Long.parseLong(HexUtils.bytesToHexString(workRateId),16);
            Long period = Long.parseLong(HexUtils.bytesToHexString(periodId),16);
            Long workHour = Long.parseLong(HexUtils.bytesToHexString(workTimeId),16);
            String batteryState1 = HexUtils.bytesToHexString(batteryState);
//            terminal1.setTerminalId(HttpClientUtils.getUUID());
            terminal1.setTerminalCode(terminalId1);
            terminal1.setTerritoryCode(territory.toString());
            terminal1.setPlantCode(plant.toString());
            terminal1.setTeamCode(team.toString());
            terminal1.setRowCode(row.toString());
            terminal1.setPitCode(pit.toString());
            terminal1.setCellarCode(cellar.toString());
            terminal1.setBatteryState(batteryState1);
            terminal1.setFrenquency(frn.toString());
            terminal1.setPower(power.toString());
            terminal1.setActivationCycle(period.toString());
            terminal1.setWorkingHour(workHour.toString());
            terminal1.setInUsing(true);
        }catch (Exception e){
            e.printStackTrace();
            return "get terminal info error";
        }
        Gson gson = new Gson();
        String retString = gson.toJson(terminal1);
        System.out.println(retString);
        return retString;
    }

    private String sendToServer(JSONObject jsonObject, String url){
        Map<String, String> headParams = new HashMap<>();
        headParams.put("Content-type", "application/json; charset=utf-8");
        headParams.put("SessionId", HttpClientUtils.getSessionId());
        return HttpClientUtils.getInstance().doPostWithJson(url, headParams, jsonObject);
    }


    /**
     *新增数据
     * */
    @RequestMapping("/saveTest")
    @ResponseBody
    public String save() {
        logger.info(">>>>>>>>>>>>>start to save NewThemo<<<<<<<<<<<<<<<");

        if(commThread.isAlive()){
            getInfoFromTerminal();
            return "thread is alive, get directly";
        }else {
            boolean started = commThread.startCommPort("COM3", 9600, 8, 1, 0);
            if(started){
                commThread.start();
                getInfoFromTerminal();
                return "new start";
            }else{
                getInfoFromTerminal();
                return "started and get";
            }
        }
    }
    /**
     *485取采样信息
     * */
    private void getInfoFromTerminal(){
        LocalSite localSite = new LocalSite();
        Set<LocalSite> localPlants = new HashSet<>();
        localSite.setChileSite(localPlants);

        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        for(Terminal terminal:terminals)
        {
            if(!terminal.getInUsing())
            {
                logger.info(">>>>>>>>此设备已停用<<<<<<<");
                return;
            }
            final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
            String rsMidTerminalfix = terminal.getTerminalCode(); //终端ID
            final String rsPartitionSendCode = "00"; //分割发送码
            final String rsSufMessagefix = "AB031000000F1904"; //信息层
            final String rsSuffix = "CCCC";
            String strMessage = rsPrefix + rsMidTerminalfix + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
//        strMessage = "7E380FB08FD08CC72300AB031000000F1904CCCC";
            byte[] byteMessage = HexUtils.hexStringToByte(strMessage.trim());
            commThread.write(byteMessage);
            logger.info("aRs发送数据：" + HexUtils.bytesToHexString(byteMessage));

            try {
                Thread.sleep(4000);
                BlockingQueue<byte[]> respList = commThread.getResponseQueue();

                StringBuilder sb = new StringBuilder();
                while (respList.size() > 0){
                    sb.append(HexUtils.bytesToHexString(respList.take()));
                }

                logger.info("ars接收数据Response：" + sb.toString());

                byte[] respMessage = HexUtils.hexStringToByte(sb.toString());
                logger.info("rs接收数据长度： " + respMessage.length);
                if(respMessage.length != 60){
                    logger.info("terminal2 occur a problem, try to save a zero value");
                    TerminalValueSensor sensor = new TerminalValueSensor();
                    sensor.setSensorId(HttpClientUtils.getUUID());
                    sensor.setTopTemp(3276.7);
                    sensor.setMidTemp(3276.7);
                    sensor.setBotTemp(3276.7);
                    sensor.setCreatedTime(new Date());
                    sensor.setTerminalId(terminal);
                    sensor.setBatteryState("00");
                    terminalValueSensorService.save(sensor);
                    continue;
                }

                byte[] terminalId = new byte[6];
                System.arraycopy(respMessage, 26, terminalId, 0, 6);
                System.out.println(HexUtils.bytesToHexString(terminalId));
                byte[] msgContainer = new byte[26];
                System.arraycopy(respMessage, 32, msgContainer, 0, 26);
                System.out.println(HexUtils.bytesToHexString(msgContainer));

                byte[] workShopId = new byte[2];
                System.arraycopy(msgContainer, 0, workShopId, 0, 2);
                byte[] turnOutAreaId = new byte[2];
                System.arraycopy(msgContainer, 2, turnOutAreaId, 0, 2);
                byte[] teamId = new byte[2];
                System.arraycopy(msgContainer, 4, teamId, 0, 2);
                byte[] collapseId = new byte[1];
                System.arraycopy(msgContainer, 7, collapseId, 0, 1);
                byte[] rowId = new byte[1];
                System.arraycopy(msgContainer, 8, rowId, 0, 1);
                byte[] cellarId = new byte[1];
                System.arraycopy(msgContainer, 9, cellarId, 0, 1);
                byte[] frenquencePointId = new byte[1];
                System.arraycopy(msgContainer, 10, frenquencePointId, 0, 1);
                byte[] workRateId = new byte[1];
                System.arraycopy(msgContainer, 11, workRateId, 0, 1);
                byte[] periodId = new byte[2];
                System.arraycopy(msgContainer, 12, periodId, 0, 2);
                byte[] workTimeId = new byte[2];
                System.arraycopy(msgContainer, 14, workTimeId, 0, 2);
                byte[] topTemp = new byte[2];
                System.arraycopy(msgContainer, 16, topTemp, 0, 2);
                byte[] midTemp = new byte[2];
                System.arraycopy(msgContainer, 18, midTemp, 0, 2);
                byte[] botTemp = new byte[2];
                System.arraycopy(msgContainer, 20, botTemp, 0, 2);
                byte[] batteryState = new byte[1];
                System.arraycopy(msgContainer, 23, batteryState, 0, 1);
                byte[] CRC16 = new byte[2];
                System.arraycopy(msgContainer, 24, CRC16, 0, 2);

                Long workShop = Long.parseLong(HexUtils.bytesToHexString(workShopId),16);
                Long turnArea = Long.parseLong(HexUtils.bytesToHexString(turnOutAreaId),16);
                Long team = Long.parseLong(HexUtils.bytesToHexString(teamId),16);
                Long collapse = Long.parseLong(HexUtils.bytesToHexString(collapseId),16);
                Long row = Long.parseLong(HexUtils.bytesToHexString(rowId),16);
                Long cellar = Long.parseLong(HexUtils.bytesToHexString(cellarId),16);

                Long frn = Long.parseLong(HexUtils.bytesToHexString(frenquencePointId),16);
                Long workRate = Long.parseLong(HexUtils.bytesToHexString(workRateId),16);
                Long period = Long.parseLong(HexUtils.bytesToHexString(periodId),16);
                Long workTime = Long.parseLong(HexUtils.bytesToHexString(workTimeId),16);
                double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16))*1.0/10;
                System.out.println(topTemp1);
                double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp),16))*1.0/10;
                System.out.println(midTemp1);
                double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp),16))*1.0/10;
                System.out.println(botTemp1);
                String batteryState1 = HexUtils.bytesToHexString(batteryState);
                TerminalValueSensor sensor = new TerminalValueSensor();
                sensor.setSensorId(HttpClientUtils.getUUID());
                sensor.setTopTemp(topTemp1);
                sensor.setMidTemp(midTemp1);
                sensor.setBotTemp(botTemp1);
                sensor.setCreatedTime(new Date());
                sensor.setTerminalId(terminal);
                sensor.setBatteryState(batteryState1);
                terminalValueSensorService.save(sensor);
                System.out.println("Data message is: " + workShop  +"\n"
                        + "turnOutAreaId: " + turnArea+"\n"
                        + "team: "  +team +"\n"
                        + "fall: " + collapse+"\n"
                        + "row: " + row+"\n"
                        + "callar: " + cellar +"\n"
                        + "fren: " + frn+"\n"
                        + "workrate: "+ workRate+"\n"
                        + "period: " + period+"\n"
                        + "worktime: " + workTime+"\n"
                        + "top： " + topTemp1+"\n"
                        + "mid： " + midTemp1+"\n"
                        + "bottom： " + botTemp1+"\n"
                        + "state： " + batteryState1+"\n"
                        + "CRC： " + Long.parseLong(HexUtils.bytesToHexString(CRC16), 16)+"\n"
                );
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

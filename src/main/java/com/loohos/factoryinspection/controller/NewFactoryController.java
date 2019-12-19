package com.loohos.factoryinspection.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.enumeration.SerialPortType;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.model.formbean.*;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.model.server.*;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.CommonUtils;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.HttpClientUtils;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.loohos.factoryinspection.enumeration.SensorType.BOT_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.MID_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.TOP_TEMP_SENSOR;

@Controller
@RequestMapping(value = "/winery")
public class NewFactoryController  {
    private Logger logger = LoggerFactory.getLogger(NewFactoryController.class);
    private SerialCommThread commThread = new SerialCommThread();
    @Value("${com.loohos.machineType}")
    private String machineType;
    @Value("${com.loohos.serverUrl}")
    private String serverUrl;
    @Value("${com.loohos.newUploadValueUrl}")
    private String newUploadValueUrl;
    @Value("${com.loohos.newUploadSensorInfoUrl}")
    private String newUploadSensorInfoUrl;
    @Value("${com.loohos.localUploadServerPlant}")
    private String localUploadServerPlant;
    @Value("${com.loohos.localUploadServerTerritory}")
    private String localUploadServerTerritory;
    @Value("${com.loohos.localUploadServerTeam}")
    private String localUploadServerTeam;
    @Value("${com.loohos.localUploadServerPit}")
    private String localUploadServerPit;
    @Value("${com.loohos.localUploadServerRow}")
    private String localUploadServerRow;
    @Value("${com.loohos.localUploadServerCellar}")
    private String localUploadServerCellar;
    @Value("${com.loohos.localUploadServerSensor}")
    private String localUploadServerSensor;
    @Value("${com.loohos.localUploadServerValue}")
    private String localUploadServerValue;
    @Value("${com.loohos.loaduploadthreshold}")
    private String loaduploadthreshold;
    @Value("${com.loohos.loaduploaddeletethreshold}")
    private String loaduploaddeletethreshold;

    @Resource(name = "plantServiceImpl") private PlantService plantService;
    @Resource(name = "territoryServiceImpl") private TerritoryService territoryService;
    @Resource(name = "teamServiceImpl") private TeamService teamService;
    @Resource(name = "pitServiceImpl") private PitService pitService;
    @Resource(name = "rowServiceImpl") private RowService rowService;
    @Resource(name = "cellarServiceImpl") private CellarService cellarService;
    @Resource(name = "sensorNodeServiceImpl") private SensorNodeService sensorNodeService;
    @Resource(name = "sensorServiceImpl") private SensorService sensorService;
    @Resource(name = "configAlarmLevelServiceImpl") private ConfigAlarmLevelService configAlarmLevelService;
    @Resource(name = "serverPlantServiceImpl") private ServerPlantService serverPlantService;
    @Resource(name = "serverTerritoryServiceImpl") private ServerTerritoryService serverTerritoryService;
    @Resource(name = "serverTeamServiceImpl") private ServerTeamService serverTeamService;
    @Resource(name = "serverPitServiceImpl") private ServerPitService serverPitService;
    @Resource(name = "serverRowServiceImpl") private ServerRowService serverRowService;
    @Resource(name = "serverCellarServiceImpl") private ServerCellarService serverCellarService;
    @Resource(name = "serverSensorNodeServiceImpl") private ServerSensorNodeService serverSensorNodeService;
    @Resource(name = "serverSensorServiceImpl") private ServerSensorService serverSensorService;


    @RequestMapping(value = "/plant",produces={"text/html;charset=UTF-8;","application/json;"})
    public String getPlant(ModelMap modelMap){
        List<Plant> plantList = plantService.getScrollData().getResultList();
        //TODO 无数据的校验
        if(plantList.size() > 0){
            logger.info("into plant ma111p, gotta messages.");
            modelMap.put("status", "success");
            modelMap.put("plants", plantList);
        }else{
            modelMap.put("status", "error");
            logger.info("into plant map111, no message gotta.");
        }
        return "winery/localPage/plantMap";
    }

    /**
     *新增车间
     * */
    @RequestMapping(value = "/savePlant",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String savePlant(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @ModelAttribute Plant inputData,
                                   ModelMap modelMap) {
        String retString = "";
        Plant plant = plantService.getPlantByCode(inputData.getPlantCode());
        if(plant != null){
            retString = "该车间已存在.";
            logger.info(retString);
            return retString;
        }else {
            inputData.setPlantId(HttpClientUtils.getUUID());
            inputData.setAlarmLevel(1);
            plantService.save(inputData);

            ServerPlant serverPlant = new ServerPlant();
            serverPlant.setLocalPlantId(inputData.getPlantId());
            serverPlant.setPlantCode(inputData.getPlantCode());
            serverPlant.setPlantId(HttpClientUtils.getUUID());
            serverPlant.setAlarmLevel(1);
            serverPlant.setClientX(inputData.getClientX());
            serverPlant.setClientY(inputData.getClientY());
            serverPlant.setPlantName(inputData.getPlantName());
            logger.info("Start send plant value to server.....");
            String retInfoPlant = "";
            JSONObject jsonObjectServerPlant = (JSONObject) JSONObject.toJSON(serverPlant);
            logger.info("jsonarray:   "+ jsonObjectServerPlant);
            String centerUrlPlant = serverUrl+localUploadServerPlant;
            retInfoPlant = sendToServer(jsonObjectServerPlant,centerUrlPlant);
            logger.info(">>>>>>>>>>Upload state: " + retInfoPlant);
            retString = "车间添加成功";
            return retString;
        }
    }

    @RequestMapping(value = "/index",produces={"text/html;charset=UTF-8;","application/json;"})
    public String getIndex(ModelMap modelMap,
                           @RequestParam String plantId,
                           HttpServletRequest request,
                           HttpServletResponse response){
        Plant plant = plantService.find(plantId);
        modelMap.put("plant", plant);
        request.getSession().setAttribute("plantId", plantId);

        List<Territory> territories = territoryService.getTerritoriesByPlant(plant);
        List<Pit> pits = new ArrayList<>();
        if(territories == null){
//            return "factory/factorycontainer";
            return "winery/localPage/plantInside";
        }
        else {
            modelMap.put("territories", territories);
            for(Territory territory: territories) {
                List<Team> teams = teamService.getTeamsByTerritory(territory);
                for (Team team : teams) {
                    List<Pit> pits1 = pitService.getPitsByTeam(team);
                    pits.addAll(pits1);
                }
            }
            modelMap.put("pits", pits);
        }
//        return "factory/factorycontainer";
        return "winery/localPage/plantInside";
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
            return "最小值不能超过最大值，请重新设置.";
        }
        int minRangeLevel = configAlarmLevelService.getIsOrNotUnderRange(minValue);
        int maxRangeLevel = configAlarmLevelService.getIsOrNotUnderRange(maxValue);
        if(minRangeLevel !=0 && maxRangeLevel != 0){
            return "最大与最小阈值与等级 " + minRangeLevel + " 范围重合，请重新设置.";
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
            logger.info(">>>>>>>.retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置红色高危级别成功";
        }else if(inputData.getAlarmLevel() == 2){
            inputData.setAlarmId(HttpClientUtils.getUUID());
            inputData.setAlarmColor("orange");
            JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
            logger.info("jsonarray:   "+terminalJson);
            String centerUrl = serverUrl+loaduploadthreshold;
            String retInfo = sendToServer(terminalJson, centerUrl);
            logger.info(">>>>>>>.retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置橙色预警级别成功";
        }else if(inputData.getAlarmLevel() == 1){
            inputData.setAlarmId(HttpClientUtils.getUUID());
            inputData.setAlarmColor("green");
            JSONObject terminalJson = (JSONObject) JSONObject.toJSON(inputData);
            logger.info(".jsonarray:   "+terminalJson);
            String centerUrl = serverUrl+loaduploadthreshold;
            String retInfo = sendToServer(terminalJson, centerUrl);
            logger.info(">>>>>>>retinfo is : " + retInfo);
            configAlarmLevelService.save(inputData);
            return "设置绿色安全级别成功";
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
            logger.info("删除服务器设备 retinfo is  : " + retInfo);
            return "删除阈值成功.";
        }catch (Exception e){
            logger.info(e.toString());
            return "删除阈值失败.";
        }
    }
//    @RequestMapping(value = "/index",produces={"text/html;charset=UTF-8;","application/json;"})
//    public String getIndex(ModelMap modelMap,
//                           @RequestParam String plantId,
//                           HttpServletRequest request,
//                           HttpServletResponse response){
//        Plant plant = plantService.find(plantId);
//        modelMap.put("plant", plant);
//        request.getSession().setAttribute("plantId", plantId);
//
//        List<Territory> territories = territoryService.getTerritoriesByPlant(plant);
//        List<Pit> pits = new ArrayList<>();
//        List<Sensor> sensors = new ArrayList<>();
//        if(territories == null){
////            return "factory/factorycontainer";
//            return "factory/plantInside";
//        }
//        else {
//            modelMap.put("territories", territories);
//            for(Territory territory: territories) {
//                List<Team> teams = teamService.getTeamsByTerritory(territory);
//                for (Team team : teams) {
//                    List<Pit> pits1 = pitService.getPitsByTeam(team);
//                    pits.addAll(pits1);
//                    for (Pit pit : pits) {
//                        List<Row> rows = rowService.getRowByPit(pit);
//                        for (Row row : rows) {
//                            List<Cellar> cellars = cellarService.getCellarByRow(row);
//                            Collections.sort(cellars, new Comparator<Cellar>() {
//                                @Override
//                                public int compare(Cellar o1, Cellar o2) {
//                                    return o1.getCellarCode() - o2.getCellarCode();//这里是因为由于第一个方法将所有的月份或者年份都排到了下标为0的位置，所以直接比对哈希值即可了。。。。具体逻辑具体分析嘛
//                                }
//                            });
//                            for (Cellar cellar : cellars) {
//                                Sensor sensor = sensorService.getSensorByCellar(cellar);
//                                sensors.add(sensor);
//                            }
//                        }
//                    }
//                }
//            }
//            modelMap.put("pits", pits);
//            modelMap.put("sensors", sensors);
//        }
////        return "factory/factorycontainer";
//        return "factory/plantInside";
//    }

    @RequestMapping(value = "/showCellar",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showCellar(@ModelAttribute Pit inputData,
                                 ModelMap modelMap){
        Pit pit = pitService.find(inputData.getPitId());
        modelMap.put("pit", pit);
        List<Row> rows = rowService.getRowByPit(pit);
        Map<Integer, List<Cellar>> cellarTree = new TreeMap<>();
        if(rows.size() < 4){
            for (int i = rows.size()+1; i<=4 ; i++){
                Row row = new Row();
                row.setRowCode(i);
                row.setRowType("false");
                rows.add(row);
            }
        }
        modelMap.put("rows", rows);
        for(Row row: rows){
            List<Cellar> cellars = new ArrayList<>();
            if(row.getRowType().equals("false")){
                for(int i = 1; i<=12; i++){

                    Cellar cellar = new Cellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    Sensor sensor = new Sensor();
                    sensor.setAlarmLevel(0);
                    cellar.setSensor(sensor);
                    cellars.add(cellar);
                    logger.info("填充项");
                }
            } else if(cellars.size() < 12 ){
                cellars = cellarService.getCellarByRowDesc(row);
                for(int i = cellars.size()+1; i<=12; i++){
                    Cellar cellar = new Cellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    Sensor sensor = new Sensor();
                    sensor.setAlarmLevel(0);
                    cellar.setSensor(sensor);
                    cellars.add(cellar);
                    logger.info("正常项");
                }
            }
            Collections.sort(cellars, new Comparator<Cellar>() {
                @Override
                public int compare(Cellar o1, Cellar o2) {
                    return o2.getCellarCode() - o1.getCellarCode();
                }
            });
            cellarTree.put(row.getRowCode(), cellars);
        }
        modelMap.put("cellarTree",cellarTree);
        return "winery/localPage/cellarContainer";
    }

    @RequestMapping(value = "/showCellarInfo",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showCellarInfo(@ModelAttribute Cellar inputData,
                                 ModelMap modelMap){
        Cellar cellar = cellarService.find(inputData.getCellarId());
        Sensor sensor = sensorService.getSensorByCellar(cellar);
        modelMap.put("sensor", sensor);
        SensorNode topNode = sensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, TOP_TEMP_SENSOR);
        SensorNode midNode = sensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, MID_TEMP_SENSOR);
        SensorNode botNode = sensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, BOT_TEMP_SENSOR);
        modelMap.put("topNode", topNode);
        modelMap.put("midNode", midNode);
        modelMap.put("botNode", botNode);
        return "factory/factoryCellarContainer";
    }

    @RequestMapping(value = "/showSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showSensorCurve(@ModelAttribute Sensor inputData,
                                 ModelMap modelMap){
        Sensor sensor = sensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", sensor.getSensorId());
        List<Double> topTemps = sensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> midTemps = sensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> botTemps = sensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> createdTime = sensorNodeService.getTimeBySensorAndCurDay(sensor);
        Gson topGson = new Gson();
        String topRetString = topGson.toJson(topTemps);
        modelMap.put("topTemps", topRetString);
        Gson midGson = new Gson();
        String midRetString = midGson.toJson(midTemps);
        modelMap.put("midTemps", midRetString);
        Gson botGson = new Gson();
        String botRetString = botGson.toJson(botTemps);
        modelMap.put("botTemps", botRetString);
        Gson timeGson = new Gson();
        String timeRetString = timeGson.toJson(createdTime);
        modelMap.put("createdTimes", timeRetString);

        List<Double> hisTopTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> hisMidTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> hisBotTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> hisCreatedTime = sensorNodeService.getHistoryTimeBySensorAndCurDay(sensor);
        Gson topHisGson = new Gson();
        String topHisRetString = topHisGson.toJson(hisTopTemps);
        modelMap.put("hisTopTemps", topHisRetString);
        Gson midHisGson = new Gson();
        String midHisRetString = midHisGson.toJson(hisMidTemps);
        modelMap.put("hisMidTemps", midHisRetString);
        Gson botHisGson = new Gson();
        String botHisRetString = botHisGson.toJson(hisBotTemps);
        modelMap.put("hisBotTemps", botHisRetString);
        Gson timeHisGson = new Gson();
        String timeHisRetString = timeHisGson.toJson(hisCreatedTime);
        modelMap.put("hisCreatedTime", timeHisRetString);
        return "winery/localPage/factoryCurveContainer";
    }

    @RequestMapping(value = "/showHisSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showHisSensorCurve(@ModelAttribute Sensor inputData,
                                  ModelMap modelMap){
        Sensor sensor = sensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", sensor.getSensorId());
        List<Double> hisTopTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> hisMidTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> hisBotTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> hisCreatedTime = sensorNodeService.getHistoryTimeBySensorAndCurDay(sensor);
        Gson topGson = new Gson();
        String topRetString = topGson.toJson(hisTopTemps);
        modelMap.put("hisTopTemps", topRetString);
        Gson midGson = new Gson();
        String midRetString = midGson.toJson(hisMidTemps);
        modelMap.put("hisMidTemps", midRetString);
        Gson botGson = new Gson();
        String botRetString = botGson.toJson(hisBotTemps);
        modelMap.put("hisBotTemps", botRetString);
        Gson timeGson = new Gson();
        String timeRetString = timeGson.toJson(hisCreatedTime);
        modelMap.put("hisCreatedTime", timeRetString);
        return "factory/factoryHisCurveContainer";
    }


    /**
     *根据日期查询
     * */
    @RequestMapping(value = "/queryByDateRange",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String queryByDateRange(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @ModelAttribute QuerySensorDate inputData,
                                   ModelMap modelMap){
        String sensorId = inputData.getSensorId();
        //get id by code and inUsing
        Sensor sensor = sensorService.find(sensorId);
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            //加24小时找值
            startDate = CommonUtils.addDays(format1.parse(inputData.getStartDate()), 1);
            endDate = CommonUtils.addDays(format1.parse(inputData.getEndDate()), 1);
            logger.info("Start query.....");
            List<Double> queryTopTemps = sensorNodeService.getValuesBySensorAndRange(sensor, TOP_TEMP_SENSOR, startDate, endDate);
            List<Double> queryMidTemps = sensorNodeService.getValuesBySensorAndRange(sensor, MID_TEMP_SENSOR, startDate, endDate);
            List<Double> queryBotTemps = sensorNodeService.getValuesBySensorAndRange(sensor, BOT_TEMP_SENSOR, startDate, endDate);
            List<Date> queryDates = sensorNodeService.getDatesBySensorAndRange(sensor, startDate, endDate);
            QueryDateRangeResult rangeResult = new QueryDateRangeResult();
            rangeResult.setRangeTopTemp(queryTopTemps);
            rangeResult.setRangeMidTemp(queryMidTemps);
            rangeResult.setRangeBotTemp(queryBotTemps);
            rangeResult.setRangeCreatedDate(queryDates);
            Gson gson = new Gson();
            String retString = gson.toJson(rangeResult);
            return retString;
        } catch (Exception e) {
            e.printStackTrace();
            return "获取历史数据错误";
        }
    }

    /**
     *添加终端信息
     * */
    @RequestMapping(value = "/addSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String addSensor(@ModelAttribute Sensor inputData,
                            HttpServletRequest request){
        List<Sensor> sensors = sensorService.getScrollData().getResultList();
        String plantId = request.getSession().getAttribute("plantId").toString();
        String retString = "";
        String sensorCode = inputData.getSensorCode();
        if(sensors.size() == 0){
            retString = "first time to add sensor";
            logger.info(retString);
        }else {
            for (Sensor sensor : sensors){
                if(sensorCode.equals(sensor.getSensorCode())){
                    if(sensor.getSensorWorkingType().equals(SensorWorkingType.SENSOR_IS_WORKING)){
                        retString = "设备已存在，请重新输入特征码.";
                        return retString;
                    }else if(sensor.getSensorWorkingType().equals(SensorWorkingType.SENSOR_IS_ANOMALY)){
                        logger.info("本次录入异常设备.");
                    }

                }
            }
        }
        if(commThread.isAlive()){
            return getSensorInfo(sensorCode, plantId);
        }else{
            boolean started = commThread.startCommPort(SerialPortType.COM3.getDescription(), 9600, 8, 1, 0);
            if(started){
                commThread.start();
                return getSensorInfo(sensorCode, plantId);
            }else{
                return getSensorInfo(sensorCode, plantId);
            }
        }
    }

    private String getSensorInfoDesc(String sensorCode, String plantId) {
        String retString = "";
        final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
        final String rsPartitionSendCode = "00"; //分割发送码
        final String rsSufMessagefix = "AB031000000F1904"; //信息层
        final String rsSuffix = "CCCC";
        String strMessage = rsPrefix + sensorCode + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
        String workingType = "";
        Plant plant = plantService.find(plantId);
//        strMessage = "7E380FB08FD08CC72300AB031000000F1904CCCC";
        byte[] byteMessage = HexUtils.hexStringToByte(strMessage.trim());
        commThread.write(byteMessage);
        logger.info("Rs发送数据：" + HexUtils.bytesToHexString(byteMessage));
        try {
            Thread.sleep(4000);
            BlockingQueue<byte[]> respList = commThread.getResponseQueue();

            StringBuilder sb = new StringBuilder();
            while (respList.size() > 0) {
                sb.append(HexUtils.bytesToHexString(respList.take()));
            }

            logger.info("rs接收数据Response：" + sb.toString());

            byte[] respMessage = HexUtils.hexStringToByte(sb.toString());
            logger.info("rs接收数据长度： " + respMessage.length);
            if (respMessage.length != 60) {
                logger.info("sen occur a problem, try to save a zero value");
                retString = "当前录入设备异常，请处理后录入.（可能原因：1.电量不足。2.设备故障。）";
                return retString;
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

            String retSensorCode = HexUtils.bytesToHexString(terminalId);
            int plantCode = Integer.parseInt(HexUtils.bytesToHexString(workShopId), 16);
            int territoryCode = Integer.parseInt(HexUtils.bytesToHexString(turnOutAreaId), 16);
            int teamCode = Integer.parseInt(HexUtils.bytesToHexString(teamId), 16);
            int pitCode = Integer.parseInt(HexUtils.bytesToHexString(collapseId), 16);
            int rowCode = Integer.parseInt(HexUtils.bytesToHexString(rowId), 16);
            int cellarCode = Integer.parseInt(HexUtils.bytesToHexString(cellarId), 16);
            int frequencyPoint = Integer.parseInt(HexUtils.bytesToHexString(frenquencePointId), 16);
            int workRate = Integer.parseInt(HexUtils.bytesToHexString(workRateId), 16);
            int activatePeriod = Integer.parseInt(HexUtils.bytesToHexString(periodId), 16);
            int workingHours = Integer.parseInt(HexUtils.bytesToHexString(workTimeId), 16);
            String batteryState1 = HexUtils.bytesToHexString(batteryState);
            double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16)) * 1.0 / 10;
            logger.error("The topTemp is [{}]", topTemp1);
            double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp), 16)) * 1.0 / 10;
            logger.error("The midTemp is [{}]", midTemp1);
            double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp), 16)) * 1.0 / 10;
            logger.error("The botTemp is [{}]", botTemp1);
            List<Sensor> sensors = sensorService.getScrollData().getResultList();
            if(sensors!=null){
                for (Sensor sensor: sensors){
                    //特征码相同且设备启用 返回错误
                    if(sensor.getSensorCode().equals(sensorCode)){
                        if(!sensor.getSensorWorkingType().getDescription().equals("SENSOR_IS_DELETE"))
                        {
                            retString = "特征码已存在";
                            logger.info(retString);
                            return retString;
                        }else {
                            workingType = "SENSOR_IS_DELETE";
                            retString = "已删除设备重录.";
                            logger.info(retString);
                        }
                    }
                }
            }
            if(plant.getPlantCode() != plantCode){
                retString = "设备车间不一致，请重新选择车间.";
                logger.info(retString);
                return retString;
            }
            ServerPlant serverPlant = new ServerPlant();
            serverPlant.setPlantId(HttpClientUtils.getUUID());
            serverPlant.setPlantCode(plant.getPlantCode());
            serverPlant.setPlantName(plant.getPlantName());
            serverPlant.setClientX(plant.getClientX());
            serverPlant.setClientY(plant.getClientY());
            serverPlant.setAlarmLevel(plant.getAlarmLevel());
            serverPlant.setLocalPlantId(plant.getPlantId());
            serverPlant.setTerritories(null);
            //校验完毕开始保存
            Territory territory = territoryService.getTerritoryByPlantAndTerritoryCode(plant, territoryCode);
            //责任区已存在，下一轮比较
            if(territory != null){
                ServerTerritory serverTerritory = new ServerTerritory();
                serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                serverTerritory.setTerritoryCode(territoryCode);
                serverTerritory.setServerPlant(serverPlant);
                serverTerritory.setLocalTerritoryId(territory.getTerritoryId());
                serverTerritory.setServerTeams(null);

                Team team = teamService.getTeamsByTerritoryAndTeamCode(territory, teamCode);
                if(team != null){

                    ServerTeam serverTeam = new ServerTeam();
                    serverTeam.setTeamId(HttpClientUtils.getUUID());
                    serverTeam.setTeamCode(teamCode);
                    serverTeam.setServerTerritory(serverTerritory);
                    serverTeam.setLocalTeamId(team.getTeamId());
                    serverTeam.setServerPits(null);

                    Pit pit = pitService.getPitByTeamAndPitCode(team, pitCode);
                    if(pit != null){

                        ServerPit serverPit = new ServerPit();
                        serverPit.setPitId(HttpClientUtils.getUUID());
                        serverPit.setPitCode(pit.getPitCode());
                        serverPit.setServerTeam(serverTeam);
                        serverPit.setLocalPitId(pit.getPitId());
                        serverPit.setServerRows(null);

                        Row row = rowService.getRowByPitAndRowCode(pit, rowCode);
                        if(row != null)
                        {

                            ServerRow serverRow = new ServerRow();
                            serverRow.setRowId(HttpClientUtils.getUUID());
                            serverRow.setRowCode(rowCode);
                            serverRow.setServerPit(serverPit);
                            serverRow.setLocalRowId(row.getRowId());
                            serverRow.setServerCellars(null);

                            Cellar cellar = cellarService.getCellarByRowAndCellarCode(row, cellarCode);
                            if(cellar != null){
                                return "false";
                            }else {


                                Cellar cellar1 = new Cellar();
                                cellar1.setRow(row);
                                cellar1.setCellarId(HttpClientUtils.getUUID());
                                cellar1.setCellarCode(cellarCode);
                                cellarService.save(cellar1);

                                ServerCellar serverCellar = new ServerCellar();
                                serverCellar.setCellarId(HttpClientUtils.getUUID());
                                serverCellar.setCellarCode(cellarCode);
                                serverCellar.setServerRow(serverRow);
                                serverCellar.setLocalCellarId(cellar1.getCellarId());
                                serverCellar.setServerSensor(null);

                                Sensor sensor = new Sensor();
                                sensor.setSensorId(HttpClientUtils.getUUID());
                                sensor.setSensorCode(sensorCode);
                                sensor.setFrequencyPoint(frequencyPoint);
                                sensor.setWorkRate(workRate);
                                sensor.setActivatePeriod(activatePeriod);
                                sensor.setWorkingHours(workingHours);
                                sensor.setBatteryState(batteryState1);
                                sensor.setCreatedTime(new Date());
                                sensor.setCellar(cellar1);
                                sensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                if (workingType.equals("SENSOR_IS_DELETE"))
                                {
                                    sensorService.update(sensor);
                                }else {
                                    sensorService.save(sensor);
                                }

                                ServerSensor serverSensor = new ServerSensor();
                                serverSensor.setSensorId(HttpClientUtils.getUUID());
                                serverSensor.setSensorCode(sensorCode);
                                serverSensor.setFrequencyPoint(frequencyPoint);
                                serverSensor.setWorkRate(workRate);
                                serverSensor.setActivatePeriod(activatePeriod);
                                serverSensor.setWorkingHours(workingHours);
                                serverCellar.setServerRow(null);
                                serverSensor.setServerCellar(serverCellar);
                                serverSensor.setBatteryState(batteryState1);
                                serverSensor.setCreatedTime(new Date());
                                serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                serverSensor.setLocalSensorId(sensor.getSensorId());
                                serverSensor.setSensorNodeSet(null);
                                if(workingType.equals("SENSOR_IS_DELETE")){
                                    serverSensor.setSavingType("update");
                                }else {
                                    serverSensor.setSavingType("save");
                                }

                                logger.info("Start send territory value to server.....");
                                String retInfoTerritory = "";
                                JSONObject jsonObjectServerTerritory = (JSONObject) JSONObject.toJSON(serverTerritory);
                                logger.info("jsonarray:   "+ jsonObjectServerTerritory);
                                String centerUrlTerritory = serverUrl+localUploadServerTerritory;
                                Map<String, String> headParamsTerritory = new HashMap<>();
                                headParamsTerritory.put("Content-type", "application/json; charset=utf-8");
                                headParamsTerritory.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoTerritory = HttpClientUtils.getInstance().doPostWithJson(centerUrlTerritory, headParamsTerritory, jsonObjectServerTerritory);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoTerritory);

                                logger.info("Start send team value to server.....");
                                String retInfoTeam = "";
                                JSONObject jsonObjectServerTeam = (JSONObject) JSONObject.toJSON(serverTeam);
                                logger.info("jsonarray:   "+ jsonObjectServerTeam);
                                String centerUrlTeam = serverUrl+localUploadServerTeam;
                                Map<String, String> headParamsTeam = new HashMap<>();
                                headParamsTeam.put("Content-type", "application/json; charset=utf-8");
                                headParamsTeam.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoTeam = HttpClientUtils.getInstance().doPostWithJson(centerUrlTeam, headParamsTeam, jsonObjectServerTeam);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoTeam);


                                logger.info("Start send pit value to server.....");
                                String retInfoPit = "";
                                JSONObject jsonObjectServerPit = (JSONObject) JSONObject.toJSON(serverPit);
                                logger.info("jsonarray:   "+ jsonObjectServerPit);
                                String centerUrlPit = serverUrl+localUploadServerPit;
                                Map<String, String> headParamsPit = new HashMap<>();
                                headParamsPit.put("Content-type", "application/json; charset=utf-8");
                                headParamsPit.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoPit = HttpClientUtils.getInstance().doPostWithJson(centerUrlPit, headParamsPit, jsonObjectServerPit);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoPit);

                                logger.info("Start send row value to server.....");
                                String retInfoRow = "";
                                JSONObject jsonObjectServerRow = (JSONObject) JSONObject.toJSON(serverRow);
                                logger.info("jsonarray:   "+ jsonObjectServerRow);
                                String centerUrlRow = serverUrl+localUploadServerRow;
                                Map<String, String> headParamsRow = new HashMap<>();
                                headParamsRow.put("Content-type", "application/json; charset=utf-8");
                                headParamsRow.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoRow = HttpClientUtils.getInstance().doPostWithJson(centerUrlRow, headParamsRow, jsonObjectServerRow);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoRow);

                                logger.info("Start send cellar value to server.....");
                                String retInfoCellar = "";
                                JSONObject jsonObjectServerCellar = (JSONObject) JSONObject.toJSON(serverCellar);
                                logger.info("jsonarray:   "+ jsonObjectServerCellar);
                                String centerUrlCellar = serverUrl+localUploadServerCellar;
                                Map<String, String> headParamsCellar = new HashMap<>();
                                headParamsCellar.put("Content-type", "application/json; charset=utf-8");
                                headParamsCellar.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoCellar = HttpClientUtils.getInstance().doPostWithJson(centerUrlCellar, headParamsCellar, jsonObjectServerCellar);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoCellar);

                                logger.info("Start send sensor value to server.....");
                                String retInfoSensor = "";
                                JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(serverSensor);
                                logger.info("jsonarray:   "+ jsonObjectServerSensor);
                                String centerUrlSensor = serverUrl+localUploadServerSensor;
                                Map<String, String> headParamsSensor = new HashMap<>();
                                headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                                headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                                retString = "异窖信息录入成功.";
                                logger.info(retString);
                                return retString;
                            }
                        }else {
                            Row row1 = new Row();
                            row1.setRowId(HttpClientUtils.getUUID());
                            row1.setRowCode(rowCode);
                            row1.setPit(pit);
                            rowService.save(row1);

                            ServerRow serverRow = new ServerRow();
                            serverRow.setRowId(HttpClientUtils.getUUID());
                            serverRow.setRowCode(rowCode);
                            serverRow.setServerPit(serverPit);
                            serverRow.setLocalRowId(row1.getRowId());
                            serverRow.setServerCellars(null);

                            Cellar cellar1 = new Cellar();
                            cellar1.setRow(row1);
                            cellar1.setCellarId(HttpClientUtils.getUUID());
                            cellar1.setCellarCode(cellarCode);
                            cellarService.save(cellar1);

                            ServerCellar serverCellar = new ServerCellar();
                            serverCellar.setCellarId(HttpClientUtils.getUUID());
                            serverCellar.setCellarCode(cellarCode);
                            serverCellar.setServerRow(serverRow);
                            serverCellar.setLocalCellarId(cellar1.getCellarId());
                            serverCellar.setServerSensor(null);

                            Sensor sensor = new Sensor();
                            sensor.setSensorId(HttpClientUtils.getUUID());
                            sensor.setSensorCode(sensorCode);
                            sensor.setFrequencyPoint(frequencyPoint);
                            sensor.setWorkRate(workRate);
                            sensor.setActivatePeriod(activatePeriod);
                            sensor.setWorkingHours(workingHours);
                            sensor.setBatteryState(batteryState1);
                            sensor.setCreatedTime(new Date());
                            sensor.setCellar(cellar1);
                            sensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                            if (workingType.equals("SENSOR_IS_DELETE"))
                            {
                                sensorService.update(sensor);
                            }else {
                                sensorService.save(sensor);
                            }

                            ServerSensor serverSensor = new ServerSensor();
                            serverSensor.setSensorId(HttpClientUtils.getUUID());
                            serverSensor.setSensorCode(sensorCode);
                            serverSensor.setFrequencyPoint(frequencyPoint);
                            serverSensor.setWorkRate(workRate);
                            serverSensor.setActivatePeriod(activatePeriod);
                            serverSensor.setWorkingHours(workingHours);
                            serverCellar.setServerRow(null);
                            serverSensor.setServerCellar(serverCellar);
                            serverSensor.setBatteryState(batteryState1);
                            serverSensor.setCreatedTime(new Date());
                            serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                            serverSensor.setLocalSensorId(sensor.getSensorId());
                            serverSensor.setSensorNodeSet(null);
                            if(workingType.equals("SENSOR_IS_DELETE")){
                                serverSensor.setSavingType("update");
                            }else {
                                serverSensor.setSavingType("save");
                            }

                            logger.info("Start send territory value to server.....");
                            String retInfoTerritory = "";
                            JSONObject jsonObjectServerTerritory = (JSONObject) JSONObject.toJSON(serverTerritory);
                            logger.info("jsonarray:   "+ jsonObjectServerTerritory);
                            String centerUrlTerritory = serverUrl+localUploadServerTerritory;
                            Map<String, String> headParamsTerritory = new HashMap<>();
                            headParamsTerritory.put("Content-type", "application/json; charset=utf-8");
                            headParamsTerritory.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoTerritory = HttpClientUtils.getInstance().doPostWithJson(centerUrlTerritory, headParamsTerritory, jsonObjectServerTerritory);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoTerritory);

                            logger.info("Start send team value to server.....");
                            String retInfoTeam = "";
                            JSONObject jsonObjectServerTeam = (JSONObject) JSONObject.toJSON(serverTeam);
                            logger.info("jsonarray:   "+ jsonObjectServerTeam);
                            String centerUrlTeam = serverUrl+localUploadServerTeam;
                            Map<String, String> headParamsTeam = new HashMap<>();
                            headParamsTeam.put("Content-type", "application/json; charset=utf-8");
                            headParamsTeam.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoTeam = HttpClientUtils.getInstance().doPostWithJson(centerUrlTeam, headParamsTeam, jsonObjectServerTeam);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoTeam);


                            logger.info("Start send pit value to server.....");
                            String retInfoPit = "";
                            JSONObject jsonObjectServerPit = (JSONObject) JSONObject.toJSON(serverPit);
                            logger.info("jsonarray:   "+ jsonObjectServerPit);
                            String centerUrlPit = serverUrl+localUploadServerPit;
                            Map<String, String> headParamsPit = new HashMap<>();
                            headParamsPit.put("Content-type", "application/json; charset=utf-8");
                            headParamsPit.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoPit = HttpClientUtils.getInstance().doPostWithJson(centerUrlPit, headParamsPit, jsonObjectServerPit);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoPit);

                            logger.info("Start send row value to server.....");
                            String retInfoRow = "";
                            JSONObject jsonObjectServerRow = (JSONObject) JSONObject.toJSON(serverRow);
                            logger.info("jsonarray:   "+ jsonObjectServerRow);
                            String centerUrlRow = serverUrl+localUploadServerRow;
                            Map<String, String> headParamsRow = new HashMap<>();
                            headParamsRow.put("Content-type", "application/json; charset=utf-8");
                            headParamsRow.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoRow = HttpClientUtils.getInstance().doPostWithJson(centerUrlRow, headParamsRow, jsonObjectServerRow);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoRow);

                            logger.info("Start send cellar value to server.....");
                            String retInfoCellar = "";
                            JSONObject jsonObjectServerCellar = (JSONObject) JSONObject.toJSON(serverCellar);
                            logger.info("jsonarray:   "+ jsonObjectServerCellar);
                            String centerUrlCellar = serverUrl+localUploadServerCellar;
                            Map<String, String> headParamsCellar = new HashMap<>();
                            headParamsCellar.put("Content-type", "application/json; charset=utf-8");
                            headParamsCellar.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoCellar = HttpClientUtils.getInstance().doPostWithJson(centerUrlCellar, headParamsCellar, jsonObjectServerCellar);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoCellar);

                            logger.info("Start send sensor value to server.....");
                            String retInfoSensor = "";
                            JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(serverSensor);
                            logger.info("jsonarray:   "+ jsonObjectServerSensor);
                            String centerUrlSensor = serverUrl+localUploadServerSensor;
                            Map<String, String> headParamsSensor = new HashMap<>();
                            headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                            headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                            retString = "异排信息录入成功.";
                            logger.info(retString);
                            return retString;
                        }
                    }else{
                        Pit pit1 = new Pit();
                        pit1.setPitId(HttpClientUtils.getUUID());
                        pit1.setPitCode(pitCode);
                        pit1.setTeam(team);
                        pitService.save(pit1);

                        ServerPit serverPit = new ServerPit();
                        serverPit.setPitId(HttpClientUtils.getUUID());
                        serverPit.setPitCode(pit.getPitCode());
                        serverPit.setServerTeam(serverTeam);
                        serverPit.setLocalPitId(pit1.getPitId());
                        serverPit.setServerRows(null);

                        Row row1 = new Row();
                        row1.setRowId(HttpClientUtils.getUUID());
                        row1.setRowCode(rowCode);
                        row1.setPit(pit1);
                        rowService.save(row1);

                        ServerRow serverRow = new ServerRow();
                        serverRow.setRowId(HttpClientUtils.getUUID());
                        serverRow.setRowCode(rowCode);
                        serverRow.setServerPit(serverPit);
                        serverRow.setLocalRowId(row1.getRowId());
                        serverRow.setServerCellars(null);

                        Cellar cellar1 = new Cellar();
                        cellar1.setRow(row1);
                        cellar1.setCellarId(HttpClientUtils.getUUID());
                        cellar1.setCellarCode(cellarCode);
                        cellarService.save(cellar1);

                        ServerCellar serverCellar = new ServerCellar();
                        serverCellar.setCellarId(HttpClientUtils.getUUID());
                        serverCellar.setCellarCode(cellarCode);
                        serverCellar.setServerRow(serverRow);
                        serverCellar.setLocalCellarId(cellar1.getCellarId());
                        serverCellar.setServerSensor(null);

                        Sensor sensor = new Sensor();
                        sensor.setSensorId(HttpClientUtils.getUUID());
                        sensor.setSensorCode(sensorCode);
                        sensor.setFrequencyPoint(frequencyPoint);
                        sensor.setWorkRate(workRate);
                        sensor.setActivatePeriod(activatePeriod);
                        sensor.setWorkingHours(workingHours);
                        sensor.setBatteryState(batteryState1);
                        sensor.setCreatedTime(new Date());
                        sensor.setCellar(cellar1);
                        sensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                        if (workingType.equals("SENSOR_IS_DELETE"))
                        {
                            sensorService.update(sensor);
                        }else {
                            sensorService.save(sensor);
                        }

                        ServerSensor serverSensor = new ServerSensor();
                        serverSensor.setSensorId(HttpClientUtils.getUUID());
                        serverSensor.setSensorCode(sensorCode);
                        serverSensor.setFrequencyPoint(frequencyPoint);
                        serverSensor.setWorkRate(workRate);
                        serverSensor.setActivatePeriod(activatePeriod);
                        serverSensor.setWorkingHours(workingHours);
                        serverCellar.setServerRow(null);
                        serverSensor.setServerCellar(serverCellar);
                        serverSensor.setBatteryState(batteryState1);
                        serverSensor.setCreatedTime(new Date());
                        serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                        serverSensor.setLocalSensorId(sensor.getSensorId());
                        serverSensor.setSensorNodeSet(null);
                        if(workingType.equals("SENSOR_IS_DELETE")){
                            serverSensor.setSavingType("update");
                        }else {
                            serverSensor.setSavingType("save");
                        }

                        logger.info("Start send territory value to server.....");
                        String retInfoTerritory = "";
                        JSONObject jsonObjectServerTerritory = (JSONObject) JSONObject.toJSON(serverTerritory);
                        logger.info("jsonarray:   "+ jsonObjectServerTerritory);
                        String centerUrlTerritory = serverUrl+localUploadServerTerritory;
                        Map<String, String> headParamsTerritory = new HashMap<>();
                        headParamsTerritory.put("Content-type", "application/json; charset=utf-8");
                        headParamsTerritory.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoTerritory = HttpClientUtils.getInstance().doPostWithJson(centerUrlTerritory, headParamsTerritory, jsonObjectServerTerritory);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoTerritory);

                        logger.info("Start send team value to server.....");
                        String retInfoTeam = "";
                        JSONObject jsonObjectServerTeam = (JSONObject) JSONObject.toJSON(serverTeam);
                        logger.info("jsonarray:   "+ jsonObjectServerTeam);
                        String centerUrlTeam = serverUrl+localUploadServerTeam;
                        Map<String, String> headParamsTeam = new HashMap<>();
                        headParamsTeam.put("Content-type", "application/json; charset=utf-8");
                        headParamsTeam.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoTeam = HttpClientUtils.getInstance().doPostWithJson(centerUrlTeam, headParamsTeam, jsonObjectServerTeam);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoTeam);


                        logger.info("Start send pit value to server.....");
                        String retInfoPit = "";
                        JSONObject jsonObjectServerPit = (JSONObject) JSONObject.toJSON(serverPit);
                        logger.info("jsonarray:   "+ jsonObjectServerPit);
                        String centerUrlPit = serverUrl+localUploadServerPit;
                        Map<String, String> headParamsPit = new HashMap<>();
                        headParamsPit.put("Content-type", "application/json; charset=utf-8");
                        headParamsPit.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoPit = HttpClientUtils.getInstance().doPostWithJson(centerUrlPit, headParamsPit, jsonObjectServerPit);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoPit);

                        logger.info("Start send row value to server.....");
                        String retInfoRow = "";
                        JSONObject jsonObjectServerRow = (JSONObject) JSONObject.toJSON(serverRow);
                        logger.info("jsonarray:   "+ jsonObjectServerRow);
                        String centerUrlRow = serverUrl+localUploadServerRow;
                        Map<String, String> headParamsRow = new HashMap<>();
                        headParamsRow.put("Content-type", "application/json; charset=utf-8");
                        headParamsRow.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoRow = HttpClientUtils.getInstance().doPostWithJson(centerUrlRow, headParamsRow, jsonObjectServerRow);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoRow);

                        logger.info("Start send cellar value to server.....");
                        String retInfoCellar = "";
                        JSONObject jsonObjectServerCellar = (JSONObject) JSONObject.toJSON(serverCellar);
                        logger.info("jsonarray:   "+ jsonObjectServerCellar);
                        String centerUrlCellar = serverUrl+localUploadServerCellar;
                        Map<String, String> headParamsCellar = new HashMap<>();
                        headParamsCellar.put("Content-type", "application/json; charset=utf-8");
                        headParamsCellar.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoCellar = HttpClientUtils.getInstance().doPostWithJson(centerUrlCellar, headParamsCellar, jsonObjectServerCellar);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoCellar);

                        logger.info("Start send sensor value to server.....");
                        String retInfoSensor = "";
                        JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(serverSensor);
                        logger.info("jsonarray:   "+ jsonObjectServerSensor);
                        String centerUrlSensor = serverUrl+localUploadServerSensor;
                        Map<String, String> headParamsSensor = new HashMap<>();
                        headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                        headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                        retString = "异垮信息录入成功.";
                        logger.info(retString);
                        return retString;
                    }
                }else {
                    Team team1 = new Team();
                    team1.setTeamId(HttpClientUtils.getUUID());
                    team1.setTeamCode(teamCode);
                    team1.setTerritory(territory);
                    teamService.save(team1);

                    ServerTeam serverTeam = new ServerTeam();
                    serverTeam.setTeamId(HttpClientUtils.getUUID());
                    serverTeam.setTeamCode(teamCode);
                    serverTeam.setServerTerritory(serverTerritory);
                    serverTeam.setLocalTeamId(team1.getTeamId());
                    serverTeam.setServerPits(null);

                    Pit pit1 = new Pit();
                    pit1.setPitId(HttpClientUtils.getUUID());
                    pit1.setPitCode(pitCode);
                    pit1.setTeam(team1);
                    pitService.save(pit1);

                    ServerPit serverPit = new ServerPit();
                    serverPit.setPitId(HttpClientUtils.getUUID());
                    serverPit.setPitCode(pit1.getPitCode());
                    serverPit.setServerTeam(serverTeam);
                    serverPit.setLocalPitId(pit1.getPitId());
                    serverPit.setServerRows(null);

                    Row row1 = new Row();
                    row1.setRowId(HttpClientUtils.getUUID());
                    row1.setRowCode(rowCode);
                    row1.setPit(pit1);
                    rowService.save(row1);

                    ServerRow serverRow = new ServerRow();
                    serverRow.setRowId(HttpClientUtils.getUUID());
                    serverRow.setRowCode(rowCode);
                    serverRow.setServerPit(serverPit);
                    serverRow.setLocalRowId(row1.getRowId());
                    serverRow.setServerCellars(null);

                    Cellar cellar1 = new Cellar();
                    cellar1.setRow(row1);
                    cellar1.setCellarId(HttpClientUtils.getUUID());
                    cellar1.setCellarCode(cellarCode);
                    cellarService.save(cellar1);

                    ServerCellar serverCellar = new ServerCellar();
                    serverCellar.setCellarId(HttpClientUtils.getUUID());
                    serverCellar.setCellarCode(cellarCode);
                    serverCellar.setServerRow(serverRow);
                    serverCellar.setLocalCellarId(cellar1.getCellarId());
                    serverCellar.setServerSensor(null);

                    Sensor sensor = new Sensor();
                    sensor.setSensorId(HttpClientUtils.getUUID());
                    sensor.setSensorCode(sensorCode);
                    sensor.setFrequencyPoint(frequencyPoint);
                    sensor.setWorkRate(workRate);
                    sensor.setActivatePeriod(activatePeriod);
                    sensor.setWorkingHours(workingHours);
                    sensor.setBatteryState(batteryState1);
                    sensor.setCreatedTime(new Date());
                    sensor.setCellar(cellar1);
                    sensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    if (workingType.equals("SENSOR_IS_DELETE"))
                    {
                        sensorService.update(sensor);
                    }else {
                        sensorService.save(sensor);
                    }

                    ServerSensor serverSensor = new ServerSensor();
                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                    serverSensor.setSensorCode(sensorCode);
                    serverSensor.setFrequencyPoint(frequencyPoint);
                    serverSensor.setWorkRate(workRate);
                    serverSensor.setActivatePeriod(activatePeriod);
                    serverSensor.setWorkingHours(workingHours);
                    serverCellar.setServerRow(null);
                    serverSensor.setServerCellar(serverCellar);
                    serverSensor.setBatteryState(batteryState1);
                    serverSensor.setCreatedTime(new Date());
                    serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    serverSensor.setLocalSensorId(sensor.getSensorId());
                    serverSensor.setSensorNodeSet(null);
                    if(workingType.equals("SENSOR_IS_DELETE")){
                        serverSensor.setSavingType("update");
                    }else {
                        serverSensor.setSavingType("save");
                    }

                    logger.info("Start send territory value to server.....");
                    String retInfoTerritory = "";
                    JSONObject jsonObjectServerTerritory = (JSONObject) JSONObject.toJSON(serverTerritory);
                    logger.info("jsonarray:   "+ jsonObjectServerTerritory);
                    String centerUrlTerritory = serverUrl+localUploadServerTerritory;
                    Map<String, String> headParamsTerritory = new HashMap<>();
                    headParamsTerritory.put("Content-type", "application/json; charset=utf-8");
                    headParamsTerritory.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoTerritory = HttpClientUtils.getInstance().doPostWithJson(centerUrlTerritory, headParamsTerritory, jsonObjectServerTerritory);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoTerritory);

                    logger.info("Start send team value to server.....");
                    String retInfoTeam = "";
                    JSONObject jsonObjectServerTeam = (JSONObject) JSONObject.toJSON(serverTeam);
                    logger.info("jsonarray:   "+ jsonObjectServerTeam);
                    String centerUrlTeam = serverUrl+localUploadServerTeam;
                    Map<String, String> headParamsTeam = new HashMap<>();
                    headParamsTeam.put("Content-type", "application/json; charset=utf-8");
                    headParamsTeam.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoTeam = HttpClientUtils.getInstance().doPostWithJson(centerUrlTeam, headParamsTeam, jsonObjectServerTeam);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoTeam);


                    logger.info("Start send pit value to server.....");
                    String retInfoPit = "";
                    JSONObject jsonObjectServerPit = (JSONObject) JSONObject.toJSON(serverPit);
                    logger.info("jsonarray:   "+ jsonObjectServerPit);
                    String centerUrlPit = serverUrl+localUploadServerPit;
                    Map<String, String> headParamsPit = new HashMap<>();
                    headParamsPit.put("Content-type", "application/json; charset=utf-8");
                    headParamsPit.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoPit = HttpClientUtils.getInstance().doPostWithJson(centerUrlPit, headParamsPit, jsonObjectServerPit);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoPit);

                    logger.info("Start send row value to server.....");
                    String retInfoRow = "";
                    JSONObject jsonObjectServerRow = (JSONObject) JSONObject.toJSON(serverRow);
                    logger.info("jsonarray:   "+ jsonObjectServerRow);
                    String centerUrlRow = serverUrl+localUploadServerRow;
                    Map<String, String> headParamsRow = new HashMap<>();
                    headParamsRow.put("Content-type", "application/json; charset=utf-8");
                    headParamsRow.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoRow = HttpClientUtils.getInstance().doPostWithJson(centerUrlRow, headParamsRow, jsonObjectServerRow);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoRow);

                    logger.info("Start send cellar value to server.....");
                    String retInfoCellar = "";
                    JSONObject jsonObjectServerCellar = (JSONObject) JSONObject.toJSON(serverCellar);
                    logger.info("jsonarray:   "+ jsonObjectServerCellar);
                    String centerUrlCellar = serverUrl+localUploadServerCellar;
                    Map<String, String> headParamsCellar = new HashMap<>();
                    headParamsCellar.put("Content-type", "application/json; charset=utf-8");
                    headParamsCellar.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoCellar = HttpClientUtils.getInstance().doPostWithJson(centerUrlCellar, headParamsCellar, jsonObjectServerCellar);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoCellar);

                    logger.info("Start send sensor value to server.....");
                    String retInfoSensor = "";
                    JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(serverSensor);
                    logger.info("jsonarray:   "+ jsonObjectServerSensor);
                    String centerUrlSensor = serverUrl+localUploadServerSensor;
                    Map<String, String> headParamsSensor = new HashMap<>();
                    headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                    headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);
                    retString = "异班组信息录入成功.";
                    logger.info(retString);
                    return retString;
                }
            }else {
                Territory territory1 = new Territory();
                territory1.setTerritoryId(HttpClientUtils.getUUID());
                territory1.setTerritoryCode(territoryCode);
                territory1.setPlant(plant);
                territoryService.save(territory1);

                ServerTerritory serverTerritory = new ServerTerritory();
                serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                serverTerritory.setTerritoryCode(territoryCode);
                serverTerritory.setServerPlant(serverPlant);
                serverTerritory.setLocalTerritoryId(territory1.getTerritoryId());
                serverTerritory.setServerTeams(null);

                Team team1 = new Team();
                team1.setTeamId(HttpClientUtils.getUUID());
                team1.setTeamCode(teamCode);
                team1.setTerritory(territory1);
                teamService.save(team1);

                ServerTeam serverTeam = new ServerTeam();
                serverTeam.setTeamId(HttpClientUtils.getUUID());
                serverTeam.setTeamCode(teamCode);
                serverTeam.setServerTerritory(serverTerritory);
                serverTeam.setLocalTeamId(team1.getTeamId());
                serverTeam.setServerPits(null);

                Pit pit1 = new Pit();
                pit1.setPitId(HttpClientUtils.getUUID());
                pit1.setPitCode(pitCode);
                pit1.setTeam(team1);
                pitService.save(pit1);

                ServerPit serverPit = new ServerPit();
                serverPit.setPitId(HttpClientUtils.getUUID());
                serverPit.setPitCode(pit1.getPitCode());
                serverPit.setServerTeam(serverTeam);
                serverPit.setLocalPitId(pit1.getPitId());
                serverPit.setServerRows(null);

                Row row1 = new Row();
                row1.setRowId(HttpClientUtils.getUUID());
                row1.setRowCode(rowCode);
                row1.setPit(pit1);
                rowService.save(row1);

                ServerRow serverRow = new ServerRow();
                serverRow.setRowId(HttpClientUtils.getUUID());
                serverRow.setRowCode(rowCode);
                serverRow.setServerPit(serverPit);
                serverRow.setLocalRowId(row1.getRowId());
                serverRow.setServerCellars(null);

                Cellar cellar1 = new Cellar();
                cellar1.setRow(row1);
                cellar1.setCellarId(HttpClientUtils.getUUID());
                cellar1.setCellarCode(cellarCode);
                cellarService.save(cellar1);

                ServerCellar serverCellar = new ServerCellar();
                serverCellar.setCellarId(HttpClientUtils.getUUID());
                serverCellar.setCellarCode(cellarCode);
                serverCellar.setServerRow(serverRow);
                serverCellar.setLocalCellarId(cellar1.getCellarId());
                serverCellar.setServerSensor(null);

                Sensor sensor = new Sensor();
                sensor.setSensorId(HttpClientUtils.getUUID());
                sensor.setSensorCode(sensorCode);
                sensor.setFrequencyPoint(frequencyPoint);
                sensor.setWorkRate(workRate);
                sensor.setActivatePeriod(activatePeriod);
                sensor.setWorkingHours(workingHours);
                sensor.setBatteryState(batteryState1);
                sensor.setCreatedTime(new Date());
                sensor.setCellar(cellar1);
                sensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                if (workingType.equals("SENSOR_IS_DELETE"))
                {
                    sensorService.update(sensor);
                }else {
                    sensorService.save(sensor);
                }

                ServerSensor serverSensor = new ServerSensor();
                serverSensor.setSensorId(HttpClientUtils.getUUID());
                serverSensor.setSensorCode(sensorCode);
                serverSensor.setFrequencyPoint(frequencyPoint);
                serverSensor.setWorkRate(workRate);
                serverSensor.setActivatePeriod(activatePeriod);
                serverSensor.setWorkingHours(workingHours);
                serverCellar.setServerRow(null);
                serverSensor.setServerCellar(serverCellar);
                serverSensor.setBatteryState(batteryState1);
                serverSensor.setCreatedTime(new Date());
                serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                serverSensor.setLocalSensorId(sensor.getSensorId());
                serverSensor.setSensorNodeSet(null);
                if(workingType.equals("SENSOR_IS_DELETE")){
                    serverSensor.setSavingType("update");
                }else {
                    serverSensor.setSavingType("save");
                }

                logger.info("Start send territory value to server.....");
                String retInfoTerritory = "";
                JSONObject jsonObjectServerTerritory = (JSONObject) JSONObject.toJSON(serverTerritory);
                logger.info("jsonarray:   "+ jsonObjectServerTerritory);
                String centerUrlTerritory = serverUrl+localUploadServerTerritory;
                Map<String, String> headParamsTerritory = new HashMap<>();
                headParamsTerritory.put("Content-type", "application/json; charset=utf-8");
                headParamsTerritory.put("SessionId", HttpClientUtils.getSessionId());
                retInfoTerritory = HttpClientUtils.getInstance().doPostWithJson(centerUrlTerritory, headParamsTerritory, jsonObjectServerTerritory);
                logger.info(">>>>>>>>>>Upload state: " + retInfoTerritory);

                logger.info("Start send team value to server.....");
                String retInfoTeam = "";
                JSONObject jsonObjectServerTeam = (JSONObject) JSONObject.toJSON(serverTeam);
                logger.info("jsonarray:   "+ jsonObjectServerTeam);
                String centerUrlTeam = serverUrl+localUploadServerTeam;
                Map<String, String> headParamsTeam = new HashMap<>();
                headParamsTeam.put("Content-type", "application/json; charset=utf-8");
                headParamsTeam.put("SessionId", HttpClientUtils.getSessionId());
                retInfoTeam = HttpClientUtils.getInstance().doPostWithJson(centerUrlTeam, headParamsTeam, jsonObjectServerTeam);
                logger.info(">>>>>>>>>>Upload state: " + retInfoTeam);


                logger.info("Start send pit value to server.....");
                String retInfoPit = "";
                JSONObject jsonObjectServerPit = (JSONObject) JSONObject.toJSON(serverPit);
                logger.info("jsonarray:   "+ jsonObjectServerPit);
                String centerUrlPit = serverUrl+localUploadServerPit;
                Map<String, String> headParamsPit = new HashMap<>();
                headParamsPit.put("Content-type", "application/json; charset=utf-8");
                headParamsPit.put("SessionId", HttpClientUtils.getSessionId());
                retInfoPit = HttpClientUtils.getInstance().doPostWithJson(centerUrlPit, headParamsPit, jsonObjectServerPit);
                logger.info(">>>>>>>>>>Upload state: " + retInfoPit);

                logger.info("Start send row value to server.....");
                String retInfoRow = "";
                JSONObject jsonObjectServerRow = (JSONObject) JSONObject.toJSON(serverRow);
                logger.info("jsonarray:   "+ jsonObjectServerRow);
                String centerUrlRow = serverUrl+localUploadServerRow;
                Map<String, String> headParamsRow = new HashMap<>();
                headParamsRow.put("Content-type", "application/json; charset=utf-8");
                headParamsRow.put("SessionId", HttpClientUtils.getSessionId());
                retInfoRow = HttpClientUtils.getInstance().doPostWithJson(centerUrlRow, headParamsRow, jsonObjectServerRow);
                logger.info(">>>>>>>>>>Upload state: " + retInfoRow);

                logger.info("Start send cellar value to server.....");
                String retInfoCellar = "";
                JSONObject jsonObjectServerCellar = (JSONObject) JSONObject.toJSON(serverCellar);
                logger.info("jsonarray:   "+ jsonObjectServerCellar);
                String centerUrlCellar = serverUrl+localUploadServerCellar;
                Map<String, String> headParamsCellar = new HashMap<>();
                headParamsCellar.put("Content-type", "application/json; charset=utf-8");
                headParamsCellar.put("SessionId", HttpClientUtils.getSessionId());
                retInfoCellar = HttpClientUtils.getInstance().doPostWithJson(centerUrlCellar, headParamsCellar, jsonObjectServerCellar);
                logger.info(">>>>>>>>>>Upload state: " + retInfoCellar);

                logger.info("Start send sensor value to server.....");
                String retInfoSensor = "";
                JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(serverSensor);
                logger.info("jsonarray:   "+ jsonObjectServerSensor);
                String centerUrlSensor = serverUrl+localUploadServerSensor;
                Map<String, String> headParamsSensor = new HashMap<>();
                headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                retString = "异责任区信息录入成功.";
                logger.info(retString);
                return retString;
            }
        }catch (Exception e){
            e.printStackTrace();
            retString = "新增设备异常，异常信息: " + e.toString();
            logger.info(retString);
            return retString;
        }
    }

    private String getSensorInfo(String sensorCode, String plantId) {
        String retString = "";
        String workingType = "";
        final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
        final String rsPartitionSendCode = "00"; //分割发送码
        final String rsSufMessagefix = "AB031000000F1904"; //信息层
        final String rsSuffix = "CCCC";
        String strMessage = rsPrefix + sensorCode + rsPartitionSendCode + rsSufMessagefix + rsSuffix;

        Plant plant = plantService.find(plantId);
//        strMessage = "7E380FB08FD08CC72300AB031000000F1904CCCC";
        byte[] byteMessage = HexUtils.hexStringToByte(strMessage.trim());
        commThread.write(byteMessage);
        logger.info("Rs发送数据：" + HexUtils.bytesToHexString(byteMessage));
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
                logger.info("sen occur a problem, try to save a zero value");
                retString = "当前录入设备异常，请处理后录入.（可能原因：1.电量不足。2.设备故障。）";
                return retString;
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

            String retSensorCode = HexUtils.bytesToHexString(terminalId);
            int plantCode = Integer.parseInt(HexUtils.bytesToHexString(workShopId),16);
            int territoryCode = Integer.parseInt(HexUtils.bytesToHexString(turnOutAreaId),16);
            int teamCode = Integer.parseInt(HexUtils.bytesToHexString(teamId),16);
            int pitCode = Integer.parseInt(HexUtils.bytesToHexString(collapseId),16);
            int rowCode = Integer.parseInt(HexUtils.bytesToHexString(rowId),16);
            int cellarCode = Integer.parseInt(HexUtils.bytesToHexString(cellarId),16);
            int frequencyPoint = Integer.parseInt(HexUtils.bytesToHexString(frenquencePointId),16);
            int workRate = Integer.parseInt(HexUtils.bytesToHexString(workRateId),16);
            int activatePeriod = Integer.parseInt(HexUtils.bytesToHexString(periodId),16);
            int workingHours = Integer.parseInt(HexUtils.bytesToHexString(workTimeId),16);
            String batteryState1 = HexUtils.bytesToHexString(batteryState);
            double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16))*1.0/10;
            logger.error("The topTemp is [{}]", topTemp1);
            double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp),16))*1.0/10;
            logger.error("The mideTemp is [{}]", midTemp1);
            double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp),16))*1.0/10;
            logger.error("The botTemp is [{}]", botTemp1);
            List<Sensor> sensors = sensorService.getScrollData().getResultList();
            LocalToServerSensorAdding sensorAdding = new LocalToServerSensorAdding();
            if(sensors!=null){
                for (Sensor sensor: sensors){
                    //特征码相同且设备启用 返回错误
                    if(sensor.getSensorCode().equals(sensorCode)){
                        if(!sensor.getSensorWorkingType().getDescription().equals("SENSOR_IS_DELETE"))
                        {
                            retString = "特征码已存在";
                            logger.info(retString);
                            return retString;
                        }else {
                            workingType = "delete";
                            retString = "1已删除设备重录.";
                            logger.info(retString);
                        }
                    }
                }
            }
            if(plantCode != plant.getPlantCode()){
                retString = "本设备不属于 " + plant.getPlantName() + " , 请确认后录入。";
                logger.info(retString);
                return retString;
            } else
            {

                logger.error("车间编码相同，开始录入......");
                //根据前端键入值保存责任区和班组.
                logger.error("开始比较责任区班组信息......");
                Territory territory = territoryService.getTerritoryByPlantAndTerritoryCode(plant, territoryCode);

                if(territory != null){
                    Team team = teamService.getTeamsByTerritoryAndTeamCode(territory, teamCode);
                    if(team != null)
                    {
                        //plant与当前打开页面session中的plant作对比
                        Pit pit = pitService.getPitByTeamAndPitCode(team, pitCode);
                        if (pit != null){
                            //不为空说明pit值重复，则进入下一步 Row 校验
                            logger.info("垮号已存在，进入该垮号，进行 Row 比较......");
                            Row row = rowService.getRowByPitAndRowCode(pit, rowCode);
                            if(row != null){
                                logger.info("排号已存在，进入该垮号，进行 cellar 比较......");
                                Cellar cellar = cellarService.getCellarByRowAndCellarCode(row, cellarCode);
                                if(cellar != null && workingType.equals("delete")){
                                    retString = "窖号已存在，发生错误......";
                                    logger.info(retString);
                                    return retString;
                                }else {
                                    ServerPlant serverPlant = new ServerPlant();
                                    serverPlant.setPlantCode(plant.getPlantCode());
                                    serverPlant.setLocalPlantId(plant.getPlantId());
                                    sensorAdding.setServerPlant(serverPlant);

                                    ServerTerritory serverTerritory = new ServerTerritory();
                                    serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                                    serverTerritory.setTerritoryCode(territoryCode);
                                    serverTerritory.setLocalTerritoryId(territory.getTerritoryId());
                                    sensorAdding.setServerTerritory(serverTerritory);

                                    ServerTeam serverTeam = new ServerTeam();
                                    serverTeam.setTeamId(HttpClientUtils.getUUID());
                                    serverTeam.setTeamCode(teamCode);
                                    serverTeam.setLocalTeamId(team.getTeamId());
                                    sensorAdding.setServerTeam(serverTeam);

                                    ServerPit serverPit = new ServerPit();
                                    serverPit.setPitId(HttpClientUtils.getUUID());
                                    serverPit.setPitCode(pit.getPitCode());
                                    serverPit.setLocalPitId(pit.getPitId());
                                    sensorAdding.setServerPit(serverPit);

                                    ServerRow serverRow = new ServerRow();
                                    serverRow.setRowId(HttpClientUtils.getUUID());
                                    serverRow.setRowCode(rowCode);
                                    serverRow.setLocalRowId(row.getRowId());
                                    sensorAdding.setServerRow(serverRow);

                                    Cellar cellar1 = new Cellar();
                                    cellar1.setCellarId(HttpClientUtils.getUUID());
                                    cellar1.setCellarCode(cellarCode);
                                    cellar1.setRow(row);
                                    cellarService.save(cellar1);

                                    ServerCellar serverCellar = new ServerCellar();
                                    serverCellar.setCellarId(HttpClientUtils.getUUID());
                                    serverCellar.setCellarCode(cellarCode);
                                    serverCellar.setLocalCellarId(cellar1.getCellarId());
                                    sensorAdding.setServerCellar(serverCellar);

                                    Sensor sensor1 = new Sensor();
                                    sensor1.setSensorId(HttpClientUtils.getUUID());
                                    sensor1.setSensorCode(sensorCode);
                                    sensor1.setFrequencyPoint(frequencyPoint);
                                    sensor1.setWorkRate(workRate);
                                    sensor1.setActivatePeriod(activatePeriod);
                                    sensor1.setWorkingHours(workingHours);
                                    sensor1.setCellar(cellar1);
                                    sensor1.setBatteryState(batteryState1);
                                    sensor1.setCreatedTime(new Date());
                                    sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                    sensorService.save(sensor1);

                                    ServerSensor serverSensor = new ServerSensor();
                                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                                    serverSensor.setFrequencyPoint(frequencyPoint);
                                    serverSensor.setWorkingHours(workingHours);
                                    serverSensor.setWorkRate(workRate);
                                    serverSensor.setActivatePeriod(activatePeriod);
                                    serverSensor.setSensorCode(sensorCode);
                                    serverSensor.setBatteryState(batteryState1);
                                    serverSensor.setLocalSensorId(sensor1.getSensorId());
                                    sensorAdding.setServerSensor(serverSensor);

                                    logger.info("Start send sensor value to server.....");
                                    String retInfoSensor = "";
                                    JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                                    logger.info("jsonarray:   "+ jsonObjectServerSensor);
                                    String centerUrlSensor = serverUrl+localUploadServerValue;
                                    Map<String, String> headParamsSensor = new HashMap<>();
                                    headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                                    headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                                    retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                                    logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                                    retString = "异窖信息录入成功.";
                                    logger.info(retString);
                                    return retString;
                                }
                            } else {
                                ServerPlant serverPlant = new ServerPlant();
                                serverPlant.setPlantCode(plant.getPlantCode());
                                serverPlant.setLocalPlantId(plant.getPlantId());
                                sensorAdding.setServerPlant(serverPlant);

                                ServerTerritory serverTerritory = new ServerTerritory();
                                serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                                serverTerritory.setTerritoryCode(territoryCode);
                                serverTerritory.setLocalTerritoryId(territory.getTerritoryId());
                                sensorAdding.setServerTerritory(serverTerritory);

                                ServerTeam serverTeam = new ServerTeam();
                                serverTeam.setTeamId(HttpClientUtils.getUUID());
                                serverTeam.setTeamCode(teamCode);
                                serverTeam.setLocalTeamId(team.getTeamId());
                                sensorAdding.setServerTeam(serverTeam);

                                ServerPit serverPit = new ServerPit();
                                serverPit.setPitId(HttpClientUtils.getUUID());
                                serverPit.setPitCode(pit.getPitCode());
                                serverPit.setLocalPitId(pit.getPitId());
                                sensorAdding.setServerPit(serverPit);

                                Row row1 = new Row();
                                row1.setPit(pit);
                                row1.setRowId(HttpClientUtils.getUUID());
                                row1.setRowCode(rowCode);
                                rowService.save(row1);

                                ServerRow serverRow = new ServerRow();
                                serverRow.setRowId(HttpClientUtils.getUUID());
                                serverRow.setRowCode(rowCode);
                                serverRow.setLocalRowId(row1.getRowId());
                                sensorAdding.setServerRow(serverRow);

                                Cellar cellar1 = new Cellar();
                                cellar1.setCellarId(HttpClientUtils.getUUID());
                                cellar1.setCellarCode(cellarCode);
                                cellar1.setRow(row1);
                                cellarService.save(cellar1);

                                ServerCellar serverCellar = new ServerCellar();
                                serverCellar.setCellarId(HttpClientUtils.getUUID());
                                serverCellar.setCellarCode(cellarCode);
                                serverCellar.setLocalCellarId(cellar1.getCellarId());
                                sensorAdding.setServerCellar(serverCellar);

                                Sensor sensor1 = new Sensor();
                                sensor1.setSensorId(HttpClientUtils.getUUID());
                                sensor1.setSensorCode(sensorCode);
                                sensor1.setFrequencyPoint(frequencyPoint);
                                sensor1.setWorkRate(workRate);
                                sensor1.setActivatePeriod(activatePeriod);
                                sensor1.setWorkingHours(workingHours);
                                sensor1.setCellar(cellar1);
                                sensor1.setBatteryState(batteryState1);
                                sensor1.setCreatedTime(new Date());
                                sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                sensorService.save(sensor1);

                                ServerSensor serverSensor = new ServerSensor();
                                serverSensor.setSensorId(HttpClientUtils.getUUID());
                                serverSensor.setFrequencyPoint(frequencyPoint);
                                serverSensor.setWorkingHours(workingHours);
                                serverSensor.setWorkRate(workRate);
                                serverSensor.setActivatePeriod(activatePeriod);
                                serverSensor.setSensorCode(sensorCode);
                                serverSensor.setBatteryState(batteryState1);
                                serverSensor.setLocalSensorId(sensor1.getSensorId());
                                sensorAdding.setServerSensor(serverSensor);

                                logger.info("Start send sensor value to server.....");
                                String retInfoSensor = "";
                                JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                                logger.info("jsonarray:   "+ jsonObjectServerSensor);
                                String centerUrlSensor = serverUrl+localUploadServerValue;
                                Map<String, String> headParamsSensor = new HashMap<>();
                                headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                                headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                                retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                                logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                                retString = "异排信息录入成功.";
                                logger.info(retString);
                                return retString;

                            }
                        } else{
                            ServerPlant serverPlant = new ServerPlant();
                            serverPlant.setPlantCode(plant.getPlantCode());
                            serverPlant.setLocalPlantId(plant.getPlantId());
                            sensorAdding.setServerPlant(serverPlant);

                            ServerTerritory serverTerritory = new ServerTerritory();
                            serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                            serverTerritory.setTerritoryCode(territoryCode);
                            serverTerritory.setLocalTerritoryId(territory.getTerritoryId());
                            sensorAdding.setServerTerritory(serverTerritory);

                            ServerTeam serverTeam = new ServerTeam();
                            serverTeam.setTeamId(HttpClientUtils.getUUID());
                            serverTeam.setTeamCode(teamCode);
                            serverTeam.setLocalTeamId(team.getTeamId());
                            sensorAdding.setServerTeam(serverTeam);

                            Pit pit1 = new Pit();
                            pit1.setPitId(HttpClientUtils.getUUID());
                            pit1.setPitCode(pitCode);
                            pit1.setTeam(team);
                            pitService.save(pit1);

                            ServerPit serverPit = new ServerPit();
                            serverPit.setPitId(HttpClientUtils.getUUID());
                            serverPit.setPitCode(pit1.getPitCode());
                            serverPit.setLocalPitId(pit1.getPitId());
                            sensorAdding.setServerPit(serverPit);

                            Row row1 = new Row();
                            row1.setPit(pit1);
                            row1.setRowId(HttpClientUtils.getUUID());
                            row1.setRowCode(rowCode);
                            rowService.save(row1);

                            ServerRow serverRow = new ServerRow();
                            serverRow.setRowId(HttpClientUtils.getUUID());
                            serverRow.setRowCode(rowCode);
                            serverRow.setLocalRowId(row1.getRowId());
                            sensorAdding.setServerRow(serverRow);

                            Cellar cellar1 = new Cellar();
                            cellar1.setCellarId(HttpClientUtils.getUUID());
                            cellar1.setCellarCode(cellarCode);
                            cellar1.setRow(row1);
                            cellarService.save(cellar1);

                            ServerCellar serverCellar = new ServerCellar();
                            serverCellar.setCellarId(HttpClientUtils.getUUID());
                            serverCellar.setCellarCode(cellarCode);
                            serverCellar.setLocalCellarId(cellar1.getCellarId());
                            sensorAdding.setServerCellar(serverCellar);

                            Sensor sensor1 = new Sensor();
                            sensor1.setSensorId(HttpClientUtils.getUUID());
                            sensor1.setSensorCode(sensorCode);
                            sensor1.setFrequencyPoint(frequencyPoint);
                            sensor1.setWorkRate(workRate);
                            sensor1.setActivatePeriod(activatePeriod);
                            sensor1.setWorkingHours(workingHours);
                            sensor1.setCellar(cellar1);
                            sensor1.setBatteryState(batteryState1);
                            sensor1.setCreatedTime(new Date());
                            sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                            sensorService.save(sensor1);

                            ServerSensor serverSensor = new ServerSensor();
                            serverSensor.setSensorId(HttpClientUtils.getUUID());
                            serverSensor.setFrequencyPoint(frequencyPoint);
                            serverSensor.setWorkingHours(workingHours);
                            serverSensor.setWorkRate(workRate);
                            serverSensor.setActivatePeriod(activatePeriod);
                            serverSensor.setSensorCode(sensorCode);
                            serverSensor.setBatteryState(batteryState1);
                            serverSensor.setLocalSensorId(sensor1.getSensorId());
                            sensorAdding.setServerSensor(serverSensor);

                            logger.info("Start send sensor value to server.....");
                            String retInfoSensor = "";
                            JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                            logger.info("jsonarray:   "+ jsonObjectServerSensor);
                            String centerUrlSensor = serverUrl+localUploadServerValue;
                            Map<String, String> headParamsSensor = new HashMap<>();
                            headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                            headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                            retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                            logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                            retString = "异垮信息录入成功.";
                            logger.info(retString);
                            return retString;
                        }


                    }else {
                        ServerPlant serverPlant = new ServerPlant();
                        serverPlant.setPlantCode(plant.getPlantCode());
                        serverPlant.setLocalPlantId(plant.getPlantId());
                        sensorAdding.setServerPlant(serverPlant);

                        ServerTerritory serverTerritory = new ServerTerritory();
                        serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                        serverTerritory.setTerritoryCode(territoryCode);
                        serverTerritory.setLocalTerritoryId(territory.getTerritoryId());
                        sensorAdding.setServerTerritory(serverTerritory);

                        Team team1 = new Team();
                        team1.setTerritory(territory);
                        team1.setTeamCode(teamCode);
                        team1.setTeamId(HttpClientUtils.getUUID());
                        teamService.save(team1);

                        ServerTeam serverTeam = new ServerTeam();
                        serverTeam.setTeamId(HttpClientUtils.getUUID());
                        serverTeam.setTeamCode(teamCode);
                        serverTeam.setLocalTeamId(team1.getTeamId());
                        sensorAdding.setServerTeam(serverTeam);

                        Pit pit1 = new Pit();
                        pit1.setPitId(HttpClientUtils.getUUID());
                        pit1.setPitCode(pitCode);
                        pit1.setTeam(team1);
                        pitService.save(pit1);

                        ServerPit serverPit = new ServerPit();
                        serverPit.setPitId(HttpClientUtils.getUUID());
                        serverPit.setPitCode(pit1.getPitCode());
                        serverPit.setLocalPitId(pit1.getPitId());
                        sensorAdding.setServerPit(serverPit);

                        Row row1 = new Row();
                        row1.setPit(pit1);
                        row1.setRowId(HttpClientUtils.getUUID());
                        row1.setRowCode(rowCode);
                        rowService.save(row1);

                        ServerRow serverRow = new ServerRow();
                        serverRow.setRowId(HttpClientUtils.getUUID());
                        serverRow.setRowCode(rowCode);
                        serverRow.setLocalRowId(row1.getRowId());
                        sensorAdding.setServerRow(serverRow);

                        Cellar cellar1 = new Cellar();
                        cellar1.setCellarId(HttpClientUtils.getUUID());
                        cellar1.setCellarCode(cellarCode);
                        cellar1.setRow(row1);
                        cellarService.save(cellar1);

                        ServerCellar serverCellar = new ServerCellar();
                        serverCellar.setCellarId(HttpClientUtils.getUUID());
                        serverCellar.setCellarCode(cellarCode);
                        serverCellar.setLocalCellarId(cellar1.getCellarId());
                        sensorAdding.setServerCellar(serverCellar);

                        Sensor sensor1 = new Sensor();
                        sensor1.setSensorId(HttpClientUtils.getUUID());
                        sensor1.setSensorCode(sensorCode);
                        sensor1.setFrequencyPoint(frequencyPoint);
                        sensor1.setWorkRate(workRate);
                        sensor1.setActivatePeriod(activatePeriod);
                        sensor1.setWorkingHours(workingHours);
                        sensor1.setCellar(cellar1);
                        sensor1.setBatteryState(batteryState1);
                        sensor1.setCreatedTime(new Date());
                        sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                        sensorService.save(sensor1);

                        ServerSensor serverSensor = new ServerSensor();
                        serverSensor.setSensorId(HttpClientUtils.getUUID());
                        serverSensor.setFrequencyPoint(frequencyPoint);
                        serverSensor.setWorkingHours(workingHours);
                        serverSensor.setWorkRate(workRate);
                        serverSensor.setActivatePeriod(activatePeriod);
                        serverSensor.setSensorCode(sensorCode);
                        serverSensor.setBatteryState(batteryState1);
                        serverSensor.setLocalSensorId(sensor1.getSensorId());
                        sensorAdding.setServerSensor(serverSensor);

                        logger.info("Start send sensor value to server.....");
                        String retInfoSensor = "";
                        JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                        logger.info("jsonarray:   "+ jsonObjectServerSensor);
                        String centerUrlSensor = serverUrl+localUploadServerValue;
                        Map<String, String> headParamsSensor = new HashMap<>();
                        headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                        headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                        retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                        logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                        retString = "异班组信息录入成功.";
                        logger.info(retString);
                        return retString;
                    }
                }else {
                    ServerPlant serverPlant = new ServerPlant();
                    serverPlant.setPlantCode(plant.getPlantCode());
                    serverPlant.setLocalPlantId(plant.getPlantId());
                    sensorAdding.setServerPlant(serverPlant);

                    Territory territory1 = new Territory();
                    territory1.setTerritoryId(HttpClientUtils.getUUID());
                    territory1.setTerritoryCode(territoryCode);
                    territory1.setPlant(plant);
                    territoryService.save(territory1);

                    ServerTerritory serverTerritory = new ServerTerritory();
                    serverTerritory.setTerritoryId(HttpClientUtils.getUUID());
                    serverTerritory.setTerritoryCode(territoryCode);
                    serverTerritory.setLocalTerritoryId(territory1.getTerritoryId());
                    sensorAdding.setServerTerritory(serverTerritory);

                    Team team1 = new Team();
                    team1.setTerritory(territory1);
                    team1.setTeamCode(teamCode);
                    team1.setTeamId(HttpClientUtils.getUUID());
                    teamService.save(team1);

                    ServerTeam serverTeam = new ServerTeam();
                    serverTeam.setTeamId(HttpClientUtils.getUUID());
                    serverTeam.setTeamCode(teamCode);
                    serverTeam.setLocalTeamId(team1.getTeamId());
                    sensorAdding.setServerTeam(serverTeam);

                    Pit pit1 = new Pit();
                    pit1.setPitId(HttpClientUtils.getUUID());
                    pit1.setPitCode(pitCode);
                    pit1.setTeam(team1);
                    pitService.save(pit1);

                    ServerPit serverPit = new ServerPit();
                    serverPit.setPitId(HttpClientUtils.getUUID());
                    serverPit.setPitCode(pit1.getPitCode());
                    serverPit.setLocalPitId(pit1.getPitId());
                    sensorAdding.setServerPit(serverPit);

                    Row row1 = new Row();
                    row1.setPit(pit1);
                    row1.setRowId(HttpClientUtils.getUUID());
                    row1.setRowCode(rowCode);
                    rowService.save(row1);

                    ServerRow serverRow = new ServerRow();
                    serverRow.setRowId(HttpClientUtils.getUUID());
                    serverRow.setRowCode(rowCode);
                    serverRow.setLocalRowId(row1.getRowId());
                    sensorAdding.setServerRow(serverRow);

                    Cellar cellar1 = new Cellar();
                    cellar1.setCellarId(HttpClientUtils.getUUID());
                    cellar1.setCellarCode(cellarCode);
                    cellar1.setRow(row1);
                    cellarService.save(cellar1);

                    ServerCellar serverCellar = new ServerCellar();
                    serverCellar.setCellarId(HttpClientUtils.getUUID());
                    serverCellar.setCellarCode(cellarCode);
                    serverCellar.setLocalCellarId(cellar1.getCellarId());
                    sensorAdding.setServerCellar(serverCellar);

                    Sensor sensor1 = new Sensor();
                    sensor1.setSensorId(HttpClientUtils.getUUID());
                    sensor1.setSensorCode(sensorCode);
                    sensor1.setFrequencyPoint(frequencyPoint);
                    sensor1.setWorkRate(workRate);
                    sensor1.setActivatePeriod(activatePeriod);
                    sensor1.setWorkingHours(workingHours);
                    sensor1.setCellar(cellar1);
                    sensor1.setBatteryState(batteryState1);
                    sensor1.setCreatedTime(new Date());
                    sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    sensorService.save(sensor1);

                    ServerSensor serverSensor = new ServerSensor();
                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                    serverSensor.setFrequencyPoint(frequencyPoint);
                    serverSensor.setWorkingHours(workingHours);
                    serverSensor.setWorkRate(workRate);
                    serverSensor.setActivatePeriod(activatePeriod);
                    serverSensor.setSensorCode(sensorCode);
                    serverSensor.setBatteryState(batteryState1);
                    serverSensor.setLocalSensorId(sensor1.getSensorId());
                    sensorAdding.setServerSensor(serverSensor);

                    logger.info("Start send sensor value to server.....");
                    String retInfoSensor = "";
                    JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                    logger.info("jsonarray:   "+ jsonObjectServerSensor);
                    String centerUrlSensor = serverUrl+localUploadServerValue;
                    Map<String, String> headParamsSensor = new HashMap<>();
                    headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                    headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                    retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                    logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                    retString = "异责任区信息录入成功.";
                    logger.info(retString);
                    return retString;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            retString = "新增设备异常，异常信息: " + e.toString();
            logger.info(retString);
            return retString;
        }
    }

    private String sendToServer(JSONObject jsonObject, String url){
        Map<String, String> headParams = new HashMap<>();
        headParams.put("Content-type", "application/json; charset=utf-8");
        headParams.put("SessionId", HttpClientUtils.getSessionId());
        return HttpClientUtils.getInstance().doPostWithJson(url, headParams, jsonObject);
    }
}

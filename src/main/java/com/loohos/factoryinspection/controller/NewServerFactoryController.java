package com.loohos.factoryinspection.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.model.formbean.*;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.model.server.*;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.CommonUtils;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static com.loohos.factoryinspection.enumeration.SensorType.BOT_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.MID_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.TOP_TEMP_SENSOR;

@Controller
@RequestMapping(value = "/server")
public class NewServerFactoryController {
    @Value("${com.loohos.machineType}")
    private String machineType;

    private Logger logger = LoggerFactory.getLogger(FactoryController.class);
    private SerialCommThread commThread = new SerialCommThread();
    @Resource(name = "configAlarmLevelServiceImpl") private ConfigAlarmLevelService configAlarmLevelService;
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;
    @Resource(name = "serverTerminalValueSensorServiceImpl") private ServerTerminalValueSensorService serverTerminalValueSensorService;
    @Resource(name = "plantServiceImpl") private PlantService plantService;
    @Resource(name = "territoryServiceImpl") private TerritoryService territoryService;
    @Resource(name = "teamServiceImpl") private TeamService teamService;
    @Resource(name = "pitServiceImpl") private PitService pitService;
    @Resource(name = "rowServiceImpl") private RowService rowService;
    @Resource(name = "cellarServiceImpl") private CellarService cellarService;
    @Resource(name = "sensorNodeServiceImpl") private SensorNodeService sensorNodeService;
    @Resource(name = "sensorServiceImpl") private SensorService sensorService;
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
        List<ServerPlant> plantList = serverPlantService.getScrollData().getResultList();
        //TODO 无数据的校验
        if(plantList.size() > 0){
            logger.info("into server plant map, gotta messages.");
            modelMap.put("plants", plantList);
        }else{
            logger.info("into plant map, no message gotta.");
        }
        return "winery/serverPage/serverPlantMap";
    }

    @RequestMapping(value = "/index",produces={"text/html;charset=UTF-8;","application/json;"})
    public String getIndex(ModelMap modelMap,
                           @RequestParam String plantId,
                           HttpServletRequest request,
                           HttpServletResponse response){
        ServerPlant plant = serverPlantService.find(plantId);
        modelMap.put("serverPlant", plant);
        request.getSession().setAttribute("plantId", plantId);

        List<ServerTerritory> territories = serverTerritoryService.getTerritoriesByPlant(plant);
        List<ServerPit> pits = new ArrayList<>();
        if(territories == null){
            logger.info("no territories, please add one.");
            return "winery/serverPage/serverPlantInside";
        }
        else {
            for(ServerTerritory territory: territories) {
                List<ServerTeam> teams = serverTeamService.getTeamsByTerritory(territory);
                for (ServerTeam team : teams) {
                    List<ServerPit> existedPits = serverPitService.getPitsByTeam(team);
                    if(existedPits.size() < 10){
//                        for (int i = pits.size()+1; i <= 10; i++){
//                            Pit pit = new Pit();
//                            pit.setPitCode(i);
//                            pit.setPitType("false");
//                            pits.add(pit);
//                        }
                        here:
                        for(int i = 1; i <= 10; i++){
                            for(ServerPit pit : existedPits){
                                if(pit.getPitCode() == i){
                                    logger.info("正常垮.");
                                    pits.add(pit);
                                    continue here;
                                }
                            }
                            logger.info("填充垮.");
                            ServerPit pit = new ServerPit();
                            pit.setPitCode(i);
                            pit.setPitType("false");
                            pits.add(pit);
                        }
                    }
                }
            }
            Collections.sort(pits, new Comparator<ServerPit>() {
                @Override
                public int compare(ServerPit o1, ServerPit o2) {
                    return o1.getPitCode() - o2.getPitCode();
                }
            });
            modelMap.put("serverPits", pits);
        }
        return "winery/serverPage/serverPlantInside";
    }


    @RequestMapping(value = "/showCellar",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showCellar(@ModelAttribute Pit inputData,
                             ModelMap modelMap){
        ServerPit pit = serverPitService.find(inputData.getPitId());
        modelMap.put("pit", pit);
        List<ServerRow> rows = new ArrayList<>();
        List<ServerRow> existedRows = serverRowService.getRowByPit(pit);
        if(existedRows.size() < 4){
//            for (int i = rows.size()+1; i<=4 ; i++){
//                Row row = new Row();
//                row.setRowCode(i);
//                row.setRowType("false");
//                rows.add(row);
//            }
            here:
            for (int i = 1; i <= 4 ; i++){
                for(ServerRow row : existedRows){
                    if(row.getRowCode() == i){
                        logger.info("正常排");
                        rows.add(row);
                        continue here;
                    }
                }
                logger.info("填充排");
                ServerRow row = new ServerRow();
                row.setRowCode(i);
                row.setRowType("false");
                rows.add(row);
            }
        }
        modelMap.put("rows", rows);
        Map<Integer, List<ServerCellarSensor>> serverCellarSensorTree = new TreeMap<>();
        for(ServerRow row: rows){
            List<ServerCellarSensor> serverCellarSensors = new ArrayList<>();
            if(row.getRowType().equals("false")){
                for(int i = 1; i<=12; i++){
                    ServerCellar cellar = new ServerCellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    ServerSensor sensor = new ServerSensor();
                    sensor.setAlarmLevel(0);
                    sensor.setServerCellar(cellar);
                    ServerCellarSensor cellarSensor = new ServerCellarSensor();
                    cellarSensor.setServerCellar(cellar);
                    cellarSensor.setServerSensor(sensor);
                    serverCellarSensors.add(cellarSensor);
                    logger.info("填充项");
                }
            }else{
                List<ServerCellar> existedCellars = serverCellarService.getCellarByRowDesc(row);
                here:
                for (int i = 1; i <= 12; i++) {
                    for (ServerCellar cellar: existedCellars) {
                        if(cellar.getCellarCode() == i){
                            logger.info("正常项.");
                            ServerSensor sensor = serverSensorService.getWorkingSensorByCellar(cellar);
                            if(sensor != null) {
                                ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                cellarSensor.setServerCellar(cellar);
                                cellarSensor.setServerSensor(sensor);
                                serverCellarSensors.add(cellarSensor);
                            }else {
                                ServerSensor sensor1 = new ServerSensor();
                                sensor1.setServerCellar(cellar);
                                sensor1.setAlarmLevel(0);
                                ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                cellarSensor.setServerCellar(cellar);
                                cellarSensor.setServerSensor(sensor1);
                                serverCellarSensors.add(cellarSensor);
                            }
                            continue here;
                        }
                    }
                    logger.info("无匹配，填充当前CellarCode");
                    ServerCellar cellar = new ServerCellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    ServerSensor sensor = new ServerSensor();
                    sensor.setAlarmLevel(0);
                    sensor.setServerCellar(cellar);
                    ServerCellarSensor cellarSensor = new ServerCellarSensor();
                    cellarSensor.setServerCellar(cellar);
                    cellarSensor.setServerSensor(sensor);
                    serverCellarSensors.add(cellarSensor);
                    logger.info("填充项");
                }
            }
//            if(row.getRowType().equals("false")){
//                for(int i = 1; i<=12; i++){
//                    Cellar cellar = new Cellar();
//                    cellar.setCellarCode(i);
//                    cellar.setCellarType("false");
//                    Sensor sensor = new Sensor();
//                    sensor.setAlarmLevel(0);
//                    cellar.setSensor(sensor);
//                    cellars.add(cellar);
//                    logger.info("填充项");
//                }
//            } else if(cellars.size() < 12 ){
//                cellars = cellarService.getCellarByRowDesc(row);
//                for(int i = cellars.size()+1; i<=12; i++){
//                    Cellar cellar = new Cellar();
//                    cellar.setCellarCode(i);
//                    cellar.setCellarType("false");
//                    Sensor sensor = new Sensor();
//                    sensor.setAlarmLevel(0);
//                    cellar.setSensor(sensor);
//                    cellars.add(cellar);
//                    logger.info("正常项");
//                }
//            }
            Collections.sort(serverCellarSensors, new Comparator<ServerCellarSensor>() {
                @Override
                public int compare(ServerCellarSensor o1, ServerCellarSensor o2) {
                    return o2.getServerCellar().getCellarCode() - o1.getServerCellar().getCellarCode();
                }
            });
            serverCellarSensorTree.put(row.getRowCode(), serverCellarSensors);
        }
        modelMap.put("cellarSensorTree",serverCellarSensorTree);
        return "winery/serverPage/cellarContainer";
    }


    /**
     * 获取全部设备
     */
    @RequestMapping(value = "/showAllSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showAllSensor(@RequestParam String plantId,
                                ModelMap modelMap) {
        ServerPlant plant = serverPlantService.find(plantId);
        modelMap.put("plant", plant);
        List<ServerTerritory> territories = serverTerritoryService.getTerritoriesByPlant(plant);
        List<ServerPit> pits = new ArrayList<>();
        if (territories == null) {
            return "winery/serverPage/allSensor";
        } else {
            for (ServerTerritory territory : territories) {
                List<ServerTeam> teams = serverTeamService.getTeamsByTerritory(territory);
                for (ServerTeam team : teams) {
                    List<ServerPit> existedPits = serverPitService.getPitsByTeam(team);
                    if (existedPits.size() < 10) {
//                        for (int i = pits.size()+1; i <= 10; i++){
//                            Pit pit = new Pit();
//                            pit.setPitCode(i);
//                            pit.setPitType("false");
//                            pits.add(pit);
//                        }
                        here:
                        for (int i = 1; i <= 10; i++) {
                            for (ServerPit pit : existedPits) {
                                if (pit.getPitCode() == i) {
                                    logger.info("1正常垮.");
                                    pits.add(pit);
                                    continue here;
                                }
                            }
                            logger.info("填充垮.");
                            ServerPit pit = new ServerPit();
                            pit.setPitCode(i);
                            pit.setPitType("false");
                            pits.add(pit);
                        }
                    }
                }
            }
            Collections.sort(pits, new Comparator<ServerPit>() {
                @Override
                public int compare(ServerPit o1, ServerPit o2) {
                    return o1.getPitCode() - o2.getPitCode();
                }
            });
        }
        Map<Integer, Map> pitTree = new TreeMap<>();
        List<ServerRow> rows = new ArrayList<>();
        for (ServerPit pit : pits) {
            if (pit.getPitType().equals("false")) {
                for (int i = 1; i <= 4; i++) {
                    ServerRow row = new ServerRow();
                    row.setRowCode(i);
                    row.setRowType("false");
                    rows.add(row);
                }
            } else if (pit.getPitType().equals("true")) {
                List<ServerRow> existedRows = serverRowService.getRowByPit(pit);
                if (existedRows.size() < 4) {
//            for (int i = rows.size()+1; i<=4 ; i++){
//                Row row = new Row();
//                row.setRowCode(i);
//                row.setRowType("false");
//                rows.add(row);
//            }
                    here:
                    for (int i = 1; i <= 4; i++) {
                        for (ServerRow row : existedRows) {
                            if (row.getRowCode() == i) {
                                logger.info("1正常排");
                                rows.add(row);
                                continue here;
                            }
                        }
                        logger.info("填充排");
                        ServerRow row = new ServerRow();
                        row.setRowCode(i);
                        row.setRowType("false");
                        rows.add(row);
                    }
                }
            }
                Map<Integer, List<ServerCellarSensor>> cellarSensorTree = new TreeMap<>();
                for (ServerRow row : rows) {
                    List<ServerCellarSensor> cellarSensors = new ArrayList<>();
                    if (row.getRowType().equals("false")) {
                        for (int i = 1; i <= 12; i++) {
                            ServerCellar cellar = new ServerCellar();
                            cellar.setCellarCode(i);
                            cellar.setCellarType("false");
                            ServerSensor sensor = new ServerSensor();
                            sensor.setAlarmLevel(0);
                            sensor.setServerCellar(cellar);
                            ServerCellarSensor cellarSensor = new ServerCellarSensor();
                            cellarSensor.setServerCellar(cellar);
                            cellarSensor.setServerSensor(sensor);
                            cellarSensors.add(cellarSensor);
                            logger.info("填充项1");
                        }
                    } else {
                        List<ServerCellar> existedCellars = serverCellarService.getCellarByRowDesc(row);
                        if (existedCellars.size() > 0) {
                            here:
                            for (int i = 1; i <= 12; i++) {
                                for (ServerCellar cellar : existedCellars) {
                                    if (cellar.getCellarCode() == i) {
                                        ServerSensor sensor = serverSensorService.getWorkingSensorByCellar(cellar);
                                        if (sensor == null) {
                                            ServerSensor sensor1 = new ServerSensor();
                                            sensor1.setAlarmLevel(0);
                                            sensor1.setServerCellar(cellar);
                                            ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                            cellarSensor.setServerCellar(cellar);
                                            cellarSensor.setServerSensor(sensor1);
                                            cellarSensors.add(cellarSensor);
                                        } else {
                                            logger.info("正常项1");
                                            ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                            cellarSensor.setServerCellar(cellar);
                                            cellarSensor.setServerSensor(sensor);
                                            cellarSensors.add(cellarSensor);
                                        }
                                        continue here;
                                    }
                                }
                                logger.info("无匹配，填充当前CellarCode");
                                ServerCellar cellar = new ServerCellar();
                                cellar.setCellarCode(i);
                                cellar.setCellarType("false");
                                ServerSensor sensor = new ServerSensor();
                                sensor.setAlarmLevel(0);
                                sensor.setServerCellar(cellar);
                                ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                cellarSensor.setServerCellar(cellar);
                                cellarSensor.setServerSensor(sensor);
                                cellarSensors.add(cellarSensor);
                                logger.info("填充项");
                            }
                        } else {
                            for (int i = 1; i <= 12; i++) {
                                logger.info("无匹配，填充当前CellarCode");
                                ServerCellar cellar = new ServerCellar();
                                cellar.setCellarCode(i);
                                cellar.setCellarType("false");
                                ServerSensor sensor = new ServerSensor();
                                sensor.setAlarmLevel(0);
                                sensor.setServerCellar(cellar);
                                ServerCellarSensor cellarSensor = new ServerCellarSensor();
                                cellarSensor.setServerCellar(cellar);
                                cellarSensor.setServerSensor(sensor);
                                cellarSensors.add(cellarSensor);
                                logger.info("填充项");
                            }
                        }

                    }
//            if(row.getRowType().equals("false")){
//                for(int i = 1; i<=12; i++){
//                    Cellar cellar = new Cellar();
//                    cellar.setCellarCode(i);
//                    cellar.setCellarType("false");
//                    Sensor sensor = new Sensor();
//                    sensor.setAlarmLevel(0);
//                    cellar.setSensor(sensor);
//                    cellars.add(cellar);
//                    logger.info("填充项");
//                }
//            } else if(cellars.size() < 12 ){
//                cellars = cellarService.getCellarByRowDesc(row);
//                for(int i = cellars.size()+1; i<=12; i++){
//                    Cellar cellar = new Cellar();
//                    cellar.setCellarCode(i);
//                    cellar.setCellarType("false");
//                    Sensor sensor = new Sensor();
//                    sensor.setAlarmLevel(0);
//                    cellar.setSensor(sensor);
//                    cellars.add(cellar);
//                    logger.info("正常项");
//                }
//            }
                    Collections.sort(cellarSensors, new Comparator<ServerCellarSensor>() {
                        @Override
                        public int compare(ServerCellarSensor o1, ServerCellarSensor o2) {
                            return o2.getServerCellar().getCellarCode() - o1.getServerCellar().getCellarCode();
                        }
                    });
                    cellarSensorTree.put(row.getRowCode(), cellarSensors);
                }
                pitTree.put(pit.getPitCode(), cellarSensorTree);
            }
            modelMap.put("pitTree", pitTree);
            return "winery/serverPage/allSensor";
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


    @RequestMapping(value = "/serverCurveDetail",produces={"text/html;charset=UTF-8;","application/json;"})
    public String serverCurveDetail(@RequestParam String sensorId,
                              ModelMap modelMap){
        ServerSensor sensor = serverSensorService.find(sensorId);
        modelMap.put("sensorId", sensorId);

        List<Double> topTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> midTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> botTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> createdTime = serverSensorNodeService.getTimeBySensorAndCurDay(sensor);
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
        return "winery/serverPage/serverCurveDetail";
    }

    @RequestMapping(value = "/showSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showSensorCurve(@ModelAttribute Sensor inputData,
                                  ModelMap modelMap){
        ServerSensor sensor = serverSensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", sensor.getSensorId());
        List<Double> topTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> midTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> botTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> createdTime = serverSensorNodeService.getTimeBySensorAndCurDay(sensor);
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
        return "winery/serverPage/factoryCurveContainer";
    }
//
//    @RequestMapping(value = "/showCellarInfo",produces={"text/html;charset=UTF-8;","application/json;"})
//    public String showCellarInfo(@ModelAttribute Cellar inputData,
//                                 ModelMap modelMap){
//        ServerCellar cellar = serverCellarService.find(inputData.getCellarId());
//        ServerSensor sensor = serverSensorService.getSensorByCellar(cellar);
//        modelMap.put("serverSensor", sensor);
//        ServerSensorNode topNode = serverSensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, TOP_TEMP_SENSOR);
//        ServerSensorNode midNode = serverSensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, MID_TEMP_SENSOR);
//        ServerSensorNode botNode = serverSensorNodeService.getNodeBySensorWithTypeAndCurTime(sensor, BOT_TEMP_SENSOR);
//        modelMap.put("serverTopNode", topNode);
//        modelMap.put("serverMidNode", midNode);
//        modelMap.put("serverBotNode", botNode);
//        return "factory/serverFactoryCellarContainer";
//    }
//
//    @RequestMapping(value = "/showSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
//    public String showSensorCurve(@ModelAttribute Sensor inputData,
//                                  ModelMap modelMap){
//        ServerSensor sensor = serverSensorService.find(inputData.getSensorId());
//        modelMap.put("sensorId", sensor.getSensorId());
//        List<Double> topTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
//        List<Double> midTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
//        List<Double> botTemps = serverSensorNodeService.getNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
//        List<Date> createdTime = serverSensorNodeService.getTimeBySensorAndCurDay(sensor);
//        Gson topGson = new Gson();
//        String topRetString = topGson.toJson(topTemps);
//        modelMap.put("serverTopTemps", topRetString);
//        Gson midGson = new Gson();
//        String midRetString = midGson.toJson(midTemps);
//        modelMap.put("serverMidTemps", midRetString);
//        Gson botGson = new Gson();
//        String botRetString = botGson.toJson(botTemps);
//        modelMap.put("serverBotTemps", botRetString);
//        Gson timeGson = new Gson();
//        String timeRetString = timeGson.toJson(createdTime);
//        modelMap.put("createdTimes", timeRetString);
//        return "factory/serverFactoryCurveContainer";
//    }
//
//    @RequestMapping(value = "/showHisSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
//    public String showHisSensorCurve(@ModelAttribute Sensor inputData,
//                                     ModelMap modelMap){
//        ServerSensor sensor = serverSensorService.find(inputData.getSensorId());
//        modelMap.put("sensorId", sensor.getSensorId());
//        List<Double> hisTopTemps = serverSensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
//        List<Double> hisMidTemps = serverSensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
//        List<Double> hisBotTemps = serverSensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
//        List<Date> hisCreatedTime = serverSensorNodeService.getHistoryTimeBySensorAndCurDay(sensor);
//        Gson topGson = new Gson();
//        String topRetString = topGson.toJson(hisTopTemps);
//        modelMap.put("hisTopTemps", topRetString);
//        Gson midGson = new Gson();
//        String midRetString = midGson.toJson(hisMidTemps);
//        modelMap.put("hisMidTemps", midRetString);
//        Gson botGson = new Gson();
//        String botRetString = botGson.toJson(hisBotTemps);
//        modelMap.put("hisBotTemps", botRetString);
//        Gson timeGson = new Gson();
//        String timeRetString = timeGson.toJson(hisCreatedTime);
//        modelMap.put("hisCreatedTime", timeRetString);
//        return "factory/serverFactoryHisCurveContainer";
//    }

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
        ServerSensor sensor = serverSensorService.find(sensorId);
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            //加24小时找值
            startDate = CommonUtils.addDays(format1.parse(inputData.getStartDate()), 1);
            endDate = CommonUtils.addDays(format1.parse(inputData.getEndDate()), 1);
            logger.info("Start server query.....");
            List<Double> queryTopTemps = serverSensorNodeService.getValuesBySensorAndRange(sensor, TOP_TEMP_SENSOR, startDate, endDate);
            List<Double> queryMidTemps = serverSensorNodeService.getValuesBySensorAndRange(sensor, MID_TEMP_SENSOR, startDate, endDate);
            List<Double> queryBotTemps = serverSensorNodeService.getValuesBySensorAndRange(sensor, BOT_TEMP_SENSOR, startDate, endDate);
            List<Date> queryDates = serverSensorNodeService.getDatesBySensorAndRange(sensor, startDate, endDate);
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


    @RequestMapping(value = "/showLastSixHours",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String showLastSixHours(@ModelAttribute QueryByHours inputData,
                                   ModelMap modelMap){
        int hours = inputData.getHours();
        ServerSensor sensor = serverSensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", inputData.getSensorId());

        List<Double> lastSixTopTemps = serverSensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR, hours);
        List<Double> lastSixMidTemps = serverSensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR, hours);
        List<Double> lastSixBotTemps = serverSensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR, hours);
        List<Date> lastSixCreatedTime = serverSensorNodeService.getLastSixTimeBySensorAndCurDay(sensor, hours);

        QueryDateSixHours queryDateSixHours = new QueryDateSixHours();
        queryDateSixHours.setRangeTopTemp(lastSixTopTemps);
        queryDateSixHours.setRangeMidTemp(lastSixMidTemps);
        queryDateSixHours.setRangeBotTemp(lastSixBotTemps);
        queryDateSixHours.setRangeCreatedTime(lastSixCreatedTime);
        Gson gson = new Gson();
        String retString = gson.toJson(queryDateSixHours);
        return retString;
    }


    @RequestMapping(value = "/localUploadServerPlant",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String localUploadServerPlant(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestBody String sensorInfo,
                                    ModelMap model){
        logger.info("Uploaded plant info is: " + sensorInfo);
        String retString = "";
        if(!machineType.equals("server")){
            retString = "Not server function... upload is not running...";
            return retString;
        }

        if(sensorInfo.trim().length() < 1) {
            retString = "服务器未接受到数据.";
            logger.info(retString);
            return retString;
        }
        //最新且已启用的sensor
        JSONObject jsonObject = JSONObject.parseObject(sensorInfo);
        ServerPlant serverPlant = JSON.toJavaObject(jsonObject,ServerPlant.class);
        serverPlantService.save(serverPlant);

        retString = "上传车间成功";
        logger.info(retString);
        return retString;
    }

    @RequestMapping(value = "/localUploadServerValue",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String localUploadServerValue(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @RequestBody String sensorInfo,
                                         ModelMap model) {
        String retString = "";
        String workintType = "";
        if(!machineType.equals("server")){
            retString = "Not server function... upload is not running...";
            return retString;
        }

        logger.info("开始添加车间plant.....");
        logger.info(">>>>>>>>>>>>>>>>> sensorinfo is  : " + sensorInfo);

        if(sensorInfo.trim().length() < 1) {
            retString = "未获取到设备信息。";
            logger.info(retString);
            return retString;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        LocalToServerSensorAdding sensorAdding = JSON.toJavaObject(jsonObject, LocalToServerSensorAdding.class);
        ServerPlant serverPlant = serverPlantService.getServerPlantByLocalId(sensorAdding.getServerPlant().getLocalPlantId());
        //本地传来数据 分解并存储
        //车间 有则下一层 无则录入
        if(serverPlant != null){
            ServerTerritory serverTerritory = serverTerritoryService.getServerTerritoryByLocalId(sensorAdding.getServerTerritory().getLocalTerritoryId());
            if(serverTerritory != null){
                ServerTeam serverTeam = serverTeamService.getServerTeamByLocalId(sensorAdding.getServerTeam().getLocalTeamId());
                if(serverTeam != null){
                    ServerPit serverPit = serverPitService.getServerPitByLocalId(sensorAdding.getServerPit().getLocalPitId());
                    if(serverPit != null){
                        ServerRow serverRow = serverRowService.getServerRowByLocalId(sensorAdding.getServerRow().getLocalRowId());
                        if(serverRow != null){
                            ServerCellar serverCellar = serverCellarService.getServerCellarByLocalId(sensorAdding.getServerCellar().getLocalCellarId());
                            String savingType = sensorAdding.getSavingType();
                            if (serverCellar != null){
                                if(!savingType.equals("deleted")){
                                    retString = "设备录入重复.";
                                    logger.info(retString);
                                    return retString;
                                }else{

                                    ServerSensor serverSensor1 = new ServerSensor();
                                    serverSensor1.setSensorId(HttpClientUtils.getUUID());
                                    serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                                    serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                                    serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                                    serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                                    serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                                    serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                                    serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                                    serverSensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                    serverSensor1.setServerCellar(serverCellar);
                                    serverSensor1.setCreatedTime(new Date());
                                    serverSensorService.save(serverSensor1);

                                    retString = "服务器异窖设备录入成功。";
                                    logger.info(retString);
                                    return retString;
                                }
                            }else {
                                ServerCellar serverCellar1 = new ServerCellar();
                                serverCellar1.setCellarId(HttpClientUtils.getUUID());
                                serverCellar1.setServerRow(serverRow);
                                serverCellar1.setCellarCode(sensorAdding.getServerCellar().getCellarCode());
                                serverCellar1.setLocalCellarId(sensorAdding.getServerCellar().getLocalCellarId());
                                serverCellarService.save(serverCellar1);

                                ServerSensor serverSensor1 = new ServerSensor();
                                serverSensor1.setSensorId(HttpClientUtils.getUUID());
                                serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                                serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                                serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                                serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                                serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                                serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                                serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                                serverSensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                serverSensor1.setServerCellar(serverCellar1);
                                serverSensor1.setCreatedTime(new Date());
                                serverSensorService.save(serverSensor1);

                                retString = "服务器异窖设备录入成功。";
                                logger.info(retString);
                                return retString;
                            }
                        }else {
                            ServerRow serverRow1 = new ServerRow();
                            serverRow1.setRowId(HttpClientUtils.getUUID());
                            serverRow1.setServerPit(serverPit);
                            serverRow1.setRowCode(sensorAdding.getServerRow().getRowCode());
                            serverRow1.setLocalRowId(sensorAdding.getServerRow().getLocalRowId());
                            serverRowService.save(serverRow1);

                            ServerCellar serverCellar1 = new ServerCellar();
                            serverCellar1.setCellarId(HttpClientUtils.getUUID());
                            serverCellar1.setServerRow(serverRow1);
                            serverCellar1.setCellarCode(sensorAdding.getServerCellar().getCellarCode());
                            serverCellar1.setLocalCellarId(sensorAdding.getServerCellar().getLocalCellarId());
                            serverCellarService.save(serverCellar1);

                            ServerSensor serverSensor1 = new ServerSensor();
                            serverSensor1.setSensorId(HttpClientUtils.getUUID());
                            serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                            serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                            serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                            serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                            serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                            serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                            serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                            serverSensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                            serverSensor1.setServerCellar(serverCellar1);
                            serverSensor1.setCreatedTime(new Date());
                            serverSensorService.save(serverSensor1);


                            retString = "服务器异排设备录入成功。";
                            logger.info(retString);
                            return retString;
                        }
                    }else {
                        ServerPit serverPit1 = new ServerPit();
                        serverPit1.setPitId(HttpClientUtils.getUUID());
                        serverPit1.setServerTeam(serverTeam);
                        serverPit1.setPitCode(sensorAdding.getServerPit().getPitCode());
                        serverPit1.setLocalPitId(sensorAdding.getServerPit().getLocalPitId());
                        serverPitService.save(serverPit1);

                        ServerRow serverRow1 = new ServerRow();
                        serverRow1.setRowId(HttpClientUtils.getUUID());
                        serverRow1.setServerPit(serverPit1);
                        serverRow1.setRowCode(sensorAdding.getServerRow().getRowCode());
                        serverRow1.setLocalRowId(sensorAdding.getServerRow().getLocalRowId());
                        serverRowService.save(serverRow1);

                        ServerCellar serverCellar1 = new ServerCellar();
                        serverCellar1.setCellarId(HttpClientUtils.getUUID());
                        serverCellar1.setServerRow(serverRow1);
                        serverCellar1.setCellarCode(sensorAdding.getServerCellar().getCellarCode());
                        serverCellar1.setLocalCellarId(sensorAdding.getServerCellar().getLocalCellarId());
                        serverCellarService.save(serverCellar1);

                        ServerSensor serverSensor1 = new ServerSensor();
                        serverSensor1.setSensorId(HttpClientUtils.getUUID());
                        serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                        serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                        serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                        serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                        serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                        serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                        serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                        serverSensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                        serverSensor1.setServerCellar(serverCellar1);
                        serverSensor1.setCreatedTime(new Date());
                        serverSensorService.save(serverSensor1);


                        retString = "服务器异垮设备录入成功。";
                        logger.info(retString);
                        return retString;
                    }
                }else {
                    ServerTeam serverTeam1 = new ServerTeam();
                    serverTeam1.setTeamId(HttpClientUtils.getUUID());
                    serverTeam1.setServerTerritory(serverTerritory);
                    serverTeam1.setTeamCode(sensorAdding.getServerTeam().getTeamCode());
                    serverTeam1.setLocalTeamId(sensorAdding.getServerTeam().getLocalTeamId());
                    serverTeamService.save(serverTeam1);

                    ServerPit serverPit1 = new ServerPit();
                    serverPit1.setPitId(HttpClientUtils.getUUID());
                    serverPit1.setServerTeam(serverTeam1);
                    serverPit1.setPitCode(sensorAdding.getServerPit().getPitCode());
                    serverPit1.setLocalPitId(sensorAdding.getServerPit().getLocalPitId());
                    serverPitService.save(serverPit1);

                    ServerRow serverRow1 = new ServerRow();
                    serverRow1.setRowId(HttpClientUtils.getUUID());
                    serverRow1.setServerPit(serverPit1);
                    serverRow1.setRowCode(sensorAdding.getServerRow().getRowCode());
                    serverRow1.setLocalRowId(sensorAdding.getServerRow().getLocalRowId());
                    serverRowService.save(serverRow1);

                    ServerCellar serverCellar1 = new ServerCellar();
                    serverCellar1.setCellarId(HttpClientUtils.getUUID());
                    serverCellar1.setServerRow(serverRow1);
                    serverCellar1.setCellarCode(sensorAdding.getServerCellar().getCellarCode());
                    serverCellar1.setLocalCellarId(sensorAdding.getServerCellar().getLocalCellarId());
                    serverCellarService.save(serverCellar1);

                    ServerSensor serverSensor1 = new ServerSensor();
                    serverSensor1.setSensorId(HttpClientUtils.getUUID());
                    serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                    serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                    serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                    serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                    serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                    serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                    serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                    serverSensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    serverSensor1.setServerCellar(serverCellar1);
                    serverSensor1.setCreatedTime(new Date());
                    serverSensorService.save(serverSensor1);


                    retString = "服务器异班组区设备录入成功。";
                    logger.info(retString);
                    return retString;
                }
            }else {
                ServerTerritory serverTerritory1 = new ServerTerritory();
                serverTerritory1.setTerritoryId(HttpClientUtils.getUUID());
                serverTerritory1.setServerPlant(serverPlant);
                serverTerritory1.setTerritoryCode(sensorAdding.getServerTerritory().getTerritoryCode());
                serverTerritory1.setLocalTerritoryId(sensorAdding.getServerTerritory().getLocalTerritoryId());
                serverTerritoryService.save(serverTerritory1);

                ServerTeam serverTeam1 = new ServerTeam();
                serverTeam1.setTeamId(HttpClientUtils.getUUID());
                serverTeam1.setServerTerritory(serverTerritory1);
                serverTeam1.setTeamCode(sensorAdding.getServerTeam().getTeamCode());
                serverTeam1.setLocalTeamId(sensorAdding.getServerTeam().getLocalTeamId());
                serverTeamService.save(serverTeam1);

                ServerPit serverPit1 = new ServerPit();
                serverPit1.setPitId(HttpClientUtils.getUUID());
                serverPit1.setServerTeam(serverTeam1);
                serverPit1.setPitCode(sensorAdding.getServerPit().getPitCode());
                serverPit1.setLocalPitId(sensorAdding.getServerPit().getLocalPitId());
                serverPitService.save(serverPit1);

                ServerRow serverRow1 = new ServerRow();
                serverRow1.setRowId(HttpClientUtils.getUUID());
                serverRow1.setServerPit(serverPit1);
                serverRow1.setRowCode(sensorAdding.getServerRow().getRowCode());
                serverRow1.setLocalRowId(sensorAdding.getServerRow().getLocalRowId());
                serverRowService.save(serverRow1);

                ServerCellar serverCellar1 = new ServerCellar();
                serverCellar1.setCellarId(HttpClientUtils.getUUID());
                serverCellar1.setServerRow(serverRow1);
                serverCellar1.setCellarCode(sensorAdding.getServerCellar().getCellarCode());
                serverCellar1.setLocalCellarId(sensorAdding.getServerCellar().getLocalCellarId());
                serverCellarService.save(serverCellar1);

                ServerSensor serverSensor1 = new ServerSensor();
                serverSensor1.setSensorId(HttpClientUtils.getUUID());
                serverSensor1.setFrequencyPoint(sensorAdding.getServerSensor().getFrequencyPoint());
                serverSensor1.setWorkingHours(sensorAdding.getServerSensor().getWorkingHours());
                serverSensor1.setWorkRate(sensorAdding.getServerSensor().getWorkRate());
                serverSensor1.setActivatePeriod(sensorAdding.getServerSensor().getActivatePeriod());
                serverSensor1.setSensorCode(sensorAdding.getServerSensor().getSensorCode());
                serverSensor1.setBatteryState(sensorAdding.getServerSensor().getBatteryState());
                serverSensor1.setLocalSensorId(sensorAdding.getServerSensor().getLocalSensorId());
                String tValue = SensorWorkingType.SENSOR_IS_WORKING.name();
                serverSensor1.setSensorWorkingType(SensorWorkingType.valueOf(tValue));
                serverSensor1.setServerCellar(serverCellar1);
                serverSensor1.setCreatedTime(new Date());
                serverSensorService.save(serverSensor1);

                retString = "服务器异责任区设备录入成功。";
                logger.info(retString);
                return retString;
            }
        }
        return retString;
    }

    @RequestMapping(value = "/newUploadValueUrl",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String newUploadValueUrl(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestBody String sensorInfo,
                                                ModelMap model){
        logger.info("Uploaded device info is: " + sensorInfo);
        String retString = "";
        if(!machineType.equals("server")){
            retString = "Not server function... upload is not running...";
            return retString;
        }

        if(sensorInfo.trim().length() < 1) {
            retString = "服务器未接受到数据.";
            logger.info(retString);
            return retString;
        }
        //最新且已启用的sensor
        JSONObject jsonObject = JSONObject.parseObject(sensorInfo);
        LocalToServerSensorNodes nodes = JSON.toJavaObject(jsonObject,LocalToServerSensorNodes.class);
        ServerSensor serverSensor = serverSensorService.getServerSensorByLocalId(nodes.getLocalSensorId());
        ServerSensorNode topNode = nodes.getTopNode();
        ServerSensorNode midNode = nodes.getMidNode();
        ServerSensorNode botNode = nodes.getBotNode();

        int topAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(topNode.getSensorValue());
        int midAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(midNode.getSensorValue());
        int botAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(botNode.getSensorValue());
        int sensorAlarmLevel = CommonUtils.comparingMaxAlarmLevel(topAlarmLevel,midAlarmLevel,botAlarmLevel);
        serverSensor.setAlarmLevel(sensorAlarmLevel);
        serverSensor.setCurTopNodeValue(topNode.getSensorValue());
        serverSensor.setCurMidNodeValue(midNode.getSensorValue());
        serverSensor.setCurBotNodeValue(botNode.getSensorValue());
        serverSensorService.update(serverSensor);

        topNode.setServerSensor(serverSensor);
        topNode.setSensorType(SensorType.TOP_TEMP_SENSOR);
        midNode.setServerSensor(serverSensor);
        midNode.setSensorType(SensorType.MID_TEMP_SENSOR);
        botNode.setServerSensor(serverSensor);
        botNode.setSensorType(SensorType.BOT_TEMP_SENSOR);
        serverSensorNodeService.save(topNode);
        serverSensorNodeService.save(midNode);
        serverSensorNodeService.save(botNode);

        retString = "上传设备最新数据成功";
        logger.info(retString);
        return retString;
    }


    @RequestMapping(value = "/localUploadServerSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String localUploadServerSensor(HttpServletRequest request,
                                          HttpServletResponse response,
                                          @RequestBody String sensorInfo,
                                          ModelMap model) {
        String retString = "";
                if(!machineType.equals("server")){
            retString = "Not server function... upload is not running...";
            return retString;
        }
        logger.info("开始添加sensor.....");
        logger.info(">>>>>>>>>>>>>>>>> sensorinfo is  : " + sensorInfo);
        if(sensorInfo.trim().length() < 1) {
            retString = "未获取到设备信息。";
            logger.info(retString);
            return retString;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        ServerSensor serverSensor = JSON.toJavaObject(jsonObject, ServerSensor.class);
        serverSensor.setCreatedTime(new Date());
        serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
        serverSensorService.save(serverSensor);
        retString = "上传sensor至服务器成功...。";
        logger.info(retString);
        return retString;
    }

    @RequestMapping(value = "/loaduploadthreshold",produces={"text/html;charset=UTF-8;","application/json;"})
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

    @RequestMapping(value = "/loaduploaddeletethreshold",produces={"text/html;charset=UTF-8;","application/json;"})
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

    @RequestMapping(value = "/loadUploadDeleteSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String loadUploadDeleteSensor(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         @RequestBody String sensorInfo,
                                                         ModelMap model){
        logger.info("Delete Uploaded sensor is: " + sensorInfo);
        String retString = "";
        if(sensorInfo.trim().length() < 1) {
            retString = "sensor added error";
            return retString;
        }
        JSONObject jsonObject = JSON.parseObject(sensorInfo);
        if(jsonObject.get("operate").toString().equals("deleteSensor"))
        {
            String sensorId = jsonObject.get("sensorId").toString();
            ServerSensor serverSensor = serverSensorService.getServerSensorByLocalId(sensorId);
            serverSensor.setSensorWorkingType(SensorWorkingType.SENSOR_WAS_DELETED);
            serverSensorService.update(serverSensor);
            retString = "Server sensor delete success.";
            logger.info(retString);
            return retString;
        }else{
            retString = "sensor deleted error.";
            return retString;
        }
    }
}
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
import com.loohos.factoryinspection.utils.*;
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
    @Value("${com.loohos.loadUploadDeleteSensor}")
    private String loadUploadDeleteSensor;

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
            for(Territory territory: territories) {
                List<Team> teams = teamService.getTeamsByTerritory(territory);
                for (Team team : teams) {
                    List<Pit> existedPits = pitService.getPitsByTeam(team);
                    if(existedPits.size() < 10){
//                        for (int i = pits.size()+1; i <= 10; i++){
//                            Pit pit = new Pit();
//                            pit.setPitCode(i);
//                            pit.setPitType("false");
//                            pits.add(pit);
//                        }
                        here:
                        for(int i = 1; i <= 10; i++){
                            for(Pit pit : existedPits){
                                if(pit.getPitCode() == i){
                                    logger.info("正常垮.");
                                    pits.add(pit);
                                    continue here;
                                }
                            }
                            logger.info("填充垮.");
                            Pit pit = new Pit();
                            pit.setPitCode(i);
                            pit.setPitType("false");
                            pits.add(pit);
                        }
                    }
                }
            }
            Collections.sort(pits, new Comparator<Pit>() {
                @Override
                public int compare(Pit o1, Pit o2) {
                    return o1.getPitCode() - o2.getPitCode();
                }
            });
            modelMap.put("pits", pits);
        }
//        return "factory/factorycontainer";
        return "winery/localPage/plantInside";
    }


    @RequestMapping(value = "/showCellar",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showCellar(@ModelAttribute Pit inputData,
                             ModelMap modelMap){
        Pit pit = pitService.find(inputData.getPitId());
        modelMap.put("pit", pit);
        List<Row> rows = new ArrayList<>();
        List<Row> existedRows = rowService.getRowByPit(pit);
        if(existedRows.size() < 4){
//            for (int i = rows.size()+1; i<=4 ; i++){
//                Row row = new Row();
//                row.setRowCode(i);
//                row.setRowType("false");
//                rows.add(row);
//            }
            here:
            for (int i = 1; i <= 4 ; i++){
                for(Row row : existedRows){
                    if(row.getRowCode() == i){
                        logger.info("正常排");
                        rows.add(row);
                        continue here;
                    }
                }
                logger.info("填充排");
                Row row = new Row();
                row.setRowCode(i);
                row.setRowType("false");
                rows.add(row);
            }
        }
        modelMap.put("rows", rows);
        Map<Integer, List<CellarSensor>> cellarSensorTree = new TreeMap<>();
        for(Row row: rows){
            List<CellarSensor> cellarSensors = new ArrayList<>();
            if(row.getRowType().equals("false")){
                for(int i = 1; i<=12; i++){
                    Cellar cellar = new Cellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    Sensor sensor = new Sensor();
                    sensor.setAlarmLevel(0);
                    sensor.setCellar(cellar);
                    CellarSensor cellarSensor = new CellarSensor();
                    cellarSensor.setCellar(cellar);
                    cellarSensor.setSensor(sensor);
                    cellarSensors.add(cellarSensor);
                    logger.info("填充项");
                }
            }else{
                List<Cellar> existedCellars = cellarService.getCellarByRowDesc(row);
                here:
                for (int i = 1; i <= 12; i++) {
                    for (Cellar cellar: existedCellars) {
                        if(cellar.getCellarCode() == i){
                            logger.info("正常项.");
                            Sensor sensor = sensorService.getWorkingSensorByCellar(cellar);
                            if(sensor != null) {
                                CellarSensor cellarSensor = new CellarSensor();
                                cellarSensor.setCellar(cellar);
                                cellarSensor.setSensor(sensor);
                                cellarSensors.add(cellarSensor);
                            }else {
                                Sensor sensor1 = new Sensor();
                                sensor1.setCellar(cellar);
                                sensor1.setAlarmLevel(0);
                                CellarSensor cellarSensor = new CellarSensor();
                                cellarSensor.setCellar(cellar);
                                cellarSensor.setSensor(sensor1);
                                cellarSensors.add(cellarSensor);
                            }
                            continue here;
                        }
                    }
                    logger.info("无匹配，填充当前CellarCode");
                    Cellar cellar = new Cellar();
                    cellar.setCellarCode(i);
                    cellar.setCellarType("false");
                    Sensor sensor = new Sensor();
                    sensor.setAlarmLevel(0);
                    sensor.setCellar(cellar);
                    CellarSensor cellarSensor = new CellarSensor();
                    cellarSensor.setCellar(cellar);
                    cellarSensor.setSensor(sensor);
                    cellarSensors.add(cellarSensor);
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
            Collections.sort(cellarSensors, new Comparator<CellarSensor>() {
                @Override
                public int compare(CellarSensor o1, CellarSensor o2) {
                    return o2.getCellar().getCellarCode() - o1.getCellar().getCellarCode();
                }
            });
            cellarSensorTree.put(row.getRowCode(), cellarSensors);
        }
//        modelMap.put("cellarTree",cellarTree);
        modelMap.put("cellarSensorTree", cellarSensorTree);
        return "winery/localPage/cellarContainer";
    }


    /**
     * 获取全部设备
     */
    @RequestMapping(value = "/showAllSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showAllSensor(@RequestParam String plantId,
                            ModelMap modelMap){
        Plant plant = plantService.find(plantId);
        modelMap.put("plant", plant);
        List<Territory> territories = territoryService.getTerritoriesByPlant(plant);
        List<Pit> pits = new ArrayList<>();
        if(territories == null){
            return "winery/localPage/allSensor";
        }
        else {
            for(Territory territory: territories) {
                List<Team> teams = teamService.getTeamsByTerritory(territory);
                for (Team team : teams) {
                    List<Pit> existedPits = pitService.getPitsByTeam(team);
                    if(existedPits.size() < 10){
//                        for (int i = pits.size()+1; i <= 10; i++){
//                            Pit pit = new Pit();
//                            pit.setPitCode(i);
//                            pit.setPitType("false");
//                            pits.add(pit);
//                        }
                        here:
                        for(int i = 1; i <= 10; i++){
                            for(Pit pit : existedPits){
                                if(pit.getPitCode() == i){
                                    logger.info("1正常垮.");
                                    pits.add(pit);
                                    continue here;
                                }
                            }
                            logger.info("填充垮.");
                            Pit pit = new Pit();
                            pit.setPitCode(i);
                            pit.setPitType("false");
                            pits.add(pit);
                        }
                    }
                }
            }
            Collections.sort(pits, new Comparator<Pit>() {
                @Override
                public int compare(Pit o1, Pit o2) {
                    return o1.getPitCode() - o2.getPitCode();
                }
            });
        }
        Map<Integer, Map> pitTree = new TreeMap<>();
        List<Row> rows = new ArrayList<>();
        for (Pit pit: pits){
            if(pit.getPitType().equals("false")){
                for (int i = 1; i<=4 ; i++){
                    Row row = new Row();
                    row.setRowCode(i);
                    row.setRowType("false");
                    rows.add(row);
                }
            }else if(pit.getPitType().equals("true")) {
                List<Row> existedRows = rowService.getRowByPit(pit);
                if(existedRows.size() < 4){
//            for (int i = rows.size()+1; i<=4 ; i++){
//                Row row = new Row();
//                row.setRowCode(i);
//                row.setRowType("false");
//                rows.add(row);
//            }
                    here:
                    for (int i = 1; i <= 4 ; i++){
                        for(Row row : existedRows){
                            if(row.getRowCode() == i){
                                logger.info("1正常排");
                                rows.add(row);
                                continue here;
                            }
                        }
                        logger.info("填充排");
                        Row row = new Row();
                        row.setRowCode(i);
                        row.setRowType("false");
                        rows.add(row);
                    }
                }
//                for(int i = rows.size()+1; i<=4; i++){
//                    Row row = new Row();
//                    row.setRowType("false");
//                    row.setRowCode(i);
//                    rows.add(row);
//                }
            }
            Map<Integer, List<CellarSensor>> cellarSensorTree = new TreeMap<>();
            for(Row row: rows){
                List<CellarSensor> cellarSensors = new ArrayList<>();
                if(row.getRowType().equals("false")){
                    for(int i = 1; i<=12; i++){
                        Cellar cellar = new Cellar();
                        cellar.setCellarCode(i);
                        cellar.setCellarType("false");
                        Sensor sensor = new Sensor();
                        sensor.setAlarmLevel(0);
                        sensor.setCellar(cellar);
                        CellarSensor cellarSensor = new CellarSensor();
                        cellarSensor.setCellar(cellar);
                        cellarSensor.setSensor(sensor);
                        cellarSensors.add(cellarSensor);
                        logger.info("填充项1");
                    }
                }else{
                    List<Cellar> existedCellars = cellarService.getCellarByRowDesc(row);
                    if(existedCellars.size()>0){
                        here:
                        for (int i = 1; i <= 12; i++) {
                            for (Cellar cellar: existedCellars) {
                                if(cellar.getCellarCode() == i){
                                    Sensor sensor = sensorService.getWorkingSensorByCellar(cellar);
                                    if(sensor == null){
                                        Sensor sensor1 = new Sensor();
                                        sensor1.setAlarmLevel(0);
                                        sensor1.setCellar(cellar);
                                        CellarSensor cellarSensor = new CellarSensor();
                                        cellarSensor.setCellar(cellar);
                                        cellarSensor.setSensor(sensor1);
                                        cellarSensors.add(cellarSensor);
                                    }else{
                                        logger.info("正常项1");
                                        CellarSensor cellarSensor = new CellarSensor();
                                        cellarSensor.setCellar(cellar);
                                        cellarSensor.setSensor(sensor);
                                        cellarSensors.add(cellarSensor);
                                    }
                                    continue here;
                                }
                            }
                            logger.info("无匹配，填充当前CellarCode");
                            Cellar cellar = new Cellar();
                            cellar.setCellarCode(i);
                            cellar.setCellarType("false");
                            Sensor sensor = new Sensor();
                            sensor.setAlarmLevel(0);
                            sensor.setCellar(cellar);
                            CellarSensor cellarSensor = new CellarSensor();
                            cellarSensor.setCellar(cellar);
                            cellarSensor.setSensor(sensor);
                            cellarSensors.add(cellarSensor);
                            logger.info("填充项");
                        }
                    }else {
                        for (int i = 1; i <= 12; i++) {
                            logger.info("无匹配，填充当前CellarCode");
                            Cellar cellar = new Cellar();
                            cellar.setCellarCode(i);
                            cellar.setCellarType("false");
                            Sensor sensor = new Sensor();
                            sensor.setAlarmLevel(0);
                            sensor.setCellar(cellar);
                            CellarSensor cellarSensor = new CellarSensor();
                            cellarSensor.setCellar(cellar);
                            cellarSensor.setSensor(sensor);
                            cellarSensors.add(cellarSensor);
                            logger.info("填充项");
                        }
                    }

                }
//        Map<Integer, Map> pitTree = new TreeMap<>();
//        List<Row> rows = new ArrayList<>();
//        for (Pit pit: pits){
//            if(pit.getPitType().equals("false")){
//                for (int i = 1; i<=4 ; i++){
//                    Row row = new Row();
//                    row.setRowCode(i);
//                    row.setRowType("false");
//                    rows.add(row);
//                }
//            }else if(pit.getPitType().equals("true")) {
//                rows = rowService.getRowByPit(pit);
//                for(int i = rows.size()+1; i<=4; i++){
//                    Row row = new Row();
//                    row.setRowType("false");
//                    row.setRowCode(i);
//                    rows.add(row);
//                }
//            }
//            Map<Integer, List<Cellar>> cellarTree = new TreeMap<>();
//            for(Row row: rows){
//                List<Cellar> cellars = new ArrayList<>();
//                if(row.getRowType().equals("false")){
//                    for(int i = 1; i<=12; i++){
//                        Cellar cellar = new Cellar();
//                        cellar.setCellarCode(i);
//                        cellar.setCellarType("false");
//                        Sensor sensor = new Sensor();
//                        sensor.setAlarmLevel(0);
//                        cellar.setSensor(sensor);
//                        cellars.add(cellar);
//                        logger.info("填充项1");
//                    }
//                }else{
//                    List<Cellar> existedCellars = cellarService.getCellarByRowDesc(row);
//                    here:
//                    for (int i = 1; i <= 12; i++) {
//                        for (Cellar cellar: existedCellars) {
//                            if(cellar.getCellarCode() == i){
//                                logger.info("正常项1");
//                                cellars.add(cellar);
//                                continue here;
//                            }
//                        }
//                        logger.info("无匹配，填充当前CellarCode");
//                        Cellar cellar = new Cellar();
//                        cellar.setCellarCode(i);
//                        cellar.setCellarType("false");
//                        Sensor sensor = new Sensor();
//                        sensor.setAlarmLevel(0);
//                        cellar.setSensor(sensor);
//                        cellars.add(cellar);
//                        logger.info("填充项");
//                    }
//                }
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
                Collections.sort(cellarSensors, new Comparator<CellarSensor>() {
                    @Override
                    public int compare(CellarSensor o1, CellarSensor o2) {
                        return o2.getCellar().getCellarCode() - o1.getCellar().getCellarCode();
                    }
                });

                cellarSensorTree.put(row.getRowCode(), cellarSensors);
            }
            pitTree.put(pit.getPitCode(), cellarSensorTree);
        }
        modelMap.put("pitTree", pitTree);
        return "winery/localPage/allSensor";
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


    @RequestMapping(value = "/curveDetail",produces={"text/html;charset=UTF-8;","application/json;"})
    public String curveDetail(@RequestParam String sensorId,
                                 ModelMap modelMap){
        Sensor sensor = sensorService.find(sensorId);
        modelMap.put("sensorId", sensorId);

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
        return "winery/localPage/curveDetail";
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

        return "winery/localPage/factoryCurveContainer";
    }

    @RequestMapping(value = "/showHisSensorCurve",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String showHisSensorCurve(@ModelAttribute Sensor inputData,
                                  ModelMap modelMap){
        Sensor sensor = sensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", sensor.getSensorId());

        List<Double> hisTopTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR);
        List<Double> hisMidTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR);
        List<Double> hisBotTemps = sensorNodeService.getHistoryNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR);
        List<Date> hisCreatedTime = sensorNodeService.getHistoryTimeBySensorAndCurDay(sensor);

        QueryHistoryResult queryHistoryResult = new QueryHistoryResult();
        queryHistoryResult.setHisTopTemp(hisTopTemps);
        queryHistoryResult.setHisMidTemp(hisBotTemps);
        queryHistoryResult.setHisBotTemp(hisMidTemps);
        queryHistoryResult.setHisCreatedTime(hisCreatedTime);
        Gson gson = new Gson();
        String retString = gson.toJson(queryHistoryResult);
        return retString;
    }

    @RequestMapping(value = "/showLastSixHours",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String showLastSixHours(@ModelAttribute QueryByHours inputData,
                                     ModelMap modelMap){
        int hours = inputData.getHours();
        Sensor sensor = sensorService.find(inputData.getSensorId());
        modelMap.put("sensorId", sensor.getSensorId());

        List<Double> lastSixTopTemps = sensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, TOP_TEMP_SENSOR, hours);
        List<Double> lastSixMidTemps = sensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, MID_TEMP_SENSOR, hours);
        List<Double> lastSixBotTemps = sensorNodeService.getLastSixNodeBySensorWithTypeAndCurDay(sensor, BOT_TEMP_SENSOR, hours);
        List<Date> lastSixCreatedTime = sensorNodeService.getLastSixTimeBySensorAndCurDay(sensor, hours);

        QueryDateSixHours queryDateSixHours = new QueryDateSixHours();
        queryDateSixHours.setRangeTopTemp(lastSixTopTemps);
        queryDateSixHours.setRangeMidTemp(lastSixMidTemps);
        queryDateSixHours.setRangeBotTemp(lastSixBotTemps);
        queryDateSixHours.setRangeCreatedTime(lastSixCreatedTime);
        Gson gson = new Gson();
        String retString = gson.toJson(queryDateSixHours);
        return retString;
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

//    /**
//     *删除终端信息
//     * */
//    @RequestMapping(value = "/deleteSensor",produces={"text/html;charset=UTF-8;","application/json;"})
//    @ResponseBody
//    public String deleteSensor(@ModelAttribute Sensor inputData,
//                            HttpServletRequest request){
//        String retString = "";
//        Sensor sensor = sensorService.find(inputData.getSensorId());
//        try{
//            sensorService.delete(sensor.getSensorId());
//            cellarService.delete(sensor.getCellar().getCellarId());
//
//            String centerUrl = serverUrl+loadUploadDeleteSensor;
//            JSONObject deleteSensorJson = new JSONObject();
//            deleteSensorJson.put("operate", "deleteSensor");
//            deleteSensorJson.put("sensorId", sensor.getSensorId());
//            String retInfo = sendToServer(deleteSensorJson, centerUrl);
//            logger.info(retInfo);
//            retString = "删除设备 " + sensor.getSensorCode() +" 成功";
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return retString;
//    }

    /**
     *删除终端信息
     * */
    @RequestMapping(value = "/deleteSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String deleteSensor(@ModelAttribute Sensor inputData,
                               HttpServletRequest request){
        String retString = "";
        Sensor sensor = sensorService.find(inputData.getSensorId());
        try{
            sensor.setSensorWorkingType(SensorWorkingType.SENSOR_WAS_DELETED);
            sensorService.update(sensor);

            String centerUrl = serverUrl+loadUploadDeleteSensor;
            JSONObject deleteSensorJson = new JSONObject();
            deleteSensorJson.put("operate", "deleteSensor");
            deleteSensorJson.put("sensorId", sensor.getSensorId());
            String retInfo = sendToServer(deleteSensorJson, centerUrl);
            logger.info(retInfo);
            retString = "删除设备 " + sensor.getSensorCode() +" 成功";
        }catch (Exception e){
            e.printStackTrace();
        }
        return retString;
    }

    /**
     *修改终端信息
     * */
    @RequestMapping(value = "/showInfoBeforeAlter",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String showInfoBeforeAlter(@ModelAttribute Sensor inputData,
                              HttpServletRequest request){
        String retString = "";
        Sensor sensor = sensorService.find(inputData.getSensorId());
        try{
            System.out.println(sensor);
            SensorPosInfo posInfo = new SensorPosInfo();
            posInfo.setCellarCode(sensor.getCellar().getCellarCode());
            posInfo.setRowCode(sensor.getCellar().getRow().getRowCode());
            posInfo.setPitCode(sensor.getCellar().getRow().getPit().getPitCode());
            posInfo.setTeamCode(sensor.getCellar().getRow().getPit().getTeam().getTeamCode());
            posInfo.setTerritoryCode(sensor.getCellar().getRow().getPit().getTeam().getTerritory().getTerritoryCode());
            posInfo.setPlantCode(sensor.getCellar().getRow().getPit().getTeam().getTerritory().getPlant().getPlantCode());
            posInfo.setSensorCode(sensor.getSensorCode());
            Gson gson = new Gson();
            String posInfoString = gson.toJson(posInfo);
            return posInfoString;
        }catch (Exception e){
            e.printStackTrace();
            return retString;
        }
    }

    /**
     *修改终端信息
     * */
    @RequestMapping(value = "/alterSensor",produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String alterSensor(@ModelAttribute SensorPosInfo inputData,
                               HttpServletRequest request){
        String retString = "";
        logger.info("terr: " + inputData.getTerritoryCode() + ", team: " + inputData.getTeamCode() + ", pit: " + inputData.getPitCode()
        + ", row: " + inputData.getRowCode() + ", cellar: " + inputData.getCellarCode());
        try{
            if(commThread.isAlive()){
                retString = setSensorInfo(inputData);
            }else{
                boolean started = commThread.startCommPort(SerialPortType.COM3.getDescription(), 9600, 8, 1, 0);
                if(started){
                    commThread.start();
                    retString = setSensorInfo(inputData);
                }else{
                    retString = setSensorInfo(inputData);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return retString;
    }

    private String setSensorInfo(SensorPosInfo posInfo){
        String retString = "";
        String workingType = "";
        Sensor sensor = sensorService.find(posInfo.getSensorId());
        int plantCode = posInfo.getPlantCode();
        int territoryCode = posInfo.getTerritoryCode();
        int teamCode = posInfo.getTeamCode();
        int pitCode = posInfo.getPitCode();
        int rowCode = posInfo.getRowCode();
        int cellarCode = posInfo.getCellarCode();
        final String rsPrefix = "7E381A"; //标志字符7E 帧格式38(发送) 帧长度0F
        final String rsPartitionSendCode = "00"; //分割发送码
        final String rsSufMessagefix = "AB10100300050A"; //信息层
        String rsLocFix = validHexUnit(plantCode, 4) + validHexUnit(territoryCode, 4)
                + validHexUnit(teamCode, 4) + "FF" + validHexUnit(pitCode, 2)
                + validHexUnit(rowCode, 2) + validHexUnit(cellarCode, 2);
        final String rsSuffix = "CCCC";
        int CRC = CRC16Util.calcCrc16(HexUtils.hexStringToByte(rsSufMessagefix + rsLocFix));
        String CRCCODE = HexUtils.bytesToHexString(CRC16Util.intToBytes(CRC));
        String strMessage = rsPrefix + posInfo.getSensorCode() + rsPartitionSendCode + rsSufMessagefix + rsLocFix
                + CRCCODE + rsSuffix;
        logger.info(strMessage);
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
            //error length : 36
            if (respMessage.length != 39) {
                logger.info("sen occur a problem, try to save a zero value");
                retString = "当前录入设备异常，请处理后录入.（可能原因：1.电量不足。2.设备故障。）";
            }
            else{
                logger.info("change sensor info success!");
                sensor.setSensorWorkingType(SensorWorkingType.SENSOR_WAS_DELETED);
                sensorService.update(sensor);

                String centerUrl = serverUrl+loadUploadDeleteSensor;
                JSONObject deleteSensorJson = new JSONObject();
                deleteSensorJson.put("operate", "deleteSensor");
                deleteSensorJson.put("sensorId", sensor.getSensorId());
                String retInfo = sendToServer(deleteSensorJson, centerUrl);
                logger.info(retInfo);

                LocalToServerSensorAdding sensorAdding = new LocalToServerSensorAdding();
                Plant plant = plantService.getPlantByCode(plantCode);
                if(plant != null){
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
                                if(cellar != null){
                                    Sensor sensor1 = sensorService.getWorkingSensorByCellar(cellar);
                                    if(sensor1 != null)
                                    {
                                        retString = "窖内已存在设备，请重新录入.";
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

                                        ServerCellar serverCellar = new ServerCellar();
                                        serverCellar.setCellarId(HttpClientUtils.getUUID());
                                        serverCellar.setCellarCode(cellarCode);
                                        serverCellar.setLocalCellarId(cellar.getCellarId());
                                        sensorAdding.setServerCellar(serverCellar);

                                        Sensor sensor2 = new Sensor();
                                        sensor2.setSensorId(HttpClientUtils.getUUID());
                                        sensor2.setSensorCode(sensor.getSensorCode());
                                        sensor2.setFrequencyPoint(sensor.getFrequencyPoint());
                                        sensor2.setWorkRate(sensor.getWorkRate());
                                        sensor2.setActivatePeriod(sensor.getActivatePeriod());
                                        sensor2.setWorkingHours(sensor.getWorkingHours());
                                        sensor2.setCellar(cellar);
                                        sensor2.setBatteryState(sensor.getBatteryState());
                                        sensor2.setCreatedTime(new Date());
                                        sensor2.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                        sensorService.save(sensor2);

                                        ServerSensor serverSensor = new ServerSensor();
                                        serverSensor.setSensorId(HttpClientUtils.getUUID());
                                        serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                                        serverSensor.setWorkingHours(sensor.getWorkingHours());
                                        serverSensor.setWorkRate(sensor.getWorkRate());
                                        serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                                        serverSensor.setSensorCode(sensor.getSensorCode());
                                        serverSensor.setBatteryState(sensor.getBatteryState());
                                        serverSensor.setLocalSensorId(sensor2.getSensorId());
                                        sensorAdding.setServerSensor(serverSensor);

                                        //已删除设备同窖重录
                                        sensorAdding.setSavingType("deleted");

                                        logger.info("Start send sensor value to server.....");
                                        String retInfoSensor = "";
                                        JSONObject jsonObjectServerSensor = (JSONObject) JSONObject.toJSON(sensorAdding);
                                        logger.info("jsonarray:   " + jsonObjectServerSensor);
                                        String centerUrlSensor = serverUrl + localUploadServerValue;
                                        Map<String, String> headParamsSensor = new HashMap<>();
                                        headParamsSensor.put("Content-type", "application/json; charset=utf-8");
                                        headParamsSensor.put("SessionId", HttpClientUtils.getSessionId());
                                        retInfoSensor = HttpClientUtils.getInstance().doPostWithJson(centerUrlSensor, headParamsSensor, jsonObjectServerSensor);
                                        logger.info(">>>>>>>>>>Upload state: " + retInfoSensor);

                                        retString = "异窖信息修改成功.";
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
                                    sensor1.setSensorCode(sensor.getSensorCode());
                                    sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                                    sensor1.setWorkRate(sensor.getWorkRate());
                                    sensor1.setActivatePeriod(sensor.getActivatePeriod());
                                    sensor1.setWorkingHours(sensor.getWorkingHours());
                                    sensor1.setCellar(cellar1);
                                    sensor1.setBatteryState(sensor.getBatteryState());
                                    sensor1.setCreatedTime(new Date());
                                    sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                    sensorService.save(sensor1);

                                    ServerSensor serverSensor = new ServerSensor();
                                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                                    serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                                    serverSensor.setWorkingHours(sensor.getWorkingHours());
                                    serverSensor.setWorkRate(sensor.getWorkRate());
                                    serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                                    serverSensor.setSensorCode(sensor.getSensorCode());
                                    serverSensor.setBatteryState(sensor.getBatteryState());
                                    serverSensor.setLocalSensorId(sensor1.getSensorId());
                                    sensorAdding.setServerSensor(serverSensor);

                                    //普通录入
                                    sensorAdding.setSavingType("normal");

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

                                    retString = "异窖信息修改成功.";
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
                                sensor1.setSensorCode(sensor.getSensorCode());
                                sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                                sensor1.setWorkRate(sensor.getWorkRate());
                                sensor1.setActivatePeriod(sensor.getActivatePeriod());
                                sensor1.setWorkingHours(sensor.getWorkingHours());
                                sensor1.setCellar(cellar1);
                                sensor1.setBatteryState(sensor.getBatteryState());
                                sensor1.setCreatedTime(new Date());
                                sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                                sensorService.save(sensor1);

                                ServerSensor serverSensor = new ServerSensor();
                                serverSensor.setSensorId(HttpClientUtils.getUUID());
                                serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                                serverSensor.setWorkingHours(sensor.getWorkingHours());
                                serverSensor.setWorkRate(sensor.getWorkRate());
                                serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                                serverSensor.setSensorCode(sensor.getSensorCode());
                                serverSensor.setBatteryState(sensor.getBatteryState());
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

                                retString = "异排信息修改成功.";
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
                            sensor1.setSensorCode(sensor.getSensorCode());
                            sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                            sensor1.setWorkRate(sensor.getWorkRate());
                            sensor1.setActivatePeriod(sensor.getActivatePeriod());
                            sensor1.setWorkingHours(sensor.getWorkingHours());
                            sensor1.setCellar(cellar1);
                            sensor1.setBatteryState(sensor.getBatteryState());
                            sensor1.setCreatedTime(new Date());
                            sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                            sensorService.save(sensor1);

                            ServerSensor serverSensor = new ServerSensor();
                            serverSensor.setSensorId(HttpClientUtils.getUUID());
                            serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                            serverSensor.setWorkingHours(sensor.getWorkingHours());
                            serverSensor.setWorkRate(sensor.getWorkRate());
                            serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                            serverSensor.setSensorCode(sensor.getSensorCode());
                            serverSensor.setBatteryState(sensor.getBatteryState());
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

                            retString = "异垮信息修改成功.";
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
                        sensor1.setSensorCode(sensor.getSensorCode());
                        sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                        sensor1.setWorkRate(sensor.getWorkRate());
                        sensor1.setActivatePeriod(sensor.getActivatePeriod());
                        sensor1.setWorkingHours(sensor.getWorkingHours());
                        sensor1.setCellar(cellar1);
                        sensor1.setBatteryState(sensor.getBatteryState());
                        sensor1.setCreatedTime(new Date());
                        sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                        sensorService.save(sensor1);

                        ServerSensor serverSensor = new ServerSensor();
                        serverSensor.setSensorId(HttpClientUtils.getUUID());
                        serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                        serverSensor.setWorkingHours(sensor.getWorkingHours());
                        serverSensor.setWorkRate(sensor.getWorkRate());
                        serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                        serverSensor.setSensorCode(sensor.getSensorCode());
                        serverSensor.setBatteryState(sensor.getBatteryState());
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

                        retString = "异班组信息修改成功.";
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
                    sensor1.setSensorCode(sensor.getSensorCode());
                    sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                    sensor1.setWorkRate(sensor.getWorkRate());
                    sensor1.setActivatePeriod(sensor.getActivatePeriod());
                    sensor1.setWorkingHours(sensor.getWorkingHours());
                    sensor1.setCellar(cellar1);
                    sensor1.setBatteryState(sensor.getBatteryState());
                    sensor1.setCreatedTime(new Date());
                    sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    sensorService.save(sensor1);

                    ServerSensor serverSensor = new ServerSensor();
                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                    serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                    serverSensor.setWorkingHours(sensor.getWorkingHours());
                    serverSensor.setWorkRate(sensor.getWorkRate());
                    serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                    serverSensor.setSensorCode(sensor.getSensorCode());
                    serverSensor.setBatteryState(sensor.getBatteryState());
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

                    retString = "异责任区信息修改成功.";
                    logger.info(retString);
                    return retString;
                }
                }else{
                    Plant plant1 = new Plant();
                    plant1.setPlantCode(plantCode);
                    plant1.setPlantId(HttpClientUtils.getUUID());
                    plant1.setPlantName(plantCode + "车间");
                    int numberX = new Random().nextInt(1000) + 1;
                    int numberY = new Random().nextInt(700) + 1;
                    plant1.setClientX(numberX);
                    plant1.setClientY(numberY);
                    plantService.save(plant1);

                    ServerPlant serverPlant = new ServerPlant();
                    serverPlant.setPlantId(HttpClientUtils.getUUID());
                    serverPlant.setPlantCode(plant1.getPlantCode());
                    serverPlant.setLocalPlantId(plant1.getPlantId());
                    serverPlant.setPlantName(plantCode + "车间");
                    serverPlant.setClientX(numberX);
                    serverPlant.setClientY(numberY);
                    sensorAdding.setServerPlant(serverPlant);

                    Territory territory1 = new Territory();
                    territory1.setTerritoryId(HttpClientUtils.getUUID());
                    territory1.setTerritoryCode(territoryCode);
                    territory1.setPlant(plant1);
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
                    sensor1.setSensorCode(sensor.getSensorCode());
                    sensor1.setFrequencyPoint(sensor.getFrequencyPoint());
                    sensor1.setWorkRate(sensor.getWorkRate());
                    sensor1.setActivatePeriod(sensor.getActivatePeriod());
                    sensor1.setWorkingHours(sensor.getWorkingHours());
                    sensor1.setCellar(cellar1);
                    sensor1.setBatteryState(sensor.getBatteryState());
                    sensor1.setCreatedTime(new Date());
                    sensor1.setSensorWorkingType(SensorWorkingType.SENSOR_IS_WORKING);
                    sensorService.save(sensor1);

                    ServerSensor serverSensor = new ServerSensor();
                    serverSensor.setSensorId(HttpClientUtils.getUUID());
                    serverSensor.setFrequencyPoint(sensor.getFrequencyPoint());
                    serverSensor.setWorkingHours(sensor.getWorkingHours());
                    serverSensor.setWorkRate(sensor.getWorkRate());
                    serverSensor.setActivatePeriod(sensor.getActivatePeriod());
                    serverSensor.setSensorCode(sensor.getSensorCode());
                    serverSensor.setBatteryState(sensor.getBatteryState());
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

                    retString = "异车间信息修改成功.";
                    logger.info(retString);
                    return retString;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            retString =  "修改信息发生错误";
        }
        return retString;
    }

    private String validHexUnit(int i, int num){
        String hex = Integer.toHexString(i);
//转为大写
        hex = hex.toUpperCase();
//加长到四位字符，用0补齐
        while (hex.length() < num) {
            hex = "0" + hex;
        }
        return hex;
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
            byte[] CRC16 = new byte[2];
            System.arraycopy(msgContainer, 24, CRC16, 0, 2);

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
//            long CRC16Code = Long.parseLong(HexUtils.bytesToHexString(workTimeId),16);
            String batteryState1 = HexUtils.bytesToHexString(batteryState);
            double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16))*1.0/10;
            logger.error("The topTemp is [{}]", topTemp1);
            double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp),16))*1.0/10;
            logger.error("The mideTemp is [{}]", midTemp1);
            double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp),16))*1.0/10;
            logger.error("The botTemp is [{}]", botTemp1);
            logger.info("plantCode: " + HexUtils.bytesToHexString(workShopId) + ", " + plantCode);
            logger.info("territoryCode: " + HexUtils.bytesToHexString(turnOutAreaId) + ", " + territoryCode);
            logger.info("teamCode: " + HexUtils.bytesToHexString(teamId) + ", " + teamCode);
            logger.info("pitCode: " + HexUtils.bytesToHexString(collapseId) + ", " + pitCode);
            logger.info("rowCode: " + HexUtils.bytesToHexString(rowId) + ", " + rowCode);
            logger.info("cellarCode: " + HexUtils.bytesToHexString(cellarId) + ", " + cellarCode);
            List<Sensor> sensors = sensorService.getScrollData().getResultList();
            LocalToServerSensorAdding sensorAdding = new LocalToServerSensorAdding();
            //检验当前特征码是否存在
            if(sensors!=null){
                for (Sensor sensor: sensors){
                    //特征码相同且设备启用 返回错误
                    if(sensor.getSensorCode().equals(sensorCode)){
                        if(sensor.getSensorWorkingType() != SensorWorkingType.SENSOR_WAS_DELETED)
                        {
                            retString = "特征码已存在，请勿重复录入.";
                            logger.info(retString);
                            return retString;
                        }else {
                            workingType = "deleted";
                            retString = "重录已删除设备.";
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
                                if(cellar != null){
                                    if(!workingType.equals("deleted"))
                                    {
                                        retString = "窖内已存在设备，请重新录入.";
                                        logger.info(retString);
                                        return retString;
                                    }
                                    else{
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

                                        ServerCellar serverCellar = new ServerCellar();
                                        serverCellar.setCellarId(HttpClientUtils.getUUID());
                                        serverCellar.setCellarCode(cellarCode);
                                        serverCellar.setLocalCellarId(cellar.getCellarId());
                                        sensorAdding.setServerCellar(serverCellar);

                                        Sensor sensor1 = new Sensor();
                                        sensor1.setSensorId(HttpClientUtils.getUUID());
                                        sensor1.setSensorCode(sensorCode);
                                        sensor1.setFrequencyPoint(frequencyPoint);
                                        sensor1.setWorkRate(workRate);
                                        sensor1.setActivatePeriod(activatePeriod);
                                        sensor1.setWorkingHours(workingHours);
                                        sensor1.setCellar(cellar);
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

                                        //已删除设备同窖重录
                                        sensorAdding.setSavingType("deleted");

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

                                    //普通录入
                                    sensorAdding.setSavingType("normal");

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

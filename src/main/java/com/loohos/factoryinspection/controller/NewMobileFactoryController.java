package com.loohos.factoryinspection.controller;

import com.google.gson.Gson;
import com.loohos.factoryinspection.model.formbean.PlantPit;
import com.loohos.factoryinspection.model.formbean.ServerCellarSensor;
import com.loohos.factoryinspection.model.local.Camera;
import com.loohos.factoryinspection.model.local.Pit;
import com.loohos.factoryinspection.model.server.*;
import com.loohos.factoryinspection.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.loohos.factoryinspection.enumeration.SensorType.BOT_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.MID_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.TOP_TEMP_SENSOR;

@Controller
@RequestMapping(value = "/mobile")
public class NewMobileFactoryController {
    @Value("${com.loohos.machineType}")
    private String machineType;

    private Logger logger = LoggerFactory.getLogger(FactoryController.class);
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
    @Resource(name = "cameraServiceImpl") private CameraService cameraService;



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
        return "winery/mobilePage/mobilePlantMap";
    }

    @RequestMapping(value = "/index",produces={"text/html;charset=UTF-8;","application/json;"})
    public String getIndex(ModelMap modelMap,
                           @RequestParam String plantId,
                           HttpServletRequest request,
                           HttpServletResponse response){
        ServerPlant plant = serverPlantService.find(plantId);
        modelMap.put("serverPlant", plant);
        request.getSession().setAttribute("plantId", plantId);

        List<Camera> cameras = cameraService.getCamerasByPlantCode(plant.getPlantCode());
        Map<Integer, List<Camera>> cameraExisted = new HashMap<>();

        if(cameras == null){
            cameraExisted.put(0, cameras);
        }else {
            cameraExisted.put(1, cameras);
        }

        modelMap.put("cameraExisted", cameraExisted);

        List<ServerTerritory> territories = serverTerritoryService.getTerritoriesByPlant(plant);
        List<ServerPit> pits = new ArrayList<>();
        if(territories == null){
            logger.info("mobile no territories, please add one.");
            return "winery/mobilePage/serverPlantInside";
        }
        else {
            for(ServerTerritory territory: territories) {
                List<ServerTeam> teams = serverTeamService.getTeamsByTerritory(territory);
                for (ServerTeam team : teams) {
                    List<ServerPit> existedPits = serverPitService.getPitsByTeam(team);
                    if(existedPits.size() <= 10){
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
                            logger.info("填充垮/");
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
        return "winery/mobilePage/mobilePlantInside";
    }

    @RequestMapping(value = "/cellarContainer",produces={"text/html;charset=UTF-8;","application/json;"})
    public String showCellar(@RequestParam String plantId,
                             @RequestParam String pitId,
                             ModelMap modelMap){
        ServerPlant plant = serverPlantService.find(plantId);
        modelMap.put("serverPlant", plant);
        ServerPit pit = serverPitService.find(pitId);
        modelMap.put("serverPit", pit);
        List<ServerRow> rows = new ArrayList<>();
        List<ServerRow> existedRows = serverRowService.getRowByPit(pit);
        Camera camera = cameraService.getCamerasByPlantAndPit(plant.getPlantCode(), pit.getPitCode());
        Map<Integer, Camera> cameraExisted = new HashMap<>();

        if(camera == null){
            cameraExisted.put(0, null);
        }else {
            cameraExisted.put(1, camera);
        }
        modelMap.put("cameraExisted", cameraExisted);
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
                logger.info("填充排:");
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
                    logger.info("填充项:");
                }
            }else{
                List<ServerCellar> existedCellars = serverCellarService.getCellarByRowDesc(row);
                here:
                for (int i = 1; i <= 12; i++) {
                    for (ServerCellar cellar: existedCellars) {
                        if(cellar.getCellarCode() == i){
                            logger.info("正常项:");
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
        return "winery/mobilePage/mobileCellarContainer";
    }


    @RequestMapping(value = "/mobileCurveDetail",produces={"text/html;charset=UTF-8;","application/json;"})
    public String serverCurveDetail(@RequestParam String plantId,
                                    @RequestParam String pitId,
                                    @RequestParam String sensorId,
                                    ModelMap modelMap){
        ServerSensor sensor = serverSensorService.find(sensorId);
        modelMap.put("sensorId", sensorId);
        ServerPlant serverPlant = serverPlantService.find(plantId);
        ServerPit serverPit = serverPitService.find(pitId);
        modelMap.put("serverPlant", serverPlant);
        modelMap.put("serverPit", serverPit);
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
        return "winery/mobilePage/mobileCurve";
    }

}

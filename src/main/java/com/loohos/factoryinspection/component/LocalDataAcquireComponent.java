package com.loohos.factoryinspection.component;

import com.alibaba.fastjson.JSONObject;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.enumeration.SerialPortType;
import com.loohos.factoryinspection.model.formbean.LocalToServerSensorNodes;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.model.server.ServerSensorNode;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.CommonUtils;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.HttpClientUtils;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.BlockingQueue;

@EnableScheduling
@Component
public class LocalDataAcquireComponent  {
    @Value("${com.loohos.machineType}")
    private String machineType;
    @Value("${com.loohos.serverUrl}")
    private String serverUrl;
    @Value("${com.loohos.loaduploadvalue}")
    private String loaduploadvalue;
    @Value("${com.loohos.newUploadValueUrl}")
    private String newUploadValueUrl;
    //遍历厂房所有设备，发送485指令，将数据存在local端
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;
    @Resource(name = "configAlarmLevelServiceImpl") private ConfigAlarmLevelService configAlarmLevelService;
    @Resource(name = "plantServiceImpl") private PlantService plantService;
    @Resource(name = "territoryServiceImpl") private TerritoryService territoryService;
    @Resource(name = "teamServiceImpl") private TeamService teamService;
    @Resource(name = "pitServiceImpl") private PitService pitService;
    @Resource(name = "rowServiceImpl") private RowService rowService;
    @Resource(name = "cellarServiceImpl") private CellarService cellarService;
    @Resource(name = "sensorNodeServiceImpl") private SensorNodeService sensorNodeService;
    @Resource(name = "sensorServiceImpl") private SensorService sensorService;
    private SerialCommThread commThread = new SerialCommThread();
    private boolean FLAG;


    @PostConstruct
    public void initLocalConfig(){
        FLAG = true;
        if(!machineType.equals("local")){
            return;
        }
        logger.info("Begin load LocalConfig");
    }

//    @Scheduled(cron = "10 */1 * * * ?")
    @Scheduled(cron = "30 */10 * * * ?")
    private void acquireSensorInfo() {
        if(FLAG){
            List<Sensor> sensors = sensorService.getScrollData().getResultList();
            if(sensors.size() > 0){
                logger.info("Start acquire sensor info......");
            }else {
                logger.info("无设备，添加后开始取值.");
                return;
            }
        }else {
            logger.info("The cycle is not terminate...");
        }
        FLAG = false;
        if (!machineType.equals("local")) {
            logger.info("The machineType is :" + machineType);
            return;
        }
        logger.info(">>>>>>>>>>>>>start to a save local Sensor<<<<<<<<<<<<<<<");
        if (commThread.isAlive()) {
            getSensorInfo();
            logger.info("thread is alive.");
        } else {
            boolean started = commThread.startCommPort(SerialPortType.COM3.getDescription(), 9600, 8, 1, 0);
            if (started) {
                commThread.start();
                getSensorInfo();
                logger.info("get info from a new commThread.");
            } else {
                getSensorInfo();
                logger.info("get info from a started thread.");
            }
        }
    }

    private void getSensorInfo() {
        String retString = "";
        final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
        final String rsPartitionSendCode = "00"; //分割发送码
        final String rsSufMessagefix = "AB031000000F1904"; //信息层
        final String rsSuffix = "CCCC";
        List<Sensor> sensors = sensorService.getScrollData().getResultList();
        //遍历获取所有的终端信息
        for(Sensor sensor : sensors)
        {
            if(sensor.getSensorWorkingType().getDescription().equals("SENSOR_IS_DELETE")){
                logger.info("This sensor is deleted...");
                continue;
            }
            String strMessage = rsPrefix + sensor.getSensorCode() + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
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
                    logger.info("异常设备取值");
                    retString = "当前录入设备异常，请及时处理.";
                    SensorNode topNode = new SensorNode();

                    topNode.setAlarmLevel(4);
                    topNode.setSensorType(SensorType.TOP_TEMP_SENSOR);
                    topNode.setSensorValue(3276.7);
                    topNode.setSensor(sensor);
                    topNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topNode.setCreatedTime(new Date());

                    SensorNode midNode = new SensorNode();
                    midNode.setAlarmLevel(4);
                    midNode.setSensorType(SensorType.MID_TEMP_SENSOR);
                    midNode.setSensorValue(3276.7);
                    midNode.setSensor(sensor);
                    midNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midNode.setCreatedTime(new Date());

                    SensorNode botNode = new SensorNode();
                    botNode.setAlarmLevel(4);
                    botNode.setSensorType(SensorType.BOT_TEMP_SENSOR);
                    botNode.setSensorValue(3276.7);
                    botNode.setSensor(sensor);
                    botNode.setSensorNodeId(HttpClientUtils.getUUID());
                    botNode.setCreatedTime(new Date());

                    sensor.setAlarmLevel(4);
                    sensorService.update(sensor);
                    sensorNodeService.save(topNode);
                    sensorNodeService.save(midNode);
                    sensorNodeService.save(botNode);

                    logger.info("Start send a terminal value to server.....");
                    String retInfo = "";
                    LocalToServerSensorNodes nodes = new LocalToServerSensorNodes();
                    ServerSensorNode topServerNode = new ServerSensorNode();
                    topServerNode.setSensorValue(3276.7);
                    topServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topServerNode.setCreatedTime(new Date());
                    nodes.setTopNode(topServerNode);

                    ServerSensorNode midServerNode = new ServerSensorNode();
                    midServerNode.setAlarmLevel(4);
                    midServerNode.setSensorValue(3276.7);
                    midServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midServerNode.setCreatedTime(new Date());
                    nodes.setMidNode(midServerNode);

                    ServerSensorNode botServerNode = new ServerSensorNode();
                    botServerNode.setAlarmLevel(4);
                    botServerNode.setSensorValue(3276.7);
                    botServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    botServerNode.setCreatedTime(new Date());
                    nodes.setBotNode(botServerNode);

                    nodes.setLocalSensorId(sensor.getSensorId());

                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(nodes);
                    logger.info("jsonarray:   "+ jsonObject);
                    String centerUrl = serverUrl+newUploadValueUrl;
                    Map<String, String> headParams = new HashMap<>();
                    headParams.put("Content-type", "application/json; charset=utf-8");
                    headParams.put("SessionId", HttpClientUtils.getSessionId());
                    retInfo = HttpClientUtils.getInstance().doPostWithJson(centerUrl, headParams, jsonObject);
                    logger.info(">>>>>>>>>>Upload state: " + retInfo);

                    continue;
                }
                byte[] terminalId = new byte[6];
                System.arraycopy(respMessage, 26, terminalId, 0, 6);
                System.out.println(HexUtils.bytesToHexString(terminalId));
                byte[] msgContainer = new byte[26];
                System.arraycopy(respMessage, 32, msgContainer, 0, 26);
                System.out.println(HexUtils.bytesToHexString(msgContainer));

                byte[] topTemp = new byte[2];
                System.arraycopy(msgContainer, 16, topTemp, 0, 2);
                byte[] midTemp = new byte[2];
                System.arraycopy(msgContainer, 18, midTemp, 0, 2);
                byte[] botTemp = new byte[2];
                System.arraycopy(msgContainer, 20, botTemp, 0, 2);

                double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16)) * 1.0 / 10;
                logger.error("The topTemp is [{}]", topTemp1);
                double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp), 16)) * 1.0 / 10;
                logger.error("The mideTemp is [{}]", midTemp1);
                double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp), 16)) * 1.0 / 10;
                logger.error("The botTemp is [{}]", botTemp1);
                SensorNode topNode = new SensorNode();
                SensorNode midNode = new SensorNode();
                SensorNode botNode = new SensorNode();
                ServerSensorNode topServerNode = new ServerSensorNode();
                ServerSensorNode midServerNode = new ServerSensorNode();
                ServerSensorNode botServerNode = new ServerSensorNode();

                //比较新增值与阈值的关系， 来设置alarmLevel
                int topAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(topTemp1);
                int midAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(midTemp1);
                int botAlarmLevel = configAlarmLevelService.getIsOrNotUnderRange(botTemp1);
                logger.info("Start acquiring sensor info....");
                if(topTemp1 == 3276.7 || topTemp1 == 0){
                    topAlarmLevel = 4;
                    topNode.setAlarmLevel(topAlarmLevel);
                    topNode.setSensorType(SensorType.TOP_TEMP_SENSOR);
                    topNode.setSensorValue(3276.7);
                    topNode.setSensor(sensor);
                    topNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topNode.setCreatedTime(new Date());
                    sensorNodeService.save(topNode);
                    logger.info("Top sensor was ANOMALY...");

                    topServerNode.setAlarmLevel(topAlarmLevel);
                    topServerNode.setSensorValue(3276.7);
                    topServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topServerNode.setCreatedTime(new Date());
                }else {
                    topNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topNode.setSensorType(SensorType.TOP_TEMP_SENSOR);
                    topNode.setSensorValue(topTemp1);
                    topNode.setSensor(sensor);
                    topNode.setCreatedTime(new Date());
                    topNode.setAlarmLevel(topAlarmLevel);
                    sensorNodeService.save(topNode);
                    logger.info("Top sensor was OPERATING...");

                    topServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    topServerNode.setSensorValue(topTemp1);
                    topServerNode.setCreatedTime(new Date());
                    topServerNode.setAlarmLevel(topAlarmLevel);
                }
                if(midTemp1 == 3276.7 || midTemp1 == 0){
                    midAlarmLevel = 4;
                    midNode.setAlarmLevel(midAlarmLevel);
                    midNode.setSensorType(SensorType.MID_TEMP_SENSOR);
                    midNode.setSensorValue(3276.7);
                    midNode.setSensor(sensor);
                    midNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midNode.setCreatedTime(new Date());
                    sensorNodeService.save(midNode);
                    logger.info("Mid sensor was ANOMALY...");

                    midServerNode.setAlarmLevel(midAlarmLevel);
                    midServerNode.setSensorValue(3276.7);
                    midServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midServerNode.setCreatedTime(new Date());
                }else {
                    midNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midNode.setSensorType(SensorType.MID_TEMP_SENSOR);
                    midNode.setSensorValue(midTemp1);
                    midNode.setSensor(sensor);
                    midNode.setCreatedTime(new Date());
                    midNode.setAlarmLevel(midAlarmLevel);
                    sensorNodeService.save(midNode);
                    logger.info("Top sensor was OPERATING...");

                    midServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    midServerNode.setSensorValue(midTemp1);
                    midServerNode.setCreatedTime(new Date());
                    midServerNode.setAlarmLevel(midAlarmLevel);
                }
                if(botTemp1 == 3276.7 || botTemp1 == 0){
                    botAlarmLevel = 4;
                    botNode.setAlarmLevel(botAlarmLevel);
                    botNode.setSensorType(SensorType.BOT_TEMP_SENSOR);
                    botNode.setSensorValue(3276.7);
                    botNode.setSensor(sensor);
                    botNode.setSensorNodeId(HttpClientUtils.getUUID());
                    botNode.setCreatedTime(new Date());
                    sensorNodeService.save(botNode);
                    logger.info("Bot sensor was ANOMALY...");

                    botServerNode.setAlarmLevel(botAlarmLevel);
                    botServerNode.setSensorValue(3276.7);
                    botServerNode.setCreatedTime(new Date());
                    botServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                }else {
                    botNode.setSensorNodeId(HttpClientUtils.getUUID());
                    botNode.setSensorType(SensorType.BOT_TEMP_SENSOR);
                    botNode.setSensorValue(botTemp1);
                    botNode.setSensor(sensor);
                    botNode.setCreatedTime(new Date());
                    botNode.setAlarmLevel(botAlarmLevel);
                    sensorNodeService.save(botNode);
                    logger.info("Bot sensor was OPERATING...");

                    botServerNode.setSensorNodeId(HttpClientUtils.getUUID());
                    botServerNode.setSensorValue(botTemp1);
                    botServerNode.setCreatedTime(new Date());
                    botServerNode.setAlarmLevel(botAlarmLevel);
                }
                int sensorAlarmLevel = CommonUtils.comparingMaxAlarmLevel(topAlarmLevel,midAlarmLevel,botAlarmLevel);
                sensor.setAlarmLevel(sensorAlarmLevel);
                sensorService.update(sensor);
                logger.info("Acquired sensor info success.....");
                //本次循环取值结束 开始上传内容至服务器
                logger.info("Start send a terminal value to server.....");
                String retInfo = "";
                LocalToServerSensorNodes nodes = new LocalToServerSensorNodes();
                nodes.setLocalSensorId(sensor.getSensorId());
                nodes.setTopNode(topServerNode);
                nodes.setMidNode(midServerNode);
                nodes.setBotNode(botServerNode);

                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(nodes);
                logger.info("jsonarray:   "+ jsonObject);
                String centerUrl = serverUrl+newUploadValueUrl;
                Map<String, String> headParams = new HashMap<>();
                headParams.put("Content-type", "application/json; charset=utf-8");
                headParams.put("SessionId", HttpClientUtils.getSessionId());
                retInfo = HttpClientUtils.getInstance().doPostWithJson(centerUrl, headParams, jsonObject);
                logger.info(">>>>>>>>>>Upload state: " + retInfo);

                logger.info("End upload info....");
            }catch (Exception e){
                e.printStackTrace();
                logger.info(e.toString());
            }
            }
        logger.info("The cycle is terminate.....");
        //重置标识 准备开始下一次循环
        FLAG = true;
    }



//    @Scheduled(cron = "10 */5 * * * ?")
//    public void threadStart(){
//        String localFlag = machineType;
//        System.out.println("localFlag is : " + localFlag);
//        if(!localFlag.equals("local")){
//            return;
//        }
//        logger.info(">>>>>>>>>>>>>period start to a save local Sensor<<<<<<<<<<<<<<<");
//        if(commThread.isAlive()){
//            getInfoFromTerminal();
//            logger.info("thread is alive.");
//        }else{
//            boolean started = commThread.startCommPort("COM3", 9600, 8, 1, 0);
//            if(started){
//                commThread.start();
//                getInfoFromTerminal();
//                logger.info("get info from a new commThread.");
//            }else{
//                getInfoFromTerminal();
//                logger.info("get info from a started thread.");
//            }
//        }
//    }
//
//
//    private void getInfoFromTerminal(){
//        LocalSite localSite = new LocalSite();
//        Set<LocalSite> localPlants = new HashSet<>();
//        localSite.setChileSite(localPlants);
//
//        List<Terminal> terminals = terminalService.getScrollData().getResultList();
//        for(Terminal terminal:terminals)
//        {
//            if(!terminal.getInUsing()){
//                logger.info("found banned terminal: " + terminal.getTerminalCode() );
//                continue;
//            }
//            final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
//            String rsMidTerminalfix = terminal.getTerminalCode(); //终端ID
//            final String rsPartitionSendCode = "00"; //分割发送码
//            final String rsSufMessagefix = "AB031000000F1904"; //信息层
//            final String rsSuffix = "CCCC";
//            String strMessage = rsPrefix + rsMidTerminalfix + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
////        strMessage = "7E380FB08FD08CC72300AB031000000F1904CCCC";
//            byte[] byteMessage = HexUtils.hexStringToByte(strMessage.trim());
//            commThread.write(byteMessage);
//            logger.info("Rs发送数据：" + HexUtils.bytesToHexString(byteMessage));
//
//            try {
//                Thread.sleep(4000);
//                BlockingQueue<byte[]> respList = commThread.getResponseQueue();
//
//                StringBuilder sb = new StringBuilder();
//                while (respList.size() > 0){
//                    sb.append(HexUtils.bytesToHexString(respList.take()));
//                }
//
//                logger.info("rs接收数据Response：" + sb.toString());
//
//                byte[] respMessage = HexUtils.hexStringToByte(sb.toString());
//                logger.info("rs接收数据长度： " + respMessage.length);
//                if(respMessage.length != 60){
//                    logger.info("terminal occur a problem, try to save a zero value");
//                    TerminalValueSensor sensor = new TerminalValueSensor();
//                    sensor.setSensorId(HttpClientUtils.getUUID());
//                    sensor.setTopTemp(3276.7);
//                    sensor.setMidTemp(3276.7);
//                    sensor.setBotTemp(3276.7);
//                    sensor.setTopAlarmLevel(3);
//                    sensor.setMidAlarmLevel(3);
//                    sensor.setBotAlarmLevel(3);
//                    sensor.setCreatedTime(new Date());
//                    sensor.setTerminalId(terminal);
//                    sensor.setBatteryState("00");
//                    terminalValueSensorService.save(sensor);
//                    continue;
//                }
//
//                byte[] terminalId = new byte[6];
//                System.arraycopy(respMessage, 26, terminalId, 0, 6);
//                System.out.println(HexUtils.bytesToHexString(terminalId));
//                byte[] msgContainer = new byte[26];
//                System.arraycopy(respMessage, 32, msgContainer, 0, 26);
//                System.out.println(HexUtils.bytesToHexString(msgContainer));
//
//                byte[] workShopId = new byte[2];
//                System.arraycopy(msgContainer, 0, workShopId, 0, 2);
//                byte[] turnOutAreaId = new byte[2];
//                System.arraycopy(msgContainer, 2, turnOutAreaId, 0, 2);
//                byte[] teamId = new byte[2];
//                System.arraycopy(msgContainer, 4, teamId, 0, 2);
//                byte[] collapseId = new byte[1];
//                System.arraycopy(msgContainer, 7, collapseId, 0, 1);
//                byte[] rowId = new byte[1];
//                System.arraycopy(msgContainer, 8, rowId, 0, 1);
//                byte[] cellarId = new byte[1];
//                System.arraycopy(msgContainer, 9, cellarId, 0, 1);
//                byte[] frenquencePointId = new byte[1];
//                System.arraycopy(msgContainer, 10, frenquencePointId, 0, 1);
//                byte[] workRateId = new byte[1];
//                System.arraycopy(msgContainer, 11, workRateId, 0, 1);
//                byte[] periodId = new byte[2];
//                System.arraycopy(msgContainer, 12, periodId, 0, 2);
//                byte[] workTimeId = new byte[2];
//                System.arraycopy(msgContainer, 14, workTimeId, 0, 2);
//                byte[] topTemp = new byte[2];
//                System.arraycopy(msgContainer, 16, topTemp, 0, 2);
//                byte[] midTemp = new byte[2];
//                System.arraycopy(msgContainer, 18, midTemp, 0, 2);
//                byte[] botTemp = new byte[2];
//                System.arraycopy(msgContainer, 20, botTemp, 0, 2);
//                byte[] batteryState = new byte[1];
//                System.arraycopy(msgContainer, 23, batteryState, 0, 1);
//                byte[] CRC16 = new byte[2];
//                System.arraycopy(msgContainer, 24, CRC16, 0, 2);
//                double topTemp1 = (Long.parseLong(HexUtils.bytesToHexString(topTemp), 16))*1.0/10;
//                System.out.println(topTemp1);
//                double midTemp1 = (Long.parseLong(HexUtils.bytesToHexString(midTemp),16))*1.0/10;
//                System.out.println(midTemp1);
//                double botTemp1 = (Long.parseLong(HexUtils.bytesToHexString(botTemp),16))*1.0/10;
//                System.out.println(botTemp1);
//                String batteryState1 = HexUtils.bytesToHexString(batteryState);
//                TerminalValueSensor sensor = new TerminalValueSensor();
//                sensor.setSensorId(HttpClientUtils.getUUID());
//                sensor.setTopTemp(topTemp1);
//                sensor.setMidTemp(midTemp1);
//                sensor.setBotTemp(botTemp1);
//                int topAlarmLevel = configAlarmLevelService.getLevelByTemp(topTemp1);
//                int midAlarmLevel = configAlarmLevelService.getLevelByTemp(midTemp1);
//                int botAlarmLevel = configAlarmLevelService.getLevelByTemp(botTemp1);
//                sensor.setTopAlarmLevel(topAlarmLevel);
//                sensor.setMidAlarmLevel(midAlarmLevel);
//                sensor.setBotAlarmLevel(botAlarmLevel);
//                sensor.setCreatedTime(new Date());
//                sensor.setTerminalId(terminal);
//                sensor.setBatteryState(batteryState1);
//                terminalValueSensorService.save(sensor);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        logger.info("End getting info, start to sending info to server");
//        //获取完毕 直接上传至服务器
//        String localConfig = machineType;
//        if(!localConfig.equals("local")){
//            return;
//        }
//        logger.info("Start send a terminal value");
//        String retInfo = "";
//        List<TerminalValueSensor> sensors = getLatestTerminal();
//        if(sensors.isEmpty()) return;
////        Gson gson = new Gson();
////        JsonObject deviceJson = gson.fromJson(gson.toJson(sensor), JsonObject.class);
//        JSONArray deviceJson = (JSONArray) JSONArray.toJSON(sensors);
//        logger.info("jsonarray:   "+deviceJson);
//        String centerUrl = serverUrl+loaduploadvalue;
//        Map<String, String> headParams = new HashMap<>();
//        headParams.put("Content-type", "application/json; charset=utf-8");
//        headParams.put("SessionId", HttpClientUtils.getSessionId());
//        retInfo = HttpClientUtils.getInstance().doPostWithJsonArray(centerUrl, headParams, deviceJson);
//        if(retInfo.trim().equals("")){
//            logger.error("Upload to center server failed. ");
//        }else {
//            logger.info(">>>>>>>>>>>>> retinfo is : " + retInfo);
//        }
//
//        logger.info("End upload info");
//
//    }
//
//    private List<TerminalValueSensor> getLatestTerminal(){
//        List<Terminal> terminals = terminalService.getScrollData().getResultList();
//        List<TerminalValueSensor> sensors = new ArrayList<>();
//        for(Terminal terminal : terminals)
//        {
//            TerminalValueSensor sensor = terminalValueSensorService.getLatestSensorByTerminalId(terminal);
//            sensor.setSensorId(HttpClientUtils.getUUID());
//            sensors.add(sensor);
//        }
//        System.out.println(sensors);
//        return sensors;
//    }
//
}

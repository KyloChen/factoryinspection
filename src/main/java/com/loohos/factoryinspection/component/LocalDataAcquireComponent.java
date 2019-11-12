package com.loohos.factoryinspection.component;

import com.loohos.factoryinspection.model.local.LocalSite;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;
import com.loohos.factoryinspection.service.FactoryService;
import com.loohos.factoryinspection.service.TerminalService;
import com.loohos.factoryinspection.service.TerminalValueSensorService;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@EnableScheduling
@Component
public class LocalDataAcquireComponent  {
    @Value("${com.loohos.machineType}")
    private String machineType;
    //遍历厂房所有设备，发送485指令，将数据存在local端
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    @Resource(name = "factoryServiceImpl") private FactoryService factoryService;
    @Resource(name = "terminalServiceImpl") private TerminalService terminalService;
    @Resource(name = "terminalValueSensorServiceImpl") private TerminalValueSensorService terminalValueSensorService;

    SerialCommThread commThread = new SerialCommThread();

    @PostConstruct
    public void initLocalConfig(){
        logger.info("Begin load LocalConfig");

    }


    @Scheduled(cron = "10 */5 * * * ?")
    public void threadStart(){
        String localFlag = machineType;
        System.out.println("localFlag is : " + localFlag);
        if(!localFlag.equals("local")){
            return;
        }
        logger.info(">>>>>>>>>>>>>period start to a save local Sensor<<<<<<<<<<<<<<<");

        if(commThread.isAlive()){
            getInfoFromTerminal();
            logger.info("thread is alive.");
        }else{
            boolean started = commThread.startCommPort("COM3", 9600, 8, 1, 0);
            if(started){
                commThread.start();
                getInfoFromTerminal();
                logger.info("get info from a new commThread.");
            }else{
                getInfoFromTerminal();
                logger.info("get info from a started thread.");
            }
        }
    }


    private void getInfoFromTerminal(){
        LocalSite localSite = new LocalSite();
        Set<LocalSite> localPlants = new HashSet<>();
        localSite.setChileSite(localPlants);

        List<Terminal> terminals = terminalService.getScrollData().getResultList();
        for(Terminal terminal:terminals)
        {
            final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
            String rsMidTerminalfix = terminal.getTerminalCode(); //终端ID
            final String rsPartitionSendCode = "00"; //分割发送码
            final String rsSufMessagefix = "AB031000000F1904"; //信息层
            final String rsSuffix = "CCCC";
            String strMessage = rsPrefix + rsMidTerminalfix + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
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
                    logger.info("terminal occur a problem, try to save a zero value");
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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

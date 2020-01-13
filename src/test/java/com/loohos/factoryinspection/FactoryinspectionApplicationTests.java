package com.loohos.factoryinspection;

import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.enumeration.SerialPortType;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.CRC16Util;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@SpringBootTest
class FactoryinspectionApplicationTests {
	private Logger logger = LoggerFactory.getLogger(FactoryinspectionApplicationTests.class);
	private SerialCommThread commThread = new SerialCommThread();
	@Resource(name = "plantServiceImpl") private PlantService plantService;
	@Resource(name = "territoryServiceImpl") private TerritoryService territoryService;
	@Resource(name = "teamServiceImpl") private TeamService teamService;
	@Resource(name = "pitServiceImpl") private PitService pitService;
	@Resource(name = "rowServiceImpl") private RowService rowService;
	@Resource(name = "cellarServiceImpl") private CellarService cellarService;
	@Resource(name = "sensorNodeServiceImpl") private SensorNodeService sensorNodeService;
	@Resource(name = "sensorServiceImpl") private SensorService sensorService;
	@Test
	void contextLoads() {
		if(commThread.isAlive()){
			getSensorInfo();
		}else{
			boolean started = commThread.startCommPort(SerialPortType.COM3.getDescription(), 9600, 8, 1, 0);
			if(started){
				commThread.start();
				getSensorInfo();
			}else{
				getSensorInfo();
			}
		}
		}

		private void getSensorInfo(){
			String retString = "";
			String workingType = "";
//			final String rsPrefix = "7E380F"; //标志字符7E 帧格式38(发送) 帧长度0F
//			final String rsPartitionSendCode = "00"; //分割发送码
//			final String rsSufMessagefix = "AB031000000F1904"; //信息层
			final String rsSuffix = "CCCC";
//			String strMessage = rsPrefix + "0A0707070A0A" + rsPartitionSendCode + rsSufMessagefix + rsSuffix;
			String rsSufMessagefix = "AB10100300050A000100010001FF010108";
			int CRC = CRC16Util.calcCrc16(HexUtils.hexStringToByte(rsSufMessagefix));
			String CRCCODE = HexUtils.bytesToHexString(CRC16Util.intToBytes(CRC));
			String strMessage = "7E381A0A0707070A0A00" + rsSufMessagefix + CRCCODE + rsSuffix;
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
				String CRC16Code = HexUtils.bytesToHexString(CRC16);
				logger.info(CRC16Code);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	@Test
	void validHexUnit(){
	int i = 905;
		//将十进制数转为十六进制数
		String hex = Integer.toHexString(i);
//转为大写
		hex = hex.toUpperCase();
//加长到四位字符，用0补齐
		while (hex.length() < 4) {
			hex = "0" + hex;
		}
	System.out.println(hex);
//	if(i < 16) {
//		String result = i < 10 ? String.format("%02d", i) :
//				"0" + Integer.toString(i, 16).toUpperCase();
//
//		System.out.println(HexUtils.stringToHexString(result));
//	}else {
//		String result = Integer.toHexString(i);
//		System.out.println(HexUtils.stringToHexString(result));
//	}
	}
}

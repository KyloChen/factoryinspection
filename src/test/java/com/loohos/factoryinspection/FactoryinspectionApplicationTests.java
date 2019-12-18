package com.loohos.factoryinspection;

import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.HttpClientUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
class FactoryinspectionApplicationTests {
	Logger logger = LoggerFactory.getLogger(FactoryinspectionApplicationTests.class);
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
//		Plant plant = new Plant();
//		plant.setPlantCode(1);
//		String sensorCode = "FFFFFF";
//		int plantCode = 1;
//		int territoryCode = 1;
//		int teamCode = 1;
//		int pitCode = 5;
//		int rowCode = 1;
//		int cellarCode = 1;
//		int frequencyPoint = 1;
//		int workRate = 1;
//		int activatePeriod = 1;
//		int workingHours = 1;
//		int batteryState1 = 1;
//		double topTemp1 = 52.0;
//		logger.error("The topTemp is [{}]", topTemp1);
//		double midTemp1 = 58.0;
//		logger.error("The mideTemp is [{}]", midTemp1);
//		double botTemp1 = 49.0;
//		logger.error("The botTemp is [{}]", botTemp1);
////            terminal1.setTerminalId(HttpClientUtils.getUUID());
//		if(plantCode == 0){
//			logger.info("1设备未出厂设置");
//			retMap.put("code", 0);
//			return retMap;
//		} else if(plantCode != plant.getPlantCode()){
//			logger.info("本设备不属于 " + plant.getPlantName() + " , 1请确认后录入。");
//			retMap.put("code", 0);
//			return retMap;
//		} else if(plantCode == plant.getPlantCode()){
//			//plant与当前打开页面session中的plant作对比
//			Pit pit = pitService.getPitByPlantAndPitCode(plant, pitCode);
//			if (pit != null){
//				logger.info("1垮号已存在，进入下一层比较.");
//			} else{
//				//同plant不同垮pit
//				Territory territory = new Territory();
//				if(sensor.getTerritoryCode() > 0){
//					territory.setTerritoryId(HttpClientUtils.getUUID());
//					territory.setTerritoryCode(sensor.getTerritoryCode());
//					territory.setPlant(plant);
//					territoryService.save(territory);
//				}
//				Team team = new Team();
//				if(sensor.getTeamCode() > 0){
//					team.setTerritory(territory);
//					team.setTeamCode(sensor.getTeamCode());
//					team.setTeamId(HttpClientUtils.getUUID());
//					team.setTerritory(territory);
//					teamService.save(team);
//				}
//
//				Pit p = new Pit();
//				p.setPitId(HttpClientUtils.getUUID());
//				p.setPitCode(pitCode);
//				p.setPlant(plant);
//				pit.setTeam(team);
//				pitService.save(p);
//
//				Row r = new Row();
//				r.setRowId(HttpClientUtils.getUUID());
//				r.setRowCode(rowCode);
//				r.setPit(p);
//				rowService.save(r);
//
//				Cellar c = new Cellar();
//				c.setCellarId(HttpClientUtils.getUUID());
//				c.setCellarCode(cellarCode);
//				c.setRow(r);
//				cellarService.save(c);
//
//				Sensor s = new Sensor();
//				s.setSensorId(HttpClientUtils.getUUID());
//				s.setSensorCode(sensorCode);
//				s.setFrequencyPoint(frequencyPoint);
//				s.setWorkRate(workRate);
//				s.setActivatePeriod(activatePeriod);
//				s.setWorkingHours(workingHours);
//				s.setCellar(c);
//				s.setBatteryState(batteryState1);
//				s.setInUsing(true);
//				s.setCreatedTime(new Date());
//				sensorService.save(s);
//
//				SensorNode topNode = new SensorNode();
//				SensorNode midNode = new SensorNode();
//				SensorNode botNode = new SensorNode();
//
//				topNode.setSensorNodeId(HttpClientUtils.getUUID());
//				topNode.setSensorType(SensorType.TOP_TEMP_SENSOR);
//				topNode.setSensorValue(topTemp1);
//				topNode.setSensor(s);
//				topNode.setCreatedTime(new Date());
//				sensorNodeService.save(topNode);
//
//				midNode.setSensorNodeId(HttpClientUtils.getUUID());
//				midNode.setSensorType(SensorType.MID_TEMP_SENSOR);
//				midNode.setSensorValue(midTemp1);
//				midNode.setSensor(s);
//				midNode.setCreatedTime(new Date());
//				sensorNodeService.save(midNode);
//
//				botNode.setSensorNodeId(HttpClientUtils.getUUID());
//				botNode.setSensorType(SensorType.BOT_TEMP_SENSOR);
//				botNode.setSensorValue(botTemp1);
//				botNode.setSensor(s);
//				botNode.setCreatedTime(new Date());
//				sensorNodeService.save(botNode);
//
//				logger.info("1信息录入成功.");
//				retMap.put("code", 1);
//			}
//
//		}
		}
	}

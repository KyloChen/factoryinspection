package com.loohos.factoryinspection;

import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.enumeration.SerialPortType;
import com.loohos.factoryinspection.model.local.*;
import com.loohos.factoryinspection.service.*;
import com.loohos.factoryinspection.utils.CRC16Util;
import com.loohos.factoryinspection.utils.HexUtils;
import com.loohos.factoryinspection.utils.SerialCommThread;
import org.junit.Test;
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
}

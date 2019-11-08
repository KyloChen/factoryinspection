package com.loohos.factoryinspection.utils;

import gnu.io.*;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SerialCommThread extends Thread implements SerialPortEventListener {
    private static Logger logger = Logger.getLogger(SerialCommThread.class);
    private static CommPortIdentifier portId;
    private Enumeration<?> portList;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static SerialPort serialPort;
    private static BlockingQueue<byte[]> responseQueue = new LinkedBlockingDeque<>();
    private volatile static boolean portInUse = false;
    /**
     *
     * @param serialPortEvent SerialPortEventListener的方法，持续监听端口上是否有数据流
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        switch (serialPortEvent.getEventType()){
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] readBuffer = new byte[1024];
                try{
                    int numBytes = -1;
                    while ((numBytes = inputStream.read(readBuffer)) > 0){
                        byte[] content = new byte[numBytes];
                        System.arraycopy(readBuffer, 0, content, 0, numBytes);
                        responseQueue.add(content);
                        //readBuffer = new byte[100]; //重新构造缓冲对象，否则影响下次接收
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
        }
    }

    public boolean startCommPort(String portName, int baudRate, int dataBits, int stopBits, int parity ){
        if(baudRate < 2400) baudRate = 2400;
        if(dataBits < 1) dataBits = SerialPort.DATABITS_8;
        if(stopBits < 1) stopBits = SerialPort.STOPBITS_1;
        if(parity < 1) parity = SerialPort.PARITY_NONE;
        //通过串口管理类获取当前连接上的串口列表
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()){
            //获取相应的串口对象
            portId = (CommPortIdentifier)portList.nextElement();
            //System.out.println("设备类型： " + portId.getPortType());
            //System.out.println("设备名称： " + portId.getName());
            //判断端口类型是否为串口
            if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL){
                //打开COM
                if(portId.getName().equalsIgnoreCase(portName)){
                    try{
                        String portNum = portName.substring(3);
                        String openPortName = "COM_" + portNum + "";
                        System.out.println("Open port is: " + openPortName);
                        serialPort = (SerialPort)portId.open(openPortName, 2000);
                        portInUse = true;
                        System.out.println("Open port success. ");
                    }catch (PortInUseException e){
                        e.printStackTrace();
                        return false;
                    }
                    //设置当前串口的输入输出值
                    try{
                        inputStream = serialPort.getInputStream();
                        outputStream = serialPort.getOutputStream();
                    }catch (IOException e){
                        e.printStackTrace();
                        return false;
                    }

                    //为当前串口添加一个监听器
                    try{
                        serialPort.addEventListener(this);
                    }catch (TooManyListenersException e){
                        e.printStackTrace();
                        return false;
                    }
                    //设置监听器有效，即：有数据时通知
                    serialPort.notifyOnDataAvailable(true); //串口有数据监听
                    //serialPort.notifyOnBreakInterrupt(true); //中断事件监听

                    //设置串口的一些读写参数
                    try{
                        //比特率，数据位，停止位，奇偶校验位
                        serialPort.setSerialPortParams(baudRate,
                                dataBits,
                                stopBits,
                                parity);
                    }catch (UnsupportedCommOperationException e){
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void run(){
        try{
            System.out.println("----------运行任务处理线程-----------");
            while (true){
                Thread.sleep(10);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void checkPort(){
        if(serialPort == null){
            System.out.println("无法找到端口");
        }else {

        }
    }

    public void write(String message){
        try{
            outputStream = new BufferedOutputStream(serialPort.getOutputStream());
        }catch (IOException e){
            throw new RuntimeException("获取端口的OutputStream出错：" + e.getMessage());
        }

        try {
            outputStream.write(message.getBytes());
            outputStream.flush();
            System.out.println("串口信息发送成功！");
        } catch (IOException e) {
            throw new RuntimeException("向端口发送信息时出错：" + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public void write(byte[]  message) {
        checkPort();

        try {
            outputStream = new BufferedOutputStream(serialPort.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("获取端口的OutputStream出错：" + e.getMessage());
        }

        try {
            outputStream.write(message);
            outputStream.flush();
            logger.info("串口信息发送成功！");
        } catch (IOException e) {
            throw new RuntimeException("向端口发送信息时出错：" + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static void closeSerialPort(SerialPort serialPort) {
        if(serialPort != null) {
            serialPort.notifyOnDataAvailable(false);
            //serialPort.notifyOnBreakInterrupt(false);
            serialPort.removeEventListener();
            if(inputStream != null){
                try {
                    inputStream.close();
                }catch (IOException e){

                }
            }
            if(outputStream != null){
                try{
                    outputStream.close();
                    serialPort.wait(30);
                }catch (Exception e){

                }
            }
            serialPort.close();
            serialPort = null;

            System.out.println("关闭了串口："+serialPort.getName());

        }
    }

    public static InputStream getInputStream() {
        return inputStream;
    }

    public static void setInputStream(InputStream inputStream) {
        SerialCommThread.inputStream = inputStream;
    }

    public static OutputStream getOutputStream() {
        return outputStream;
    }

    public static void setOutputStream(OutputStream outputStream) {
        SerialCommThread.outputStream = outputStream;
    }

    public static BlockingQueue<byte[]> getResponseQueue() {
        return responseQueue;
    }

    public static void setResponseQueue(BlockingQueue<byte[]> responseQueue) {
        SerialCommThread.responseQueue = responseQueue;
    }

    public static boolean isPortInUse() {
        return portInUse;
    }
}

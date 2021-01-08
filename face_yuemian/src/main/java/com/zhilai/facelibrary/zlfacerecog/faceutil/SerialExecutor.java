package com.zhilai.facelibrary.zlfacerecog.faceutil;

import android.text.TextUtils;

import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLErrorEnum;
import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import zhilai.serialport.SerialPort;

/**
 * contain error:
 * throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
 * throw new ZLException(ZLErrorEnum.SERIAL_SECURITY_ERROR, e);
 * throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
 * throw new ZLException(ZLErrorEnum.SERIAL_TIMEOUT_ERROR);
 * throw new ZLException(ZLErrorEnum.SERIAL_READ_TIMEOUT_ERROR);
 */
public class SerialExecutor {
    /**
     * 默认超时时间
     */
    private int timeOut = 2000;

    private byte[] buffer = new byte[2048];
    public boolean running;

    public String dev;
    public int baudrate;
    private long sendInterval = 500;

    public SerialPort openSerial() throws ZLException {
        if (TextUtils.isEmpty(dev) || baudrate <= 0) {
            return null;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(200);

            return new SerialPort(new File(dev), baudrate, 0);
        } catch (IOException e) {
            throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
        } catch (SecurityException e) {
            throw new ZLException(ZLErrorEnum.SERIAL_SECURITY_ERROR, e);
        } catch (InterruptedException e) {
            throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
        }
    }

    public void closeSerial(SerialPort serialPort) throws ZLException {
        if (serialPort != null) {
            try {
                if (serialPort.getOutputStream() != null) {
                    serialPort.getOutputStream().close();
                }
                if (serialPort.getInputStream() != null) {
                    serialPort.getInputStream().close();
                }
                serialPort.close();
                Thread.sleep(sendInterval);
            } catch (IOException e) {
                throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
            } catch (InterruptedException e) {
                throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
            }
        }
    }

    public synchronized byte[] sendData(byte[] data) throws ZLException {
        return sendData(data, timeOut);
    }

    /**
     * @param data
     * @param timeOutMills <= 0 返回值为null
     * @return
     * @throws ZLException
     */
    public synchronized byte[] sendData(byte[] data, int timeOutMills) throws ZLException {
        ZLog.i("sendData");
        SerialPort serialPort = openSerial();
        if (serialPort == null) {
            throw new NullPointerException("serialPort is null");
        }
        OutputStream os = serialPort.getOutputStream();
        InputStream is = serialPort.getInputStream();

        try {
            if (is.available() > 0) {
                int size = is.read(buffer);
                ZLog.i("run: 过滤掉脏数据:" + ByteUtil.bytesToHexString(Arrays.copyOf(buffer, size), " "));
            }

            ZLog.i("send packet (" + "data=" + ByteUtil.bytesToHexString(data, " ") + " dev=" + dev
                    + " baudrate=" + baudrate + ")");
            os.write(data);
            os.flush();

            if (timeOutMills <= 0) {
                return null;
            }

            int spendTimeMills = 0;
            while (spendTimeMills < timeOutMills) {

                if (is.available() > 0) {
                    TimeUnit.MILLISECONDS.sleep(300);
                    int size = is.read(buffer);
                    byte[] response = Arrays.copyOf(buffer, size);

                    ZLog.i("recv packet (" + "data=" + ByteUtil.bytesToHexString(response, " ") + ")");
                    return response;
                }

                spendTimeMills += 200;
                TimeUnit.MILLISECONDS.sleep(200);
            }
            if (spendTimeMills >= timeOutMills) {
                throw new ZLException(ZLErrorEnum.SERIAL_TIMEOUT_ERROR);
            }
        } catch (IOException e) {
            throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
        } catch (InterruptedException e) {
            throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
        } finally {
            closeSerial(serialPort);
        }
        return null;

    }

    public synchronized byte[] getData(int timeOutMills) throws ZLException {
        ZLog.i("getData");
        SerialPort serialPort = openSerial();
        if (serialPort == null) {
            throw new NullPointerException("serialPort is null");
        }
        InputStream is = serialPort.getInputStream();

        try {

            if (timeOutMills <= 0) {
                return null;
            }

//            if (is.available() > 0) {
//                int size = is.read(buffer);
//                ZLog.i("run: 过滤掉脏数据:" + ByteUtil.bytesToHexString(Arrays.copyOf(buffer, size), " "));
//            }

            int spendTimeMills = 0;
            while (spendTimeMills < timeOutMills && running) {

                if (is.available() > 0) {
                    TimeUnit.MILLISECONDS.sleep(300);
                    int size = is.read(buffer);
                    byte[] response = Arrays.copyOf(buffer, size);

                    ZLog.i("recv packet in getData (" + "data=" + ByteUtil.bytesToHexString(response, " ") + ")");
                    return response;
                }

                spendTimeMills += 200;
                TimeUnit.MILLISECONDS.sleep(200);
            }
            if (spendTimeMills >= timeOutMills) {
                throw new ZLException(ZLErrorEnum.SERIAL_READ_TIMEOUT_ERROR);
            }
        } catch (IOException e) {
            throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
        } catch (InterruptedException e) {
            throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
        } finally {
            running = false;
            closeSerial(serialPort);
        }
        return null;
    }

    public synchronized byte[] getData(byte[] data, int timeOutMills) throws ZLException {
        ZLog.i("getData");
        SerialPort serialPort = openSerial();
        if (serialPort == null) {
            throw new NullPointerException("serialPort is null");
        }
        InputStream is = serialPort.getInputStream();

        try {

            if (timeOutMills <= 0) {
                return null;
            }

            if (is.available() > 0) {
                int size = is.read(buffer);
                ZLog.i("run: 过滤掉脏数据:" + ByteUtil.bytesToHexString(Arrays.copyOf(buffer, size), " "));
            }

            serialPort.getOutputStream().write(data);
            ZLog.i("send data:" + ByteUtil.bytesToHexString(data, " "));

            int spendTimeMills = 0;
            while (spendTimeMills < timeOutMills && running) {

                if (is.available() > 0) {
                    TimeUnit.MILLISECONDS.sleep(300);
                    int size = is.read(buffer);
                    byte[] response = Arrays.copyOf(buffer, size);

                    ZLog.i("recv packet in getData (" + "data=" + ByteUtil.bytesToHexString(response, " ") + ")");
                    return response;
                }

                spendTimeMills += 200;
                TimeUnit.MILLISECONDS.sleep(200);
            }
            if (spendTimeMills >= timeOutMills) {
                throw new ZLException(ZLErrorEnum.SERIAL_READ_TIMEOUT_ERROR);
            }
        } catch (IOException e) {
            throw new ZLException(ZLErrorEnum.SERIAL_IO_ERROR, e);
        } catch (InterruptedException e) {
            throw new ZLException(ZLErrorEnum.WAIT_ERROR, e);
        } finally {
            running = false;
            closeSerial(serialPort);
        }
        return null;
    }

    public SerialExecutor() {

    }

    public SerialExecutor(String dev, int baudrate) {
        this.dev = dev;
        this.baudrate = baudrate;
    }

    public void clear() {
    }
}
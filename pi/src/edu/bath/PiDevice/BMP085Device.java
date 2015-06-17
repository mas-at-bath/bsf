package edu.bath.PiDevice;


// Lazy copy-paste of https://code.google.com/p/pilogger/source/browse/pilogger/src/probes/I2Cprobe.java
// VB even lazier, taken from https://github.com/noxo/BMP085Logger/blob/master/src/org/noxo/bmp085logger/util/BMP085Device.java


import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class BMP085Device {

	// Operating Mode (internal oversampling)
    public static final int OSS     = 3;
    
	// BMP085 Registers
    public static final int CAL_AC1           = 0xAA; //  # R   Calibration data (16 bits)
    public static final int CAL_AC2           = 0xAC; //  # R   Calibration data (16 bits)
    public static final int CAL_AC3           = 0xAE; //  # R   Calibration data (16 bits)
    public static final int CAL_AC4           = 0xB0; //  # R   Calibration data (16 bits)
    public static final int CAL_AC5           = 0xB2; //  # R   Calibration data (16 bits)
    public static final int CAL_AC6           = 0xB4; //  # R   Calibration data (16 bits)
    public static final int CAL_B1            = 0xB6; //  # R   Calibration data (16 bits)
    public static final int CAL_B2            = 0xB8; //  # R   Calibration data (16 bits)
    public static final int CAL_MB            = 0xBA; //  # R   Calibration data (16 bits)
    public static final int CAL_MC            = 0xBC; //  # R   Calibration data (16 bits)
    public static final int CAL_MD            = 0xBE; //  # R   Calibration data (16 bits)
    public static final int CONTROL           = 0xF4;
    public static final int DATA_REG          = 0xF6;
    public static final byte READTEMPCMD      = 0x2E;
    public static final int READPRESSURECMD   = 0xF4;
    
	public static final int BMP085_I2C_ADDR = 0x77;

	private int cal_AC1 = 0;
	private int cal_AC2 = 0;
	private int cal_AC3 = 0;
	private int cal_AC4 = 0;
	private int cal_AC5 = 0;
	private int cal_AC6 = 0;
	private int cal_B1 = 0;
	private int cal_B2 = 0;
	private int cal_MB = 0;
	private int cal_MC = 0;
	private int cal_MD = 0;

	private I2CDevice bmp085device = null;
	
	public BMP085Device(boolean v1) throws Exception
	{
		setup(v1);
	}

	private void setup(boolean v1) throws Exception {
		System.out.println("initializing bmp085");
		final I2CBus bus = v1 ? I2CFactory.getInstance(I2CBus.BUS_0) : I2CFactory.getInstance(I2CBus.BUS_1);
		System.out.println("finding bmp085 from i2c bus");
		bmp085device = bus.getDevice(BMP085_I2C_ADDR);
		System.out.println("reading bmp085 calibration data");
		readBMP085CalibrationData();
		System.out.println("calibration data read, bmp085 ok!");
	}

	private void readBMP085CalibrationData() throws IOException {
		cal_AC1 = readS16(CAL_AC1);
		cal_AC2 = readS16(CAL_AC2);
		cal_AC3 = readS16(CAL_AC3);
		cal_AC4 = readU16(CAL_AC4);
		cal_AC5 = readU16(CAL_AC5);
		cal_AC6 = readU16(CAL_AC6);
		cal_B1 = readS16(CAL_B1);
		cal_B2 = readS16(CAL_B2);
		cal_MB = readS16(CAL_MB);
		cal_MC = readS16(CAL_MC);
		cal_MD = readS16(CAL_MD);
	}
	
	private int readU8(int address) throws IOException{
        return bmp085device.read(address);
	}
	
	public double[] getReading() throws Exception
	{
		
		bmp085device.write(CONTROL, READTEMPCMD);
        Thread.sleep(50);
        int rawTemperature = readU16(DATA_REG);

        bmp085device.write(CONTROL, (byte) READPRESSURECMD);
        Thread.sleep(50);
        
        int msb = readU8(DATA_REG);
        int lsb = readU8(DATA_REG+1);
        int xlsb = readU8(DATA_REG+2);
        int rawPressure = ((msb << 16) + (lsb << 8) + xlsb) >> (8-OSS);

		return convertPressureTemp(rawPressure, rawTemperature);
	}
	
	private double[] convertPressureTemp(int rawPressure, int rawTemperature) 
	{
        double temperature = 0.0;
        double pressure = 0.0;
        double x1 = ((rawTemperature - cal_AC6) * cal_AC5) / 32768;
        double x2 = (cal_MC *2048) / (x1 + cal_MD);
        double b5 = x1 + x2;
        temperature = ((b5 + 8) / 16) / 10.0;

        double b6 = b5 - 4000;
        x1 = (cal_B2 * (b6 * b6 / 4096)) / 2048;
        x2 = cal_AC2 * b6 / 2048;
        double x3 = x1 + x2;
        double b3 = (((cal_AC1 * 4 + x3) * Math.pow(2, OSS) )+2) / 4;
        x1 = cal_AC3 * b6 / 8192;
        x2 = (cal_B1 * (b6 * b6 / 4096)) / 65536;
        x3 = ((x1 + x2) + 2) / 4;
        double b4 = cal_AC4 * (x3 + 32768) / 32768;
        double b7 = (rawPressure - b3) * (50000 / Math.pow(2, OSS));
        if (b7 < 0x80000000) pressure = (b7 * 2) / b4;
        else pressure = (b7 / b4) * 2;
        x1 = (pressure / 256) * (pressure / 256);
        x1 = (x1 * 3038) / 65536;
        x2 = (-7375 * pressure) / 65536;
        pressure = pressure + (x1 + x2 + 3791) / 16;

        return new double[] { pressure, temperature };
        
	}
	
	private int readU16(int address) throws IOException {
		int hibyte = bmp085device.read(address);
		return (hibyte << 8) + bmp085device.read(address + 1);
	}

	private int readS16(int address) throws IOException {
		int hibyte = bmp085device.read(address);
		if (hibyte > 127)
			hibyte -= 256;
		return (hibyte * 256) + bmp085device.read(address + 1);
	}
//
//	public static void main(String arg[]) {
//		try {
//			BMP085Device dev = new BMP085Device();
//			System.out.println("got device!");
//			double data[] = dev.getReading();
//			System.out.println("temperature=" + data[1] + " pressure=" + (data[0] * 0.01) + " hpa");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}


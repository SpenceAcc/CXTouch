package com.cxplan.projection.util;

import com.cxplan.projection.model.IDeviceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Created on 2018/4/16.
 *
 * @author kenny
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String TOUCH_INPUTER = "com.cxplan.projection.mediate/.inputer.CXTouchIME";
    //The package name of main application.
    public static final String PACKAGE_MAIN = "com.cxplan.projection.mediate";
    //The process name of main application run on device.
    public static final String PROCESS_NAME_MAIN = "com.cxplan.touch.mediate";
    //The package name of script application.
    public static final String PACKAGE_SCRIPT = "com.cxplan.projection.mediate.test";
    //The process name of script application run on device.
    public static final String PROCESS_NAME_SCRIPT = "com.cxplan.script.mediate";
    //The version code supported by current client.
    //Client will update main package if the version code of installed package doesn't match with this value.
    //The match rule is equals only.
    public static final int MAIN_SUPPORTED_VERSION = 3;
    public static final String VERSION_NAME = "1.3";
    //the version related with script application.
    public static final int SCRIPT_SUPPORTED_VERSION = 1;

    public static int resolveProcessID(String content, String processName) {
        if (StringUtil.isEmpty(content)) {
            return -1;
        }
        StringTokenizer st = new StringTokenizer(content);
        int index = 0;
        String processIdString = null;
        while(st.hasMoreTokens()) {
            index++;
            String token = st.nextToken();
            if (index == 2) {
                processIdString = token;
            } else if (index == 9) {
                if (!token.equals(processName)) {
                    return -1;
                } else {
                    break;
                }
            }

        }
        if (index < 9 || processIdString == null) {
            return -1;
        }
        try {
            return Integer.parseInt(processIdString);
        } catch (Exception e) {
            logger.error("Returned string is not pid: " + processIdString);
            return -1;
        }
    }

    /**
     * Return the name of current os used for naming resource directory.
     * This application is cross-platform, so the resources should
     * be built as different version for certain OS, the name of directory must match with current OS.
     *
     */
    public static String getOsDir() {
        if (SystemUtil.isWindow()) {
            return "win";
        } else if (SystemUtil.isLinux()) {
            return "linux";
        } else if (SystemUtil.isMac()) {
            return "macos";
        } else {
            throw new RuntimeException("Unknown OS");
        }
    }

    public static Point getDeviceDisplaySize(IDeviceMeta deviceMeta) {
        return getDeviceDisplaySize(deviceMeta, 1.0f);
    }
    public static Point getDeviceDisplaySize(IDeviceMeta deviceMeta, float zoomRate) {
        if (deviceMeta == null) {
            return null;
        }

        int rotation = deviceMeta.getRotation();
        if (rotation % 2 == 1) {
            return new Point((int) (deviceMeta.getScreenHeight() * zoomRate), (int) (deviceMeta.getScreenWidth() * zoomRate));
        } else {
            return new Point((int)(deviceMeta.getScreenWidth() * zoomRate),
                    (int)( deviceMeta.getScreenHeight() * zoomRate));
        }
    }

    public static Properties loadPropertyFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        FileInputStream inputStream = null;
        try {
            Properties properties = new Properties();
            inputStream = new FileInputStream(file);
            properties.load(inputStream);

            return properties;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static byte[] int2LowEndianBytes(int value) {
        byte[] ret = new byte[4];
        ret[0] =(byte) (value & 0xFF);
        ret[1] =(byte) ((value >> 8) & 0xFF);
        ret[2] =(byte) ((value >> 16) & 0xFF);
        ret[3] =(byte) ((value >> 24) & 0xFF);

        return ret;
    }

    public static void writeIntLowEndian(int value, OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new RuntimeException("The output stream object is empty!");
        }

        byte[] data = int2LowEndianBytes(value);

        outputStream.write(data);

    }

    public static int readIntLowEndian(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public static int readIntUpEndian(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }


    /**
     * Write byte array to specified file.
     *
     * @param data byte array.
     * @param file local file.
     * @throws IOException
     */
    public static void writeByteArray2File(byte[] data, File file) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, false);
            out.write(data);
            out.close(); // don't swallow close Exception if copy completes normally
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e){}
            }
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }
}

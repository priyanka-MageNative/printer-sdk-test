package com.quin.sdkdemo.util;

import com.quin.sdkdemo.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    private static final String ROOT = App.getApp().getExternalFilesDir(null).getAbsolutePath();
    public static final String FIRMWARE = "Firmware";

    public static boolean writeFirmWareToLocal(FileOutputStream out, InputStream in) {
        int length = -1;
        byte[] buff = new byte[1024];
        try {
            while ((length = in.read(buff)) != -1) {
                out.write(buff, 0, length);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return true;
    }

    public static String getFirmwarePath(String printerName) {
        return pathCombin(
                ROOT,
                FIRMWARE,
                printerName + ".zip"
        );
    }


    public static File getFirmwareFile(String printerName) {
        String path = pathCombin(
                ROOT,
                FIRMWARE,
                printerName + ".zip"
        );
        File file = new File(path);
        mkDirsIfNotExist(file);
        return file;
    }

    public static void clearFirmWareDir() {
        deleteDir(pathCombin(
                ROOT,
                FIRMWARE
        ));
    }

    public static String pathCombin(String... dirs) {
        StringBuilder sb = new StringBuilder();
        sb.append(dirs[0]);
        for (int i = 1; i < dirs.length; i++) {
            sb.append(File.separator);
            sb.append(dirs[i]);
        }
        return sb.toString();
    }

    public static void mkDirsIfNotExist(File file) {
        if (file != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }
}

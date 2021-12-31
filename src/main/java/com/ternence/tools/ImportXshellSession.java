package com.ternence.tools;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author ternence
 */
@SuppressWarnings("FieldCanBeLocal")
public class ImportXshellSession {
    private static String XshellSessionFolderPath = "";
        private static String UserSessionsFileOutPutPath;
    private static JSONArray ja = new JSONArray();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("参数异常。");
        }
        XshellSessionFolderPath = args[0];
        UserSessionsFileOutPutPath = args[1];
        File file = new File(XshellSessionFolderPath);
        FileWriter fw = null;
        try {
            recursiveSessionFile(file);
            fw = new FileWriter(UserSessionsFileOutPutPath);
            fw.write(JSON.toJSONString(ja,SerializerFeature.PrettyFormat));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    private static void recursiveSessionFile(File file) throws IOException {
        if (file.isDirectory()) {
            String groupName = file.getName();
            File[] files = file.listFiles();
            if (files != null) {
                for (File item : files) {
                    if (item.isDirectory()) {
                        recursiveSessionFile(item);
                    } else {
                        JSONObject jo = new JSONObject();
                        jo.put("modem.alwaysUseDownloadFolder", true);
                        jo.put("session.autoLogin", "");
                        jo.put("session.group", groupName);
                        jo.put("session.icon", "");
                        jo.put("session.label", item.getName().replace(".xsh", ""));
                        jo.put("session.logOption", 1);
                        jo.put("session.logType", 0);
                        jo.put("session.port", 22);
                        jo.put("session.protocol", "SSH");
                        jo.put("session.proxy", "None");
                        try (FileInputStream fis = new FileInputStream(item); InputStreamReader isr =
                                new InputStreamReader(fis, StandardCharsets.UTF_8); BufferedReader br =
                                new BufferedReader(isr)) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                String r = line.replace(" ", "").replace("\u0000", "");
                                if (r.startsWith("Host=")) {
                                    String[] s = r.split("=");
                                    if (s.length == 2) {
                                        jo.put("session.target", s[1]);
                                    } else {
                                        jo.put("session.target", "");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        jo.put("session.term", "xterm-256color");
                        jo.put("session.uuid", UUID.randomUUID());
                        jo.put("window.columns", 12);
                        jo.put("window.mouseWheelScrollLines", 1);
                        jo.put("xmodem.packetSize", 128);
                        jo.put("ymodem.packetSize", 1024);
                        ja.add(jo);
                    }
                }
            }
        }
    }
}

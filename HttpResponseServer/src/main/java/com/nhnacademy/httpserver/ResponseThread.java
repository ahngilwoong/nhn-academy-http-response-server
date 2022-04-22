package com.nhnacademy.httpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.responsedata.ClassPacket;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseThread implements Runnable {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    List<String> packetSave = new ArrayList<>();

    ResponseThread(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            boolean isChecked = false;
            String jsonStr = "";
            byte[] byteArr = new byte[4096];
            int readByteCount = in.read(byteArr);
            String message = new String(byteArr, 0, readByteCount, "UTF-8");
            System.out.println(message);

            String[] temp = message.split("\n");
            for (int i = 0; i < temp.length; i++) {
                if (temp[i].equals("\r")) {
                    isChecked = true; //requestHeader body 구분
                }
                if (!isChecked) {
                    packetSave.add(temp[i]);
                } else {
                    jsonStr += temp[i]; //requestBody
                }
            }
            System.out.println(jsonStr);
            System.out.println("여기까진 OK!");
            System.out.println(socket.getLocalAddress());
            System.out.println(
                "json Str = " + jsonStr.trim()); // str 앞에 \r이 붙어있어서 씹힌다. 그러므로 trim으로 없애 넘겨줌.

            //-----------------Requset 패킷 받아온 값들 .

            ClassPacket request = new ClassPacket(packetSave, jsonStr.trim());

            PrintStream printStream = new PrintStream(socket.getOutputStream());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            OffsetDateTime currentTime = OffsetDateTime.now();


            if (request.getUrlPath().contains("/ip")) {
                String json;
                map.put("origin", socket.getInetAddress().toString().replace("/", ""));
                json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
                System.out.println(json);   // pretty-print
                printResponseHeader(printStream, currentTime, json);
                printStream.println(json);
                printStream.flush();

            } else if (request.getUrlPath().contains("/get")) {
                map.put("args", createArgsMap(request.getUrlPathArgs()));
                map.put("hearders", headerMapSetting(request));
                map.put("origin", socket.getInetAddress().toString().replace("/", ""));
                map.put("url", socket.getLocalAddress().toString().replace("/", "") + request.getUrlPath());
                String responseJsonBody =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

                printResponseHeader(printStream, currentTime, responseJsonBody);
                printStream.println(responseJsonBody);
            } else if (request.getUrlPath().contains("/post")) {
                map.put("args", createArgsMap(request.getUrlPathArgs()));
                map.put("hearders", headerMapSetting(request));
                map.put("origin", socket.getInetAddress().toString().replace("/", ""));
                map.put("url", socket.getLocalAddress().toString().replace("/", "") + request.getUrlPath());


            } else {
                // output으로 4xx에러 발생 시키기.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResponseHeader(PrintStream printStream, OffsetDateTime currentTime,
                                     String json) {
        printStream.println("HTTP/1.1 200 OK");
        printStream.println("Date: " +
            currentTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)); //TODO 타임 포맷팅 UTF8?
        printStream.println("Content-Type: application/json");
        printStream.println("Content-Length: " + json.length());
        printStream.println("Connection: keep-alive");
        printStream.println("Server: gunicorn/19.9.0");
        printStream.println("Access-Control-Allow-Origin: *");
        printStream.println("Access-Control-Allow-Credentials: true");
        printStream.println();
    }

    public void ipController(ClassPacket request) {

    }

    public void getController(ClassPacket request) {

    }

    public void postController(ClassPacket request) {

    }

    public void responseHeader(ClassPacket request) {

    }

    public Map<String, String> headerMapSetting(ClassPacket request){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Host", request.getRequestHeader("Host"));
        headerMap.put("User-Agent", request.getRequestHeader("User-Agent"));
        headerMap.put("Accept", request.getRequestHeader("Accept"));
        headerMap.put("Host", request.getRequestHeader("Host"));
        return headerMap;
    }
    public Object createArgsMap(String input) { // TODO 시간이 남는다면 형식에 맞게 안들어올때 Exception처리
        Map<String, String> returnMap = new HashMap<>();
        if(input.equals("")){
            return returnMap;
        }
        if(input.contains("&")){
            String[] splitAndStr = input.split("&");
            for (int i = 0; i < splitAndStr.length; i++) {
                String[] splitEqualStr = splitAndStr[i].split("=");
                returnMap.put(splitEqualStr[0], splitEqualStr[1]);
            }
        }else{
            String[] args = input.split("=");
            returnMap.put(args[0], args[1]);
        }
        return returnMap;
    }


}

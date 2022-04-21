package com.nhnacademy.httpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.responsedata.ClassPacket;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.simple.JSONObject;

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
                if(temp[i].equals("\r")) {
                    isChecked = true;
                }
                if(!isChecked){
                    packetSave.add(temp[i]);
                }else{
                    jsonStr+=temp[i];
                }
            }
            System.out.println(jsonStr);
            System.out.println("여기까진 OK!");

            System.out.println("json Str = "+ jsonStr.trim()); // str 앞에 \r이 붙어있어서 씹힌다. 그러므로 trim으로 없애 넘겨줌.
            ClassPacket request = new ClassPacket(packetSave,jsonStr.trim());
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = new HashMap<>();

            if(request.getUrlPath().contains("/ip")){
                SimpleDateFormat formatter = new SimpleDateFormat ( "EEE, d MMM yyyy HH:mm:ss Z", Locale.KOREA );
                Date currentTime = new Date();
                String dTime = formatter.format ( currentTime );
                map.put("origin", socket.getInetAddress().toString().replace("/",""));
                String json;
                json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
                System.out.println(json);   // pretty-print
                printStream.println("HTTP/1.1 200 OK");
                printStream.println("Date: " + dTime);
                printStream.println("Content-Type: application/json");
                printStream.println("Content-Length: "+ json.length());
                printStream.println();
                printStream.println(json);
                printStream.flush();

            }else if(request.getUrlPath().contains("/get")){

            }else if(request.getUrlPath().contains("/post")){

            }else{
                // output으로 4xx에러 발생 시키기.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void ipController(ClassPacket request){

    }
    public void getController(ClassPacket request){

    }
    public void postController(ClassPacket request){

    }
    public void responseHeader(ClassPacket request){

    }


}

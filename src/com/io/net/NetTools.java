package com.io.net;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetTools {


    // 判断网络状态
    public static String testNet(String ip) throws Exception {
        BufferedReader br = null;
        try{
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("ping -c 3 " + ip);
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), "utf-8");
            br = new BufferedReader(inputStreamReader);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            if(!sb.toString().contains("avg")){
                return null;
            }
            else{
                return sb.toString().split("/")[4];
            }
        }catch (Exception e){
            throw new Exception();
        }finally {
            if (br != null){
                br.close();
            }
        }
    }


    /**
     * 从文件中获取
     * 使用 curl https://proxypoolss.tk/ss/sub > server.json  觉得更容易
     * @param filePath
     * @return
     */
    public static JsonArray getServersFromFile(String filePath){
        Path path = Paths.get(filePath);
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }
            JsonElement jsonElement = new JsonParser().parse(stringBuffer.toString());
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            return jsonArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网络中获取
     * @param serversUrl
     * @return
     */
    public static JsonArray getServersFromNet(String serversUrl){
        try {
            URL url = new URL(serversUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Content-type","application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonElement jsonElement = new JsonParser().parse(in);
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            return jsonArray;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
       JsonArray jsonArray =  NetTools.getServersFromNet("https://proxypoolss.tk/ss/sub");
        System.out.println();

    }
}

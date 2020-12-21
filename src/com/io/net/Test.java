package com.io.net;//主类


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Test {


    public static void main(String[] args) throws Exception {
        JsonArray jsonArray = NetTools.getServersFromNet("https://proxypoolss.tk/ss/sub");
//        JsonArray jsonArray = NetTools.getServersFromFile("/Users/zhangwei/zwData/intellij/javatest/src/main/java/com/io/net/server.json");
        List<JsonObject> list = new ArrayList<JsonObject>();
         long time1 = System.currentTimeMillis();
//        for (JsonElement jsonElement : jsonArray){
//            list.add(jsonElement.getAsJsonObject());
//        }
        for (int i=0;i<jsonArray.size();i++){
            JsonElement jsonElement = jsonArray.get(i);
            list.add(jsonElement.getAsJsonObject());
        }
        ForkJoinPing forkJoinPing = new ForkJoinPing(list);
        List<JsonObject> result = forkJoinPing.handleServerItem();


        result = result.stream().filter(jsonObject -> { return jsonObject.get("delay").getAsDouble() < 9999; }).collect(Collectors.toList());
        double speedAvg = result.stream().mapToDouble(jsonObjec->{return jsonObjec.get("speed").getAsDouble();}).average().getAsDouble();
        double delayAvg = result.stream().mapToDouble(jsonObjec->{return jsonObjec.get("delay").getAsDouble();}).average().getAsDouble();
        System.out.println("speed avg："+speedAvg);
        System.out.println("delay avg："+delayAvg);
        long time2 = System.currentTimeMillis();
        System.out.println("ping 总耗时："+(time2-time1)+"ms");
        //处理结果写入文件
        Path path = Paths.get("MaxAndMin.txt");
        if(!Files.exists(path)){
            Files.createFile(path);
        }
        //清空
        BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
        bufferedWriter.write("");
        bufferedWriter.close();

        //排序找到最大的网速
        Files.write(path,"------------max speed----------------\n".getBytes(),StandardOpenOption.APPEND);
        result.stream().sorted(Comparator.comparing(jsonObject -> {
            return -jsonObject.get("speed").getAsFloat();
        })).limit(20).collect(Collectors.toList()).forEach(jsonObject -> {
            try {
                Files.write(path,formatLine(jsonObject).getBytes(),StandardOpenOption.APPEND);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //找到最小的延迟
        Files.write(path,"------------min delay----------------\n".getBytes(),StandardOpenOption.APPEND);
        result.stream().sorted(Comparator.comparing(jsonObject -> {
            return jsonObject.get("delay").getAsFloat();
        })).limit(20).collect(Collectors.toList()).forEach(jsonObject -> {
            try {
                Files.write(path,formatLine(jsonObject).getBytes(),StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.write(path,"------------速度大于平均值延迟小于平均值----------------\n".getBytes(),StandardOpenOption.APPEND);
        result.stream().filter(jsonObject -> {
           return jsonObject.get("speed").getAsDouble()>speedAvg && jsonObject.get("delay").getAsDouble()>delayAvg;
        }).limit(30).forEach(jsonObject -> {
                    try {
                         String printline = formatLine(jsonObject).replace("\n","")+"\tlink:"+getLink(jsonObject)+"\n";
                        Files.write(path,printline.getBytes(),StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        ///Users/zhangwei/Library/Application\ Support/ShadowsocksX-NG/ss-local-config.json  修改此文件
        //shell 命令 通过替换MaxAndMin.txt 里的参数即可


    }


    /**
     * 格式化
     * @param jsonObject
     * @return
     */
    public static String formatLine(JsonObject jsonObject){
        String line = String.format("server:%s\tspeed:%s\tdelay:%s\tpassword:%s\tport:%s\tmethod:%s \n",
                jsonObject.get("server").getAsString(),
                jsonObject.get("speed").getAsString(),
                jsonObject.get("delay").getAsString(),
                jsonObject.get("password").getAsString(),
                jsonObject.get("server_port").getAsString(),
                jsonObject.get("method").getAsString()
        );
        return line;
    }

//    ss://method:password@server:port
    private static String getLink(JsonObject jsonObject){
        String link=jsonObject.get("method").getAsString()+":"
                +jsonObject.get("password").getAsString()+"@"+jsonObject.get("server").getAsString()+":"+jsonObject.get("server_port").getAsString();
        return "ss://"+ Base64.getEncoder().encodeToString(link.getBytes());
    }
}

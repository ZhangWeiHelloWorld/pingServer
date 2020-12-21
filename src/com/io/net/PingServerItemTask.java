package com.io.net;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class PingServerItemTask implements Callable<JsonObject> {
    private Logger log = LoggerFactory.getLogger(PingServerItemTask.class);
    private String ip;
    private JsonObject jsonObject;
    PingServerItemTask(String ip){
        this.ip = ip;
    }
    PingServerItemTask(JsonObject jsonObject){
        this.jsonObject = jsonObject;
    }
    @Override
    public JsonObject call() throws Exception {
        String serverIp = jsonObject.get("server").getAsString();
        String delay = NetTools.testNet(serverIp);
        if(delay == null){
            delay = "99999999";
        }
        this.jsonObject.addProperty("delay",delay);
        String speed = this.jsonObject.get("remarks").getAsString().split("\\|")[1];
        speed = speed.substring(0,speed.length()-2);
//        log.info("speed:{} delay:{}",speed,delay);
        this.jsonObject.addProperty("speed",speed);
        return this.jsonObject;
    }
}

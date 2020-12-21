package com.io.net;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ForkJoinPing implements ServerHandler {

    private static final Logger log = LoggerFactory.getLogger(ForkJoinPing.class);
    ForkJoinPool forkJoinPool;
    ExecutorService pool = Executors.newFixedThreadPool(100);
    List<JsonObject>  jsonObjectList;

    private  volatile  Boolean  used[];
    private AtomicBoolean start = new AtomicBoolean(true);

    private long startTime ;

    ForkJoinPing(List<JsonObject> jsonObjects){
        forkJoinPool = new ForkJoinPool();
        this.jsonObjectList = jsonObjects;
    }
    @Override
    public List<JsonObject> handleServerItem() {
        startTime = System.currentTimeMillis();
        used = new Boolean[this.jsonObjectList.size()];
        Arrays.fill(used,false);
        ForkJoinTask forkJoinTask = new ForkJoinTask(this.jsonObjectList,0,this.jsonObjectList.size(),used);
        count();
        List<JsonObject> result = forkJoinPool.invoke(forkJoinTask);
        forkJoinPool.shutdown();
        pool.shutdownNow();
        start.getAndSet(false);
        return result;
    }

    public  void count(){
        new Thread(()->{
            while (start.get()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long count = Arrays.stream(used).filter(use->use==true).count();
                String line  = String.format("---完成%2.2f 耗时：%dms",+count*1.0/this.jsonObjectList.size()*100,(System.currentTimeMillis()-startTime));
//                log.info("------完成"+count*1.0/this.jsonObjectList.size()*100+"%------");
                log.info(line);
            }

        }).start();
    }
    private class ForkJoinTask extends RecursiveTask<List<JsonObject>> {

        private List<JsonObject> list;
        private List<JsonObject> result = new ArrayList<JsonObject>();

        private int from;
        private int to;
        private Boolean  used[];
        private int allCount;


        public ForkJoinTask(List<JsonObject> list, int from, int to,Boolean used[]) {
            this.list = list;
            this.from = from;
            this.to = to;
            this.allCount = list.size();
            this.used = used;
        }


        @Override
        protected List<JsonObject> compute() {
            if(this.to-this.from <50){
                List<Future<JsonObject>> futures = new ArrayList<>();
                for(int i=this.from;i<this.to;i++){
                    System.out.println("提交第"+i+"个任务");
                    Future<JsonObject> future =  pool.submit(new PingServerItemTask(list.get(i)));
                    futures.add(future);

                }

                //获取 future
                Stream.iterate(0,i->i+1).limit(futures.size()).forEach(index->{
                    try {
                        used[index+this.from] = true;
                        result.add(futures.get(index).get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });


                return result;
            }else{
                int mid = (from+to)/2;
                ForkJoinPing.ForkJoinTask forkJoinTaskLeft  = new ForkJoinPing.ForkJoinTask(list,from,mid,used);
                ForkJoinPing.ForkJoinTask forkJoinTaskRight  = new ForkJoinPing.ForkJoinTask(list,mid,to,used);
                forkJoinTaskLeft.fork();
                forkJoinTaskRight.fork();
                List<JsonObject> left = forkJoinTaskLeft.join();
                List<JsonObject> right = forkJoinTaskRight.join();
                left.addAll(right);
                return left;
            }
        }


    }
}



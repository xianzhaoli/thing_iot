package com.risky.server.MQTT.common.store.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.Queues;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
public class BatchQueue<T> implements Runnable{

    private BlockingQueue queue;

    private BatchFunction<T> batchFunction;

    private final int size;

    private final int timeout;

    private final TimeUnit timeUnit;

    //副本队列
    private BlockingQueue copyBlockingQueue = new LinkedBlockingDeque();

    public BatchQueue(BlockingQueue queue, BatchFunction<T> batchFunction, int size, int timeout, TimeUnit timeUnit) {
        this.queue = queue;
        this.batchFunction = batchFunction;
        this.size = size;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public List<T> getMemoryData(){
        List<T> list = new ArrayList<>();
        copyBlockingQueue.drainTo(list,Integer.MAX_VALUE);
        return list;
    }


    public void addToQueue(T element){
        queue.offer(element);
        copyBlockingQueue.offer(element);
    }

    @Override
    public void run() {
        try {
            while (true){
                //log.info("batchSaveDataQueue running ------ thread->{},time ->{}",Thread.currentThread().getName(),new Date());
                List<T> list = new CopyOnWriteArrayList();
                Queues.drain(queue,list,size,timeout, timeUnit);
                if(!CollectionUtils.isEmpty(list)){
                    log.info("入库数据:{}条,入库时间{}",list.size(),new Date());
                    batchFunction.batchSave(list);
                    copyBlockingQueue.clear();
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

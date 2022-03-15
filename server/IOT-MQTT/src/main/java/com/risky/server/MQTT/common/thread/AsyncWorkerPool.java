package com.risky.server.MQTT.common.thread;

import com.risky.server.MQTT.common.store.SessionStoreMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步工作线程池
 */
@Component
public class AsyncWorkerPool {


    ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1000,60,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));

    @Autowired
    private MongoTemplate mongoTemplate;

    public void storeCleanSessionMessage(SessionStoreMessage sessionStoreMessage){
        //异步写入mongodb
        executor.execute(new Woker(() -> mongoTemplate.insert(sessionStoreMessage)));
    }



}

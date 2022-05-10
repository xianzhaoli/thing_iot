package com.risky.server.MQTT.config;

import com.risky.server.MQTT.common.store.SessionStoreMessage;
import com.risky.server.MQTT.common.store.batch.BatchQueue;
import com.risky.server.MQTT.common.store.batch.BatchStoreWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class StoreConfig {

    ThreadPoolExecutor executor = new ThreadPoolExecutor(1,3,60,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));

    @Autowired
    private MongoTemplate mongoTemplate;

    @Bean
    public BatchQueue batchQueue(){
        BatchQueue batchQueue = new BatchQueue(new LinkedBlockingDeque(), collection -> mongoTemplate.insert(collection,SessionStoreMessage.class),
                3000,20, TimeUnit.SECONDS);
        //executor.execute(batchQueue);
        return batchQueue;
    }
}

package com.risky.server.MQTT.common.store.batch;

import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

public class BatchStoreWorker<T> implements Runnable {

    private List<T> collections;

    private Class entityClass;

    private MongoTemplate mongoTemplate;

    @Override
    public void run() {
        //入库 mongo
        mongoTemplate.insert(collections,entityClass);
    }


    public BatchStoreWorker(List<T> collections, Class entityClass, MongoTemplate mongoTemplate) {
        this.collections = collections;
        this.entityClass = entityClass;
        this.mongoTemplate = mongoTemplate;
    }
}

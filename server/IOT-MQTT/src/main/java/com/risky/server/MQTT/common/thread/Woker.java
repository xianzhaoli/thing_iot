package com.risky.server.MQTT.common.thread;

public class Woker implements Runnable {

    private Job job;

    @Override
    public void run() {
        job.work();
    }

    public Woker(Job job) {
        this.job = job;
    }

}

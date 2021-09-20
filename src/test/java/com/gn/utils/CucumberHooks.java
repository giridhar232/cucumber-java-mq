package com.gn.utils;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;

public class CucumberHooks implements ConcurrentEventListener {
    public static KafkaConsumer<String, String> consumer;
    public static KafkaProducer<String, String> producer;

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, beforeAll);
        eventPublisher.registerHandlerFor(TestRunFinished.class, afterAll);
    }

    private EventHandler<TestRunStarted> beforeAll = event -> {
        setupKafka();
    };

    private EventHandler<TestRunFinished> afterAll = event -> {
        teardownKafka();
    };

    public void setupKafka() {
        System.out.println("Starting ZK & Kafka Server");
        try {
            Common.addDelayBeforeScenario(2);
            String cmd = "sh -c " + Constants.KAFKA_START_SCRIPT_PATH;
            Process startServerProcess = Runtime.getRuntime().exec(cmd);
            StreamConsumer streamConsumer = new StreamConsumer(startServerProcess.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamConsumer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("ZK & Kafka Server STARTED");
        createKafkaComponents();
    }

    public void teardownKafka() {
        producer.flush();
        producer.close();
        consumer.commitAsync();
        System.out.println("Stopping ZK & Kafka Server");
        try {
            Common.addDelayBeforeScenario(2);
            String cmd = "sh -c " + Constants.KAFKA_STOP_SCRIPT_PATH;
            Process stopServerProcess = Runtime.getRuntime().exec(cmd);
            StreamConsumer streamConsumer = new StreamConsumer(stopServerProcess.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamConsumer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("ZK & Kafka Server STOPPED");
    }

    private void createKafkaComponents() {
        //Producer
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", Constants.BOOTSTRAP_SERVER);
        props.setProperty("acks", "1");
        props.setProperty("retries", "2");
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        this.producer = producer;
        System.out.println("Kafka Producer CREATED");

        //Consumer
        Properties props1 = new Properties();
        props1.setProperty("bootstrap.servers", Constants.BOOTSTRAP_SERVER);
        props1.setProperty("group.id", "GN");
        props1.setProperty("key.deserializer", StringDeserializer.class.getName());
        props1.setProperty("value.deserializer", StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props1);
        this.consumer = consumer;
        System.out.println("Kafka Consumer CREATED");
    }
}

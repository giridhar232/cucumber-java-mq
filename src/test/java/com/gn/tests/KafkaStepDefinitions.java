package com.gn.tests;

import com.gn.utils.CucumberHooks;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;

import java.time.Instant;
import java.util.Collections;

public class KafkaStepDefinitions {
    String bootstrap_servers;
    String zookeeper_servers;
    public static KafkaConsumer<String, String> consumer;
    public static KafkaProducer<String, String> producer;

    @Given("^kafka is running on \"([^\"]*)\" and zookeeper is running on \"([^\"]*)\"$")
    public void kafka_is_running_on_and_zookeeper_is_running_on(String bootstrapServers, String zkServers) {
        this.bootstrap_servers = bootstrapServers;
        this.zookeeper_servers = zkServers;
        System.out.println("Zookeeper is running at: " + zookeeper_servers);
        System.out.println("Kafka server is running at: " + bootstrap_servers);
        this.consumer = CucumberHooks.consumer;
        this.producer = CucumberHooks.producer;
    }

    @When("^kafka producer publishes \"([^\"]*)\" event to \"([^\"]*)\" topic$")
    public void kafka_producer_publishes_event_to_topic(String message, String topic) {
        try {
            KafkaProducer<String, String> producer = this.producer;

            Instant instant = Instant.now();
            long timeStampMillis = instant.toEpochMilli();
            System.out.println("Timestamp: " + timeStampMillis);

            ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(
                    topic, Long.toString(timeStampMillis), message);

            //producer.send(producerRecord);
            producer.send(producerRecord, (metadata, exception) -> {
                if(exception == null ) {
                    System.out.println("message posted...");
                } else {
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Then("^kafka consumer receives \"([^\"]*)\" event on \"([^\"]*)\" topic$")
    public void kafka_consumer_receives_event_on_topic(String message, String topic) {
        KafkaConsumer<String, String> consumer = this.consumer;
        consumer.subscribe(Collections.singletonList(topic));

        boolean breakWhile = false;
        int loopCount = 0;
        while (!breakWhile) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            loopCount++;
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("ConsumerRecord Details:: offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                Assert.assertEquals(message, record.value());
                if(records.count() > 0 || loopCount > 2) {
                    breakWhile = true;
                }
            }
        }
        consumer.commitAsync();
    }
}

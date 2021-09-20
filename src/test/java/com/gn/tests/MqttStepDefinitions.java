package com.gn.tests;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.zip.DataFormatException;

import com.gn.protobuf.AddressBook;
import com.gn.protobuf.Person;
import com.gn.utils.Common;
import com.gn.utils.CompressionUtils;
import com.gn.utils.Constants;
import com.gn.utils.StreamConsumer;
import com.google.protobuf.InvalidProtocolBufferException;

import com.google.protobuf.util.JsonFormat;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.java.en.And;
import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.*;

import org.junit.Assert;

public class MqttStepDefinitions {
    String mqttServer, messageToCompare, topicToCompare, jsonString, personName, testMsgType;
    String mqttClientName = Constants.MOSQUITTO_CLIENT_NAME;
    boolean isMessageReceived = false;
    byte[] addressBookByteArray;

    Process process;
    AddressBook addressBook = null;
    MqttClient client;

    @Given("^mqtt is running on \"([^\"]*)\"$")
    public void setup_mqtt_client(String mqtt_server_str) {
        try {
            Common.addDelayBeforeScenario(2);
            String cmd = "sh -c " + Constants.MOSQUITTO_SERVER_PATH + " -c " + Constants.MOSQUITTO_CONFIG_PATH;
            this.process = Runtime.getRuntime().exec(cmd);
            StreamConsumer streamConsumer = new StreamConsumer(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamConsumer);

            System.out.println("Mosquitto server started on localhost..");
            this.mqttServer = Constants.MQTT_PROTOCOL + mqtt_server_str;
            this.client = new MqttClient(mqttServer, mqttClientName);
        } catch (MqttException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @When("^mqtt producer publishes \"([^\"]*)\" \"([^\"]*)\" to \"([^\"]*)\" topic$")
    public void mqttProduce(String message, String eventType, String topic) {
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(180);

            if(!client.isConnected()){
                client.connect();
            }
            System.out.println("conn made:: " + client.isConnected());

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) { }

                @Override
                public void connectionLost(Throwable cause) {}

                @Override
                public void messageArrived(String maTopic, MqttMessage maMessage) throws Exception {
                    System.out.println("MQTT message received on topic: " + maTopic);
                    switch (eventType) {
                        case "string event": {
                            messageToCompare = maMessage.toString();
                            System.out.println("MQTT message received: " + messageToCompare);
                            break;
                        }
                        case "protobuf event": {
                            messageToCompare =  decompressDeserializeMQTTMessage(maMessage);
                            System.out.println("MQTT message received: " + messageToCompare);
                            break;
                        }
                    }
                    isMessageReceived = true;
                    topicToCompare = maTopic;
                    client.disconnect();
                    process.destroy(); //kill mosquitto server
                    System.out.println("Mosquitto server destroyed on localhost");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.subscribe(topic);
            System.out.println("MQTT subscribed to topic: " + topic);
            MqttMessage mqtt_message = new MqttMessage();
            switch (eventType) {
                case Constants.STRING_EVENT: {
                    mqtt_message.setPayload(message.getBytes());
                    break;
                }
                case Constants.PROTOBUF_EVENT: {
                    mqtt_message.setPayload(addressBookByteArray);
                    break;
                }
            }

            System.out.println("conn status before publish:: " + client.isConnected());
            client.publish(topic, mqtt_message);
            System.out.println("MQTT message sent: " + message);
            System.out.println("MQTT message sent on topic: " + topic);
        } catch (MqttException e) {
            System.out.println(e.getMessage());
        }

        int waitCounter = 0;
        while(true) {
            try {
                Thread.sleep(Constants.MQTT_WAIT_FOR_RESPONSE_MS);
                waitCounter++;
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if(isMessageReceived || waitCounter > 2)
                break;
        }
    }

    @Then("^mqtt consumer receives \"([^\"]*)\" \"([^\"]*)\" on \"([^\"]*)\" topic$")
    public void mqttConsume(String message, String eventType, String topic) {
        switch (eventType) {
            case "string event": {
                Assert.assertEquals(message, messageToCompare);
                Assert.assertEquals(topic, topicToCompare);
                break;
            }
            case "protobuf event": {
                Assert.assertEquals(personName, messageToCompare);
                Assert.assertEquals(topic, topicToCompare);
                break;
            }
        }
    }

    @And("^\"([^\"]*)\" message is used$")
    public void sampleAggregatedMessageIsSet(String msgType) {
        this.testMsgType = msgType;
        switch (testMsgType) {
            case Constants.TEST_MSG_SAMPLE: {
                try {
                    this.jsonString = readFile("./addressbook_sample.txt");
                    AddressBook.Builder addressBookBuilder = AddressBook.newBuilder();
                    JsonFormat.parser().ignoringUnknownFields().merge(jsonString, addressBookBuilder);
                    this.addressBook = addressBookBuilder.build();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            case Constants.TEST_MSG_BUILD: {
                this.addressBook = buildAddressBookObject();
            }
        }
    }

    public String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    @And("serialize and compress the sample message")
    public void serializeAndCompressTheSampleMessage() {
        try {
            this.personName = addressBook.getPeople(0).getName();
            this.addressBookByteArray = CompressionUtils.compress(addressBook.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String decompressDeserializeMQTTMessage(MqttMessage maMessage) {
        String personName = null;
        try {
            byte[] decompressedMsg = CompressionUtils.decompress(maMessage.getPayload());
            AddressBook deSerializeAddressBook = AddressBook.parseFrom(decompressedMsg);
            personName = deSerializeAddressBook.getPeople(0).getName();
        } catch (InvalidProtocolBufferException e) {
            System.out.println("msg=Exception while protobuf deserializing:: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        return personName;
    }

    public AddressBook buildAddressBookObject() {
        Person.PhoneNumber.Builder phoneNumber1 = Person.PhoneNumber.newBuilder();
        Person.PhoneNumber.Builder phoneNumber2 = Person.PhoneNumber.newBuilder();
        Person.Builder person = Person.newBuilder();
        AddressBook.Builder addressBook = AddressBook.newBuilder();

        phoneNumber1.setNumber("123-123-1234");
        phoneNumber1.setType(Person.PhoneType.HOME);
        phoneNumber2.setNumber("999-888-7777");
        phoneNumber2.setType(Person.PhoneType.MOBILE);

        person.setName("Tester01");
        person.setId(123);
        person.setEmail("tester01@gn.com");
        person.addPhones(phoneNumber1);
        person.addPhones(phoneNumber2);

        addressBook.addPeople(person);

        return addressBook.build();
    }

}

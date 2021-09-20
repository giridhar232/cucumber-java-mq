package com.gn.utils;

public final class Constants {
    public static final String MOSQUITTO_SERVER_PATH = "/usr/local/sbin/mosquitto";
    public static final String MOSQUITTO_CONFIG_PATH = "/usr/local/etc/mosquitto/mosquitto.conf";
    public static final String MOSQUITTO_CLIENT_NAME = "GN_MQTT_CLIENT";
    public static final String MQTT_PROTOCOL = "tcp://";
    public static final int MQTT_WAIT_FOR_RESPONSE_MS = 1000;

    public static final String STRING_EVENT = "string event";
    public static final String PROTOBUF_EVENT = "protobuf event";
    public static final String TEST_MSG_SAMPLE = "sample";
    public static final String TEST_MSG_BUILD = "newly built";

    public static final String KAFKA_START_SCRIPT_PATH = "src/test/java/com/gn/utils/start_kafka.sh";
    public static final String KAFKA_STOP_SCRIPT_PATH = "src/test/java/com/gn/utils/stop_kafka.sh";
    public static final String BOOTSTRAP_SERVER = "localhost:9092";
    public static final String KAFKA_SERVER = "localhost:2181";
}

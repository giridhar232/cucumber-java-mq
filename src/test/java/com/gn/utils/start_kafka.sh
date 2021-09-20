#!/bin/bash
cd /usr/local/kafka_2.12-2.3.0
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties && \
while ! nc -z localhost 2181; do sleep 20; done && \
bin/kafka-server-start.sh -daemon config/server.properties
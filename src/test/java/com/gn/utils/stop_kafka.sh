#!/bin/bash
ps -ef | grep kafka | grep server.properties | awk '{print $2}' | xargs kill
ps -ef | grep kafka | grep zookeeper.properties | awk '{print $2}' | xargs kill
package com.gn.utils;

import org.apache.zookeeper.server.*;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Common {
    public static void addDelayBeforeScenario(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void startZookeeper(String configPath) {
        try {
            QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
            quorumConfiguration.parse(configPath);

            ZooKeeperServerMain zkServer = new ZooKeeperServerMain();
            ServerConfig config = new ServerConfig();
            //configuration.readFrom(quorumConfiguration);
            config.parse(new String[] {"2181", "/Users/gnelap200/kafka_2.12-2.3.0/config"});
            new Thread(() -> {
                try {
                    zkServer.runFromConfig(config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (QuorumPeerConfig.ConfigException e) {
            e.printStackTrace();
        }
    }

    public static void startZKFactory() {
        try {
            int clientPort = 2199; // not standard
            int numConnections = 5000;
            int tickTime = 2000;
            String dataDirectory = System.getProperty("java.io.tmpdir");

            File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();

            ZooKeeperServer server = new ZooKeeperServer(dir, dir, tickTime);
            ServerCnxnFactory factory = new NIOServerCnxnFactory();
            factory.configure(new InetSocketAddress(clientPort), numConnections);

            factory.startup(server); // start the server.
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

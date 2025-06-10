package com.kafkamonitoring.helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class KafkaJmxHelper {

    private String jmxUrl;

    public KafkaJmxHelper(String jmxUrl) {
        this.jmxUrl = jmxUrl;
    }
	
    public Set<ObjectName> getObjectNames() throws RuntimeException, MalformedURLException {
		JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
        try (JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null)) {
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            return mBeanServerConnection.queryNames(null, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to JMX server at " + jmxUrl, e);
        }
    }

    public double getMessagesInPerSec() throws Exception {
        JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
        try (JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null)) {
            MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();
            ObjectName objName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec");
            // OneMinuteRate gives messages/sec over the last minute
            return (Double) mbeanConn.getAttribute(objName, "OneMinuteRate");
        }
    }

    public double getConsumerLag(String clientId, String topic, int partition) throws Exception {
        JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
        try (JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null)) {
            MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();
            String mbeanStr = String.format(
                "kafka.consumer:type=consumer-fetch-manager-metrics,client-id=%s,topic=%s,partition=%d",
                clientId, topic, partition
            );
            ObjectName objName = new ObjectName(mbeanStr);
            // records-lag-max is a common lag metric
            return (Double) mbeanConn.getAttribute(objName, "records-lag-max");
        }
    }
}
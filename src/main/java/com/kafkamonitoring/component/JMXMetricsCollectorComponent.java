package com.kafkamonitoring.component;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

@Component
public class JMXMetricsCollectorComponent {

    public Set<ObjectName> getObjectNames(String jmxUrl) {
        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            return mBeanServerConnection.queryNames(null, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to JMX server at " + jmxUrl, e);
        }
    }

}

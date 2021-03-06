package org.binqua.testing.csd.bridge.external.hazelcast;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.*;
import org.binqua.testing.csd.client.jms.SimpleJmsMessage;
import org.binqua.testing.csd.external.core.BodyFactory;
import org.binqua.testing.csd.external.core.JsonXmlContentTypeBasedBodyFactory;
import org.binqua.testing.csd.httpclient.*;

import static java.util.Arrays.asList;

class Configuration {

    static final String MESSAGES_LIST_NAME = "http-messages";

    private static final String NETWORK_INTERFACE = "127.0.0.1";

    private static final String HAZELCAST_GROUP_NAME = "cucumber-sequence-diagram";

    private static final BodyFactory MESSAGE_BODY_FACTORY = new JsonXmlContentTypeBasedBodyFactory(new ObjectMapper());

    private static final SerializationConfig SERIALIZATION_CONFIG = new SerializationConfig()
        .addSerializerConfig(new SerializerConfig()
                                 .setImplementation(new SimpleHttpRequestStreamSerializer(MESSAGE_BODY_FACTORY))
                                 .setTypeClass(SimpleHttpRequest.class)
        ).addSerializerConfig(new SerializerConfig()
                                  .setImplementation(new SimpleHttpResponseStreamSerializer(MESSAGE_BODY_FACTORY))
                                  .setTypeClass(SimpleHttpResponse.class)
        ).addSerializerConfig(new SerializerConfig()
                                  .setImplementation(new SimpleJmsMessageStreamSerializer(MESSAGE_BODY_FACTORY))
                                  .setTypeClass(SimpleJmsMessage.class)
        );

    Config serverConfig(String clusterIdentifier, int hazelcastPort) {
        return new Config()
            .setNetworkConfig(
                new NetworkConfig()
                    .setPort(hazelcastPort)
                    .setPortAutoIncrement(false)
                    .setInterfaces(new InterfacesConfig().addInterface(NETWORK_INTERFACE).setEnabled(true))
                    .setJoin(new JoinConfig()
                                 .setMulticastConfig(new MulticastConfig().setEnabled(true))
                    )
            )
            .setGroupConfig(new GroupConfig().setName(HAZELCAST_GROUP_NAME + "-" + clusterIdentifier))
            .setSerializationConfig(SERIALIZATION_CONFIG);
    }

    ClientConfig clientConfig(String clusterIdentifier, int hazelcastPort) {
        final ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig()
            .setAddresses(asList(NETWORK_INTERFACE + ":" + hazelcastPort));
        final ClientConfig clientConfig = new ClientConfig()
            .setNetworkConfig(clientNetworkConfig)
            .setGroupConfig(new GroupConfig().setName(HAZELCAST_GROUP_NAME + "-" + clusterIdentifier))
            .setSerializationConfig(SERIALIZATION_CONFIG);
        return clientConfig;
    }

}

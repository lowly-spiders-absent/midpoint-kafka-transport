package ru.nsu.mkc;

import com.evolveum.midpoint.xml.ns._public.common.common_3.CustomTransportConfigurationType;

// A stub while we're figuring out how to extend the schema
public class KafkaTransportConfigurationType extends CustomTransportConfigurationType {
    public String getTopic() {
        return "test";
    }
}

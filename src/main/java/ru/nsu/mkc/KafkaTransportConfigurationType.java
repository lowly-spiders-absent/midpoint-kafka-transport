package ru.nsu.mkc;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CustomTransportConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "KafkaTransportConfigurationType", propOrder = {
    "kafkaServerInitialAddress",
    "topicName",
})
public class KafkaTransportConfigurationType extends CustomTransportConfigurationType {
    private static final long serialVersionUID = 20230506_1011L;

    public static final String NAMESPACE = "urn:example:mkc";
    public static final QName COMPLEX_TYPE = new QName(NAMESPACE, "KafkaTransportConfigurationType");
    public static final ItemName F_KAFKA_SERVER_INITIAL_ADDRESS = new ItemName(NAMESPACE, "kafkaServerInitialAddress");
    public static final ItemName F_TOPIC_NAME = new ItemName(NAMESPACE, "topicName");
    public final static Producer<KafkaTransportConfigurationType> FACTORY =
        new Producer<KafkaTransportConfigurationType>() {
            private static final long serialVersionUID = KafkaTransportConfigurationType.serialVersionUID;

            @Override
            public KafkaTransportConfigurationType run() {
                return new KafkaTransportConfigurationType();
            }
        };

    public KafkaTransportConfigurationType() {
        super();
    }

    @Deprecated
    public KafkaTransportConfigurationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "kafkaServerInitialAddress")
    public String getKafkaServerInitialAddress() {
        return prismGetPropertyValue(F_KAFKA_SERVER_INITIAL_ADDRESS, String.class);

    }

    public void setKafkaServerInitialAddress(String value) {
        prismSetPropertyValue(F_KAFKA_SERVER_INITIAL_ADDRESS, value);
    }

    @XmlElement(name = "topicName")
    public String getTopicName() {
        return prismGetPropertyValue(F_TOPIC_NAME, String.class);
    }

    public void setTopicName(String value) {
        prismSetPropertyValue(F_TOPIC_NAME, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public KafkaTransportConfigurationType kafkaServerInitialAddress(String value) {
        setKafkaServerInitialAddress(value);
        return this;
    }

    public KafkaTransportConfigurationType topicName(String value) {
        setTopicName(value);
        return this;
    }

    @Override
    public KafkaTransportConfigurationType clone() {
        return (KafkaTransportConfigurationType) super.clone();
    }
}

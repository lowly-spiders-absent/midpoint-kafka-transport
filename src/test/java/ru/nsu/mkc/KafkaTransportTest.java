package ru.nsu.mkc;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.notifications.api.transports.TransportSupport;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.repo.common.expression.ExpressionFactory;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CustomTransportConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NotificationMessageAttachmentType;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.nsu.mkc.kafka.MessageDeserializer;
import ru.nsu.mkc.kafka.MessageSerializerTest;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://127.0.0.1:9092", "port=9092"})
class KafkaTransportTest {
    private KafkaTransport kafkaTransport;
    private TransportSupport transportSupport;

    private static final String TEST_TOPIC = "testTopic";
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    @BeforeAll
    static void setupTopic() {
        var properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        try (Admin admin = Admin.create(properties)) {
            int partitions = 1;
            short replicationFactor = 1;
            NewTopic newTopic = new NewTopic(TEST_TOPIC, partitions, replicationFactor);
            CreateTopicsResult result = admin.createTopics(
                    Collections.singleton(newTopic)
            );
            KafkaFuture<Void> future = result.values().get(TEST_TOPIC);
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setupTransport() {
        kafkaTransport = new KafkaTransport();
        transportSupport = makeEmptyTransportSupport();
    }

    @Test
    void configureWithBadName() {
        var configuration = new CustomTransportConfigurationType();
        configuration.setName("a bad name");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            kafkaTransport.configure(configuration, transportSupport);
        });
    }

    @Test
    void sendAndReceive() {
        var consumer = makeConsumer();
        consumer.subscribe(List.of(TEST_TOPIC));

        var configuration = new CustomTransportConfigurationType();
        configuration.setName(BOOTSTRAP_SERVERS + "/" + TEST_TOPIC);
        kafkaTransport.configure(configuration, transportSupport);
        var m1 = new Message();
        m1.setBody("Hello world!");
        m1.getTo().add("Kate");
        var m2 = new Message();
        m2.setContentType("application/json");
        var a = new NotificationMessageAttachmentType();
        a.setContent("{name: Valentine}");
        m2.setFrom("me");
        m2.getAttachments().add(a);
        kafkaTransport.send(m1, "", null, null, new OperationResult("1"));
        kafkaTransport.send(m2, "", null, null, new OperationResult("2"));
        ConsumerRecords<String, Message> records =
                consumer.poll(Duration.ofMillis(5000));
        Assertions.assertEquals(2, records.count());
        var iterator = records.iterator();
        MessageSerializerTest.assertMessageEquals(m1, iterator.next().value());
        MessageSerializerTest.assertMessageEquals(m2, iterator.next().value());
    }

    private TransportSupport makeEmptyTransportSupport() {
        return new TransportSupport() {
            @Override
            public PrismContext prismContext() {
                return null;
            }

            @Override
            public ExpressionFactory expressionFactory() {
                return null;
            }

            @Override
            public RepositoryService repositoryService() {
                return null;
            }

            @Override
            public Protector protector() {
                return null;
            }

            @Override
            public ApplicationContext applicationContext() {
                return null;
            }
        };
    }

    private KafkaConsumer<String, Message> makeConsumer() {
        var consumerProps = new Properties();
        consumerProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class.getName());
        consumerProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "ru.nsu.mkc");
        consumerProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerProps);
    }
}

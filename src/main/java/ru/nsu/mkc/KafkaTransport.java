package ru.nsu.mkc;

/* Includes code from com.evolveum.midpoint.transport.impl.CustomMessageTransport.
 * The original copyright notice:
 *
 *   Copyright (C) 2010-2022 Evolveum and contributors
 *
 *   This work is dual-licensed under the Apache License 2.0
 *   and European Union Public License. See LICENSE file for details.
 */

import com.evolveum.midpoint.notifications.api.events.Event;
import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.notifications.api.transports.Transport;
import com.evolveum.midpoint.notifications.api.transports.TransportSupport;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.transport.impl.TransportUtil;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CustomTransportConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import ru.nsu.mkc.kafka.MessageSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class KafkaTransport implements Transport<CustomTransportConfigurationType> {
    private static final Trace LOGGER = TraceManager.getTrace(KafkaTransport.class);
    private static final String DOT_CLASS = KafkaTransport.class.getName() + ".";
    private static final String PATH_SEPARATOR = "/";

    private String name;
    private CustomTransportConfigurationType configuration;
    private TransportSupport transportSupport;
    private Producer<String, Message> producer;
    private String topic;
    private String kafkaServerInitialAddress;

    @Override
    public void configure(
        @NotNull CustomTransportConfigurationType configuration,
        @NotNull TransportSupport transportSupport)
    {
        this.configuration = Objects.requireNonNull(configuration);
        name = Objects.requireNonNull(configuration.getName());
        parseName(name);
        this.transportSupport = Objects.requireNonNull(transportSupport);
        producer = makeKafkaProducer();
    }

    private void parseName(String name) {
        // example: localhost:9052/testTopic
        var slashIndex = name.indexOf(PATH_SEPARATOR);

        if (slashIndex == -1) {
            throw new IllegalArgumentException("the transport name could not be parsed (expected host:port/topic)");
        }

        kafkaServerInitialAddress = name.substring(0, slashIndex);
        topic = name.substring(slashIndex + 1);
    }

    private Producer<String, Message> makeKafkaProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerInitialAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MessageSerializer.class.getName());

        return new KafkaProducer<>(props);
    }

    @Override
    public void send(Message message, String s, Event event, Task task, OperationResult parentResult) {
        var result = parentResult.createSubresult(DOT_CLASS + "send");
        result.addArbitraryObjectCollectionAsParam("message recipient(s)", message.getTo());
        result.addParam("message subject", message.getSubject());

        var logToFile = configuration.getLogToFile();

        if (logToFile != null) {
            TransportUtil.logToFile(logToFile, TransportUtil.formatToFileNew(message, name), LOGGER);
        }

        int optionsForFilteringRecipient = TransportUtil.optionsForFilteringRecipient(configuration);

        List<String> allowedRecipientTo = new ArrayList<>();
        List<String> forbiddenRecipientTo = new ArrayList<>();
        List<String> allowedRecipientCc = new ArrayList<>();
        List<String> forbiddenRecipientCc = new ArrayList<>();
        List<String> allowedRecipientBcc = new ArrayList<>();
        List<String> forbiddenRecipientBcc = new ArrayList<>();

        String file = configuration.getRedirectToFile();
        if (optionsForFilteringRecipient != 0) {
            TransportUtil.validateRecipient(
                allowedRecipientTo, forbiddenRecipientTo, message.getTo(),
                configuration, task, result,
                transportSupport.expressionFactory(), MiscSchemaUtil.getExpressionProfile(), LOGGER
            );
            TransportUtil.validateRecipient(
                allowedRecipientCc, forbiddenRecipientCc, message.getCc(),
                configuration, task, result,
                transportSupport.expressionFactory(), MiscSchemaUtil.getExpressionProfile(), LOGGER
            );
            TransportUtil.validateRecipient(
                allowedRecipientBcc, forbiddenRecipientBcc, message.getBcc(),
                configuration, task, result,
                transportSupport.expressionFactory(), MiscSchemaUtil.getExpressionProfile(), LOGGER
            );

            if (file != null) {
                if (!forbiddenRecipientTo.isEmpty() || !forbiddenRecipientCc.isEmpty()
                    || !forbiddenRecipientBcc.isEmpty())
                {
                    message.setTo(forbiddenRecipientTo);
                    message.setCc(forbiddenRecipientCc);
                    message.setBcc(forbiddenRecipientBcc);
                    writeToFile(message, file, result);
                }

                message.setTo(allowedRecipientTo);
                message.setCc(allowedRecipientCc);
                message.setBcc(allowedRecipientBcc);
            }
        } else if (file != null) {
            writeToFile(message, file, result);
            return;
        }

        try {
            LOGGER.trace("Sending message to Kafka topic {}", topic);
            producer.send(new ProducerRecord<>(topic, message)).get();
            LOGGER.trace("Message sent to Kafka topic {}", topic);
            result.recordSuccess();
        } catch (Exception e) {
            LoggingUtils.logException(LOGGER, "Couldn't write message to Kafka topic {}", e, topic);
            result.recordFatalError("Couldn't write message to Kafka topic " + topic + ": " + e.getMessage(), e);

            // this is one of these cases where the linter is technically correct yet nonsensical...
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void writeToFile(Message message, String file, OperationResult result) {
        try {
            TransportUtil.appendToFile(file, formatToFile(message));
            result.recordSuccess();
        } catch (IOException e) {
            LoggingUtils.logException(LOGGER, "Couldn't write to message redirect file {}", e, file);
            result.recordPartialError("Couldn't write to message redirect file " + file, e);
        }
    }

    private String formatToFile(Message mailMessage) {
        return "================ " + new Date() + " =======\n" + mailMessage.toString() + "\n\n";
    }

    @Override
    public String getDefaultRecipientAddress(FocusType focusType) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CustomTransportConfigurationType getConfiguration() {
        return configuration;
    }
}

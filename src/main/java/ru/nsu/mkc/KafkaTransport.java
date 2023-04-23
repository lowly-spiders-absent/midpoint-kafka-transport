package ru.nsu.mkc;

import com.evolveum.midpoint.notifications.api.events.Event;
import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.notifications.api.transports.Transport;
import com.evolveum.midpoint.notifications.api.transports.TransportSupport;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class KafkaTransport implements Transport<KafkaTransportConfigurationType> {
    private static final Trace LOGGER = TraceManager.getTrace(KafkaTransport.class);
    private static final String DOT_CLASS = KafkaTransport.class.getName() + ".";

    private KafkaTransportConfigurationType configuration;
    private String name;
    private TransportSupport transportSupport;

    @Override
    public void configure(
        @NotNull KafkaTransportConfigurationType configuration,
        @NotNull TransportSupport transportSupport)
    {
        this.configuration = Objects.requireNonNull(configuration);
        name = Objects.requireNonNull(configuration.getName());
        this.transportSupport = Objects.requireNonNull(transportSupport);
        // TODO: set up everything here
    }

    @Override
    public void send(Message message, String s, Event event, Task task, OperationResult operationResult) {
        // TODO
        throw new RuntimeException("not implemented");
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
    public KafkaTransportConfigurationType getConfiguration() {
        return configuration;
    }
}

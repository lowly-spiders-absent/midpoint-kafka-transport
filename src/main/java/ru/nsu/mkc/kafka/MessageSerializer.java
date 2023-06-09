package ru.nsu.mkc.kafka;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import ru.nsu.mkc.dto.MessageMapper;

@Slf4j
public class MessageSerializer implements Serializer<Message> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, Message data) {
        try {
            if (data == null) {
                log.error("Null received at serializing");
                return new byte[0];
            }

            return objectMapper.writeValueAsBytes(MessageMapper.messageToDto(data));
        } catch (Exception e) {
            throw new SerializationException("Error when serializing MessageDto to byte[]");
        }
    }
}

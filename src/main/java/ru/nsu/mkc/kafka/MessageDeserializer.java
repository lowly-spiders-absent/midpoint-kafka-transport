package ru.nsu.mkc.kafka;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.nsu.mkc.dto.MessageDto;
import ru.nsu.mkc.dto.MessageMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
public class MessageDeserializer implements Deserializer<Message> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Message deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                log.error("Null received at deserializing");
                return null;
            }

            return MessageMapper.dtoToMessage(
                objectMapper.readValue(new String(data, StandardCharsets.UTF_8), MessageDto.class)
            );
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to MessageDto");
        }
    }
}

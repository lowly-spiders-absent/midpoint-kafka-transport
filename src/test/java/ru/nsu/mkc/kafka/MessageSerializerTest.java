package ru.nsu.mkc.kafka;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NotificationMessageAttachmentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MessageSerializerTest {
    private static final String topicName = "testTopic";
    private MessageSerializer serializer;
    private MessageDeserializer deserializer;

    public static void assertMessageEquals(Message m, Message md) {
        Assertions.assertEquals(m.getContentType(), md.getContentType());
        Assertions.assertIterableEquals(m.getTo(), md.getTo());
        Assertions.assertIterableEquals(m.getCc(), md.getCc());
        Assertions.assertIterableEquals(m.getBcc(), md.getBcc());
        Assertions.assertEquals(m.getBody(), md.getBody());
        Assertions.assertEquals(m.getFrom(), md.getFrom());
        Assertions.assertEquals(m.getSubject(), md.getSubject());
        Assertions.assertIterableEquals(m.getAttachments(), md.getAttachments());
    }

    @BeforeEach
    void setup() {
        serializer = new MessageSerializer();
        deserializer = new MessageDeserializer();
    }

    @Test
    void serializeVerboseMessage() {
        Message m = new Message();
        m.setFrom("Billy Millington");
        m.setSubject("user");
        m.setContentType("application/json");
        m.getTo().add("Jack The Sparrow");
        m.getTo().add("Wicked John");
        m.getCc().add("Bloody Mary");
        m.getBcc().add("Agent A");
        m.getBcc().add("Agent B");
        m.setBody("{data: {user: administrator, password: 5ecr3t}}");
        var a = new NotificationMessageAttachmentType();
        a.setContentType("text");
        a.setContent("don't enter the data, it's a trick");
        m.getAttachments().add(a);
        byte[] bytes = serializer.serialize(topicName, m);
        Message md = deserializer.deserialize(topicName, bytes);
        assertMessageEquals(m, md);
    }

    @Test
    void serializeEmptyMessage() {
        Message m = new Message();
        byte[] bytes = serializer.serialize(topicName, m);
        Message md = deserializer.deserialize(topicName, bytes);
        assertMessageEquals(m, md);
    }

    @Test
    void serializeMessageWithAttachment() {
        Message m = new Message();
        var a = new NotificationMessageAttachmentType();
        Map<String, Integer> content = new HashMap<>();
        content.put("key1", 70);
        content.put("key2", 157);
        a.setContent(content);
        m.getAttachments().add(a);
        byte[] bytes = serializer.serialize(topicName, m);
        Message md = deserializer.deserialize(topicName, bytes);
        assertMessageEquals(m, md);
        Assertions.assertEquals(m.getAttachments().get(0).getContent(), md.getAttachments().get(0).getContent());
    }
}

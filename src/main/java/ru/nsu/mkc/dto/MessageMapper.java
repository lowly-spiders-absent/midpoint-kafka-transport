package ru.nsu.mkc.dto;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NotificationMessageAttachmentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException {
        Message m = new Message();
        m.setFrom("alex");
        m.getTo().add("misha");
        m.setBody("hello misha");
        m.setSubject("greetings");
        NotificationMessageAttachmentType a = new NotificationMessageAttachmentType();
        a.contentType("number");
        a.content(9);
        a.fileName("num.txt");
        m.getAttachments().add(a);
        MessageDto messageDto = messageToDto(m);
        byte[] data = objectMapper.writeValueAsBytes(messageDto);
        MessageDto converted = objectMapper.readValue(new String(data, StandardCharsets.UTF_8), MessageDto.class);
        System.out.println(converted.equals(messageDto));
        System.out.println(converted);
    }

    static public MessageDto messageToDto(Message m) {
        List<AttachmentDto> attachmentDtos = new ArrayList<>();
        for (NotificationMessageAttachmentType a : m.getAttachments()) {
            attachmentDtos.add(attachmentToDto(a));
        }
        return MessageDto.builder().attachments(attachmentDtos).bcc(m.getBcc()).cc(m.getCc())
                .contentType(m.getContentType()).body(m.getBody()).from(m.getFrom()).to(m.getTo())
                .subject(m.getSubject()).build();
    }

    static public Message dtoToMessage(MessageDto m) {
        Message converted = new Message();
        converted.setFrom(m.getFrom());
        converted.setTo(m.getTo());
        converted.setSubject(m.getSubject());
        converted.setBody(m.getBody());
        converted.setBcc(m.getBcc());
        converted.setCc(m.getCc());
        converted.setContentType(m.getContentType());
        for (AttachmentDto a : m.getAttachments()) {
            converted.getAttachments().add(dtoToAttachment(a));
        }
        return converted;
    }

    static private AttachmentDto attachmentToDto(NotificationMessageAttachmentType a) {
        return AttachmentDto.builder().content(a.getContent()).contentType(a.getContentType()).
                fileName(a.getFileName()).contentFromFile(a.getContentFromFile()).build();
    }

    static private NotificationMessageAttachmentType dtoToAttachment(AttachmentDto a) {
        NotificationMessageAttachmentType converted = new NotificationMessageAttachmentType();
        converted.setContentType(a.getContentType());
        converted.setContent(a.getContent());
        converted.setContentFromFile(a.getContentFromFile());
        converted.setFileName(a.getFileName());
        return converted;
    }
}

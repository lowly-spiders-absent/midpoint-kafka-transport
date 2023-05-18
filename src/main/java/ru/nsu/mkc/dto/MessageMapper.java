package ru.nsu.mkc.dto;

import com.evolveum.midpoint.notifications.api.transports.Message;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NotificationMessageAttachmentType;

import java.util.ArrayList;
import java.util.List;

public class MessageMapper {
    public static MessageDto messageToDto(Message m) {
        List<AttachmentDto> attachmentDtos = new ArrayList<>();
        for (NotificationMessageAttachmentType a : m.getAttachments()) {
            attachmentDtos.add(attachmentToDto(a));
        }
        return MessageDto.builder().attachments(attachmentDtos).bcc(m.getBcc()).cc(m.getCc())
                .contentType(m.getContentType()).body(m.getBody()).from(m.getFrom()).to(m.getTo())
                .subject(m.getSubject()).build();
    }

    public static Message dtoToMessage(MessageDto m) {
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

    private static AttachmentDto attachmentToDto(NotificationMessageAttachmentType a) {
        return AttachmentDto.builder().content(a.getContent()).contentType(a.getContentType()).
                fileName(a.getFileName()).contentFromFile(a.getContentFromFile()).build();
    }

    private static NotificationMessageAttachmentType dtoToAttachment(AttachmentDto a) {
        NotificationMessageAttachmentType converted = new NotificationMessageAttachmentType();
        converted.setContentType(a.getContentType());
        converted.setContent(a.getContent());
        converted.setContentFromFile(a.getContentFromFile());
        converted.setFileName(a.getFileName());
        return converted;
    }
}

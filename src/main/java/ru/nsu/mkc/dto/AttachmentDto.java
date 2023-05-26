package ru.nsu.mkc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentDto {
    private String contentType;
    private Object content;
    private String contentFromFile;
    private String fileName;
}

package com.example.eventhub.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "EventCreateMultipartRequest")
public class EventCreateMultipartRequest {

    @Schema(description = "Event data in JSON format")
    public EventCreateForm event;

    @Schema(type = "string", format = "binary")
    public MultipartFile image;
}

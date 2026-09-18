package com.example.eventhub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)

class S3ServiceTest {
    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "eventhub-storage-174302433598");
    }

    @Test
    @DisplayName("Should upload event image to S3")
    void upload_ShouldUploadImage_WhenValidParametersAreProvided() {
        Long eventId = 22L;
        byte[] content = "fake-image".getBytes();
        String contentType = "image/png";

        String result = s3Service.upload(eventId, content, contentType);

        assertEquals("events/22/image", result);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should generate a presigned URL for event image")
    void generatePresignedUrl_ShouldGenerateUrl_WhenValidKeyIsProvided() throws MalformedURLException {
        String key = "events/22/image";
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        URI expectedUrl;

        try {
            expectedUrl = new URI("https://fake-s3-url.com/events/22/image");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);
        when(presignedRequest.url()).thenReturn(expectedUrl.toURL());

        String result = s3Service.generatePresignedUrl(key);

        assertEquals("https://fake-s3-url.com/events/22/image", result);

        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
        verify(presignedRequest).url();
    }
}
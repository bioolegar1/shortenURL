package br.com.bioolegari.encurtalink.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortenUrlResponse {
    private String shortUrl;
    private String originalUrl;
    private String shortKey;
    private String qrCodeUrl;
    private LocalDateTime expiresAt;
}

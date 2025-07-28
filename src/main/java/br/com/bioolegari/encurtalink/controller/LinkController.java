package br.com.bioolegari.encurtalink.controller;


import br.com.bioolegari.encurtalink.dto.ShortenUrlrequest;
import br.com.bioolegari.encurtalink.dto.ShortenUrlResponse;
import br.com.bioolegari.encurtalink.model.Link;
import br.com.bioolegari.encurtalink.model.User;
import br.com.bioolegari.encurtalink.service.LinkService;
import br.com.bioolegari.encurtalink.service.QrCodeService;
import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class LinkController {
    
    private final LinkService linkService;
    private final QrCodeService qrCodeService;
    
    @PostMapping("/links")

    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @RequestBody ShortenUrlrequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest servletRequest
            ){

        Link createdLink = linkService.createShortLink(request.getUrl(), user);

        //Monta a url com base no dominio:
        String baseUrl = ServletUriComponentsBuilder.fromRequest(servletRequest)
                .replacePath(null)
                .build()
                .toUriString();
        //Constroi a resposta
        
        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .originalUrl(createdLink.getOriginalUrl())
                .shortKey(createdLink.getShortKey())
                .shortUrl(baseUrl + "/" + createdLink.getShortKey())
                .qrCodeUrl(baseUrl + "/qrcode/" + createdLink.getShortKey())
                .expiresAt(createdLink.getExpiredAt())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortKey) {
        String originalUrl = linkService.getOriginalUrlAndIncrementClick(shortKey);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping(value = "/qrcode/{shortKey}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortKey, HttpServletRequest servletRequest) {
        try {
            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                    .replacePath(null)
                    .build()
                    .toUriString();
            String shortUrl = baseUrl + "/" + shortKey;
            byte[] qrCodeImage = qrCodeService.generateQrCodeImage(shortUrl,250,250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);
        }
        catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

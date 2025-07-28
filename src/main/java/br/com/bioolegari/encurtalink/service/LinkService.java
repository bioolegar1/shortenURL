package br.com.bioolegari.encurtalink.service;

import br.com.bioolegari.encurtalink.model.Link;
import br.com.bioolegari.encurtalink.model.User;
import br.com.bioolegari.encurtalink.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    @Transactional
    public Link createShortLink(String originalUrl, User user) {
        String shortKey;
        do {
            shortKey = RandomStringUtils.randomAlphanumeric(7);
        }while (linkRepository.findByShortKey(shortKey).isPresent());

        Link link = Link.builder()
                .originalUrl(originalUrl)
                .shortKey(shortKey)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .clickCount(0)
                .build();
        return linkRepository.save(link);
    }

    @Transactional
    public String getOriginalUrlAndIncrementClick(String shortKey) {
        Link link = linkRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("Link não encontrado"));

        if (link.getExpiredAt() != null && link.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este Link Expirou");
        }

        link.setClickCount(link.getClickCount() + 1);
        linkRepository.save(link);

        return link.getOriginalUrl();
    }
}

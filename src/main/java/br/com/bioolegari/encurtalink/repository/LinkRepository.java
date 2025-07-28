package br.com.bioolegari.encurtalink.repository;

import br.com.bioolegari.encurtalink.model.Link;
import br.com.bioolegari.encurtalink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortKey(String shortKey);
    List<Link> findByUser(User user);
}

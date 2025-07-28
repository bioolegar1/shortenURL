package br.com.bioolegari.encurtalink.repository;

import br.com.bioolegari.encurtalink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername (String username);
    Optional<User> findByEmail (String email);

}

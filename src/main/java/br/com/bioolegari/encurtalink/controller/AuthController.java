package br.com.bioolegari.encurtalink.controller;


import br.com.bioolegari.encurtalink.dto.AuthResponse;
import br.com.bioolegari.encurtalink.dto.LoginRequest;
import br.com.bioolegari.encurtalink.dto.RegisterRequest;
import br.com.bioolegari.encurtalink.model.User;
import br.com.bioolegari.encurtalink.repository.UserRepository;
import br.com.bioolegari.encurtalink.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de Usuário já esta em uso!");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já está em uso!");
        }


        User user = User.builder()
            .username(request.getUsername())
                .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new ResponseEntity<>(AuthResponse.builder()
                .token(token)
                .expiresIn(jwtExpiration)
                .build(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        //Validação usuario e senha
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        //se der tudo certo terá o userDetails
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .expiresIn(jwtExpiration)
                .build()
        );
    }
}

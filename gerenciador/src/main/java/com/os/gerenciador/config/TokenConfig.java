package com.os.gerenciador.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenConfig {

    @Value("${api.security.token.secret}")
    private String token;

    public String generateToken(UsuarioPrincipal usuario) {

        Algorithm algorithm = Algorithm.HMAC256(token);
        String role = usuario.role();

        var jwtBuilder = JWT.create()
                .withClaim("id", usuario.id().longValue())
                .withSubject(usuario.email())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .withIssuedAt(Instant.now());

        if (role != null) {
            jwtBuilder.withClaim("role", role);
        }
        return jwtBuilder.sign(algorithm);
    }

    public Optional<JWTUserData> validateToken(String jwtToken) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(token);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(jwtToken);

            String role = jwt.getClaim("role").asString();
            return Optional.of(JWTUserData.builder()
                    .userId(jwt.getClaim("id").asLong())
                    .email(jwt.getSubject())
                    .role(role)
                    .build());
        }
        catch (JWTVerificationException exception){
            return Optional.empty();
        }
    }

}

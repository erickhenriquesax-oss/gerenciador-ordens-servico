package com.os.gerenciador.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenConfig tokenConfig;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizeHeader = request.getHeader("Authorization");
        if (authorizeHeader != null && authorizeHeader.startsWith("Bearer ")) {
            String token = authorizeHeader.substring("Bearer ".length());
            Optional<JWTUserData> optUser = tokenConfig.validateToken(token);
            if (optUser.isPresent()) {

                JWTUserData jwtUserData = optUser.get();
                List<GrantedAuthority> authorities = (jwtUserData.role() != null && !jwtUserData.role().isBlank())
                        ? List.of(new SimpleGrantedAuthority(jwtUserData.role())) : List.of();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUserData, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }else{
            filterChain.doFilter(request, response);
        }
    }
}

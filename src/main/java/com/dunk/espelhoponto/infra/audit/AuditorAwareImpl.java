package com.dunk.espelhoponto.infra.audit;

import com.dunk.espelhoponto.entity.Usuario;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();
        return Optional.ofNullable(usuario.getId());
    }


}

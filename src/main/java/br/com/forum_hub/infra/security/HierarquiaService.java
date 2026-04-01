package br.com.forum_hub.infra.security;

import br.com.forum_hub.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HierarquiaService {

    @Autowired
    private RoleHierarchy roleHierarchy;

    public boolean usuarioNaoTemPermissoes(Usuario logado, Usuario autor, String perfilDesejado) {
        return logado.getAuthorities().stream()
                .flatMap(autoridade -> roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade)).stream())
                .noneMatch(perfil -> perfil.getAuthority().equals(perfilDesejado) || logado.getId().equals(autor.getId()));
    }
}

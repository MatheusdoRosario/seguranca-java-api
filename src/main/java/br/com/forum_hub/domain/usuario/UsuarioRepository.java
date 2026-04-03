package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCaseAndVerificadoTrue(String username);

    Optional<Usuario> findByEmailIgnoreCaseAndVerificadoTrueAndAtivoTrue(String username);

    Optional<Usuario> findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(String email, String nomeUsuario);

    Optional<Usuario> findByToken(String codigo);

    Optional<Usuario> findByNomeUsuarioAndVerificadoTrueAndAtivoTrue(String nomeUsuario);
}

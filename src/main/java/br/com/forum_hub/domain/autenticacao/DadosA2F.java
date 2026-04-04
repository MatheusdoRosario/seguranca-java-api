package br.com.forum_hub.domain.autenticacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosA2F(@NotBlank String email,
                       @NotBlank String codigo,
                       @NotNull MetodosA2F metodo) {
}

package br.com.forum_hub.controller.usuario;

import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.usuario.*;
import br.com.forum_hub.domain.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/{nomeUsuario}")
    public ResponseEntity<DadosListagemUsuario> listarPorNomeUsuario(@PathVariable String nomeUsuario) {
        var usuario = service.listarPorNomeUsuario(nomeUsuario);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/editar-perfil")
    public ResponseEntity<DadosListagemUsuario> atualizar(@RequestBody @Valid DadosAtualizacaoUsuario dados, @AuthenticationPrincipal Usuario logado) {
        var usuario = service.editarPerfil(dados, logado);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid DadosAtualizacaoSenha dados, @AuthenticationPrincipal Usuario logado) {
        service.alterarSenha(dados, logado);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/desativar")
    public ResponseEntity<Void> banirUsuario(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        service.desativarUsuario(id, logado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/adicionar-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> adicionarPerfil(@PathVariable Long id, @RequestBody @Valid DadosPerfil dados) {
        var usuario = service.adicionarPerfil(id, dados);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PatchMapping("/remover-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> removerPerfil(@PathVariable Long id, @RequestBody @Valid DadosPerfil dados) {
        var usuario = service.removerPerfil(id, dados);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PatchMapping("/reativar-perfil/{id}")
    public ResponseEntity<Void> reativarPerfil(@PathVariable Long id) {
        service.reativarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}

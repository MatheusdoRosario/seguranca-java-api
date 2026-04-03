package br.com.forum_hub.controller.autenticacao;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.service.A2FService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class A2FController {

    @Autowired
    private A2FService service;

    @PatchMapping("/configurar-a2f")
    public ResponseEntity<String> gerarQrCode(@AuthenticationPrincipal Usuario logado) {
        var url = service.gerarQrCode(logado);
        return ResponseEntity.ok(url);
    }

    @PatchMapping("/ativar-a2f")
    public ResponseEntity<Void> ativarA2f(@RequestParam String codigo, @AuthenticationPrincipal Usuario logado) {
        service.ativarA2f(codigo, logado);
        return ResponseEntity.noContent().build();
    }
}

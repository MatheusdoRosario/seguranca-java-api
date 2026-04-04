package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.MetodosA2F;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.autenticacao.github.LoginGithubService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.service.RegistroService;
import br.com.forum_hub.domain.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/github")
public class LoginGithubController {

    @Autowired
    private LoginGithubService loginGithubService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<Void> redirecionarGithub() {
        var url = loginGithubService.gerarUrl();
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autenticarUsuarioOAuth(@RequestParam String code) {
        var email = loginGithubService.obterEmail(code);

        var usuario = (Usuario) usuarioService.loadUserByUsername(email);
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        String refreshToken = tokenService.gerarRefreshToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, MetodosA2F.APP));
    }

    @GetMapping("/registro")
    public ResponseEntity<Void> redirecionarRegistroGithub() {
        var url = loginGithubService.gerarUrlRegistro();
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/registro-autorizado")
    public ResponseEntity<DadosToken> registrarOAuth(@RequestParam String code) {
        var dadosUsuario = loginGithubService.obterDadosOAuth(code);
        var usuario = registroService.cadastrarVerificado(dadosUsuario);

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, MetodosA2F.APP));
    }
}

package br.com.forum_hub.controller.autenticacao;

import br.com.forum_hub.domain.autenticacao.*;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.domain.usuario.service.A2FService;
import br.com.forum_hub.infra.security.totp.TotpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TotpService totpService;

    @Autowired
    private A2FService a2FService;

    @PostMapping("/login")
    public ResponseEntity<DadosToken> efetuarLogin(@Valid @RequestBody DadosLogin dados) {
        var autenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = authenticationManager.authenticate(autenticationToken);

        var usuario = (Usuario) authentication.getPrincipal();
        
        if (usuario.getMetodosA2F() != null) {
            return ResponseEntity.ok(new DadosToken(null, null, usuario.getMetodosA2F()));
        }

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, null));
    }

    @PostMapping("/verificar-a2f")
    public ResponseEntity<DadosToken> verificarSegundoFator(@Valid @RequestBody DadosA2F dadosA2F) {
        var usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrueAndAtivoTrue(dadosA2F.email())
                .orElseThrow();
        if (dadosA2F.metodo().equals(MetodosA2F.APP)) {
            var codigoValido = totpService.verificarCodigo(dadosA2F.codigo(), usuario);
            if (!codigoValido) {
                throw new BadCredentialsException("Código inválido");
            }
        } else if (dadosA2F.metodo().equals(MetodosA2F.EMAIL)) {
            a2FService.validarCodigoEmail(dadosA2F.codigo(), usuario);
        }
        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, null));
    }

    @PostMapping("/atualizar-token")
    public ResponseEntity<DadosToken> atualizarToken(@Valid @RequestBody DadosRefreshToken dados) {
        var refreshToken = dados.refreshToken();
        Long idUsuario = Long.valueOf(tokenService.verificarToken(refreshToken));
        var usuario = usuarioRepository.findById(idUsuario).orElseThrow();

        String tokenAcessoAtualizado = tokenService.gerarToken(usuario);
        String refreshTokenAtualizado = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcessoAtualizado, refreshTokenAtualizado, null));
    }
}

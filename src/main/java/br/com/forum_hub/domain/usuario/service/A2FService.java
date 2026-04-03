package br.com.forum_hub.domain.usuario.service;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.security.totp.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class A2FService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TotpService totpService;

    @Transactional
    public String gerarQrCode(Usuario logado) {
        var secret = totpService.gerarSecret();
        logado.gerarSecret(secret);
        usuarioRepository.save(logado);

        return totpService.gerarQrCode(logado);
    }

    @Transactional
    public void ativarA2f(String codigo, Usuario logado) {
        if (logado.isA2fAtiva()) {
            throw new RegraDeNegocioException("Sua autenticação de dois fatores já está ativada!");
        }

        var codigoValido = totpService.verificarCodigo(codigo, logado);
        if (!codigoValido) {
            throw new RegraDeNegocioException("Código inválido!");
        }

        logado.ativarA2f();
        usuarioRepository.save(logado);
    }
}

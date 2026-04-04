package br.com.forum_hub.domain.usuario.service;

import br.com.forum_hub.domain.autenticacao.MetodosA2F;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.security.totp.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class A2FService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TotpService totpService;

    @Autowired
    private EmailService emailService;

    private static final int MAX_TENTATIVAS = 5;

    @Transactional
    public String gerarQrCode(Usuario logado) {
        var secret = totpService.gerarSecret();
        logado.gerarSecret(secret);
        usuarioRepository.save(logado);

        return totpService.gerarQrCode(logado);
    }

    @Transactional
    public String gerarCodigoEmail() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public void ativarA2f(String codigo, Usuario logado, MetodosA2F metodo) {
        if (logado.getMetodosA2F() != null) {
            throw new RegraDeNegocioException("Sua autenticação de dois fatores já está ativada!");
        }

        if (metodo == MetodosA2F.APP) {
            var codigoValido = totpService.verificarCodigo(codigo, logado);
            if (!codigoValido) {
                throw new RegraDeNegocioException("Código inválido!");
            }
            logado.ativarA2f(metodo);
        } else if (metodo == MetodosA2F.EMAIL) {
            validarCodigoEmail(codigo, logado);
            logado.ativarA2f(metodo);
        }
        logado.setEmailCode(null);
        logado.setEmailCodeExpiracao(null);
        usuarioRepository.save(logado);
    }

    @Transactional
    public void enviarCodigoEmail(Usuario logado) {
        String code = gerarCodigoEmail();
        logado.setEmailCode(code);
        logado.setEmailCodeExpiracao(LocalDateTime.now().plusMinutes(5));
        logado.setTentativasA2F(0);
        usuarioRepository.save(logado);
        emailService.enviarCodigoAutenticacaoEmail(logado.getUsername(), code);
    }

    @Transactional
    public void validarCodigoEmail(String codigo, Usuario logado) {
        if (logado.getEmailCodeExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("Código expirado!");
        }
        if (logado.getTentativasA2F() >= MAX_TENTATIVAS) {
            throw new RegraDeNegocioException("Numero máximo de tentativas excedido!");
        }
        if (!codigo.equals(logado.getEmailCode())) {
            logado.setTentativasA2F(logado.getTentativasA2F() + 1);
            usuarioRepository.save(logado);
            throw new RegraDeNegocioException("Código inválido!");
        }
        logado.setEmailCode(null);
        logado.setEmailCodeExpiracao(null);
        logado.setTentativasA2F(0);
        usuarioRepository.save(logado);
    }
}

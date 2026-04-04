package br.com.forum_hub.infra.security.totp;

import br.com.forum_hub.domain.usuario.Usuario;
import com.atlassian.onetime.core.TOTPGenerator;
import com.atlassian.onetime.model.TOTPSecret;
import com.atlassian.onetime.service.RandomSecretProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    public String gerarSecret() {
        return new RandomSecretProvider().generateSecret().getBase32Encoded();
    }

    public String gerarQrCode(Usuario logado) {
        var issuer = "Fórum Hub";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, logado.getUsername(), logado.getSecret(), issuer
        );
    }

    public Boolean verificarCodigo(String codigo, Usuario logado) {
        var secretDecodificada = TOTPSecret.Companion
                .fromBase32EncodedString(logado.getSecret());
        var codigoAplicacao = new TOTPGenerator()
                .generateCurrent(secretDecodificada).getValue();
        return codigoAplicacao.equals(codigo);
    }
}

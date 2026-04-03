package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class LoginGoogleService {

    @Value("${google.oauth.clienteId}")
    private String clienteId;
    @Value("${google.oauth.clientSecret}")
    private String clientSecret;
    private final String redirect_uri = "http://localhost:8080/login/google/autorizado";
    private final String redirectUriRegistro = "http://localhost:8080/login/google/registro-autorizado";
    private final RestClient restClient;

    public LoginGoogleService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clienteId +
                "&redirect_uri=" + redirect_uri +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code";
    }

    private String obterToken(String code, String uri) {
        var resposta = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", clienteId, "client_secret", clientSecret,
                        "redirect_uri", uri, "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class);
        return resposta.get("id_token").toString();
    }

    public String obterEmail(String code) {
        var token = obterToken(code, redirect_uri);

        var decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("email").asString();
    }

    public String gerarUrlRegistro() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clienteId +
                "&redirect_uri=" + redirectUriRegistro +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "%20https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";
    }

    public DadosCadastroUsuario obterDadosOAuth(String code) {
        var token = obterToken(code, redirectUriRegistro);
        var decodedJWT = JWT.decode(token);
        var email = decodedJWT.getClaim("email").asString();
        var senha = UUID.randomUUID().toString();
        var nomeCompleto = decodedJWT.getClaim("name").asString();
        var nomeUsuario = email.split("@")[0];

        return new DadosCadastroUsuario(email, senha, senha, nomeCompleto, nomeUsuario, null, null);
    }
}

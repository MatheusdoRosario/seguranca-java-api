package br.com.forum_hub.domain.autenticacao.github;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class LoginGithubService {

    private final String clienteId = "${GITHUB_CLIENT_ID}";
    private final String clientSecret = "${GITHUB_CLIENT_SECRET}";
    private final String redirect_uri = "http://localhost:8080/login/github/autorizado";
    private final String redirectUriRegistro = "http://localhost:8080/login/github/registro-autorizado";
    private final RestClient restClient;

    public LoginGithubService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clienteId +
                "&redirect_uri=" + redirect_uri +
                "&scope=user:email";
    }

    private String obterToken(String code, String id, String uri) {
        var resposta = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", id, "client_secret", clientSecret,
                        "redirect_uri", uri))
                .retrieve()
                .body(Map.class);
        return resposta.get("access_token").toString();
    }

    public String obterEmail(String code) {
        var token = obterToken(code, clienteId, redirect_uri);

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return enviarRequisicaoEmail(headers);
    }

    public String gerarUrlRegistro() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clienteId +
                "&redirect_uri=" + redirectUriRegistro +
                "&scope=red:user,user:email";
    }

    public DadosCadastroUsuario obterDadosOAuth(String code) {
        var accessToken = obterToken(code, clienteId, redirectUriRegistro);
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        var email = enviarRequisicaoEmail(headers);

        var resposta = restClient.get()
                .uri("https://api.github.com/user")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

        var nomeCompleto = resposta.get("name").toString();
        var nomeUsuario = resposta.get("login").toString();
        var senha = UUID.randomUUID().toString();

        return new DadosCadastroUsuario(email, senha, senha,nomeCompleto, nomeCompleto, null, null);
    }

    private String enviarRequisicaoEmail(HttpHeaders headers) {
        var resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);
        for(DadosEmail d: resposta){
            if(d.primary() && d.verified())
                return d.email();
        }
        return null;
    }
}

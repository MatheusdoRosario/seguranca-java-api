package br.com.forum_hub.domain.autenticacao.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGithubService {

    private final String clienteId = "Ov23liuoIK84HT7BwsB0";
    private final String clientSecret = "96595014577ff5185f9c79733456be73f2742c24";
    private final String redirect_uri = "http://localhost:8080/login/github/autorizado";
    private final RestClient restClient;

    public LoginGithubService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clienteId +
                "&redirect_uri=" + redirect_uri +
                "&scope=read:user,user:email";
    }

    private String obterToken(String code) {
        var resposta = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", clienteId, "client_secret", clientSecret,
                        "redirect_uri", redirect_uri))
                .retrieve()
                .body(Map.class);
        return resposta.get("access_token").toString();
    }

    public String obterEmail(String code) {
        var token = obterToken(code);

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        return resposta;
    }
}

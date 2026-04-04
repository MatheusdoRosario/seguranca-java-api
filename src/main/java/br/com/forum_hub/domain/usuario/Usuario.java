package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.MetodosA2F;
import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCompleto;
    private String email;
    private String senha;
    private String nomeUsuario;
    private String biografia;
    private String miniBiografia;
    private Boolean verificado;
    private String token;
    private LocalDateTime expiracaoToken;
    private Boolean ativo;
    private String secret;
    @Enumerated(value = EnumType.STRING)
    private MetodosA2F metodosA2F;
    private String emailCode;
    private LocalDateTime emailCodeExpiracao;
    private int tentativasA2F;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_perfis",
                joinColumns = @JoinColumn(name = "usuario_id"),
                inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografa, Perfil perfil, Boolean verificado) {
        this.nomeCompleto = dados.nomeCompleto();
        this.email = dados.email();
        this.senha = senhaCriptografa;
        this.nomeUsuario = dados.nomeUsuario();
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();
        if(verificado){
            aprovarUsuario();
        } else {
            this.verificado = false;
            this.token = UUID.randomUUID().toString();
            this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
            this.ativo = false;
        }
        this.perfis.add(perfil);
        this.tentativasA2F = 0;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis;
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public Long getId() {
        return this.id;
    }

    public String getNomeCompleto() {
        return this.nomeCompleto;
    }

    public String getNomeUsuario() {
        return this.nomeUsuario;
    }

    public String getBiografia() {
        return this.biografia;
    }

    public String getMiniBiografia() {
        return this.miniBiografia;
    }

    public String getToken() {
        return this.token;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public LocalDateTime getEmailCodeExpiracao() {
        return emailCodeExpiracao;
    }

    public int getTentativasA2F() {
        return tentativasA2F;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    public void verificar() {
        if (expiracaoToken.isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("Link de verificação expirou!");
        }
        aprovarUsuario();
    }

    public Usuario alterarDados(DadosAtualizacaoUsuario dados) {
        if (dados.nomeUsuario() != null) {
            this.nomeUsuario = dados.nomeUsuario();
        }
        if (dados.biografia() != null) {
            this.biografia = dados.biografia();
        }
        if (dados.miniBiografia() != null) {
            this.miniBiografia = dados.miniBiografia();
        }
        return this;
    }

    public void alterarSenha(String senhaCriptografa) {
        this.senha = senhaCriptografa;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void adicionarPerfil(Perfil perfil) {
        this.perfis.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        this.perfis.remove(perfil);
    }

    public void reativar() {
        this.ativo = true;
    }

    private void aprovarUsuario(){
        this.verificado = true;
        this.ativo = true;
        this.token = null;
        this.expiracaoToken = null;
    }

    public void gerarSecret(String secret) {
        this.secret = secret;
    }

    public MetodosA2F getMetodosA2F() {
        return this.metodosA2F;
    }

    public void ativarA2f(MetodosA2F metodo) {
        this.metodosA2F = metodo;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public void setEmailCodeExpiracao(LocalDateTime emailCodeExpiracao) {
        this.emailCodeExpiracao = emailCodeExpiracao;
    }

    public void setTentativasA2F(int tentativasA2F) {
        this.tentativasA2F = tentativasA2F;
    }
}

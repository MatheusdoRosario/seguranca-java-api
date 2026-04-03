package br.com.forum_hub.domain.usuario.service;

import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.perfil.PerfilRepository;
import br.com.forum_hub.domain.usuario.*;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.security.HierarquiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HierarquiaService hierarquiaService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    public DadosListagemUsuario listarPorNomeUsuario(String nomeUsuario) {
        var usuario = usuarioRepository.findByNomeUsuarioAndVerificadoTrueAndAtivoTrue(nomeUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado!"));
        return new DadosListagemUsuario(usuario);
    }

    @Transactional
    public Usuario editarPerfil(DadosAtualizacaoUsuario dados, Usuario logado) {
        return logado.alterarDados(dados);
    }

    @Transactional
    public void alterarSenha(DadosAtualizacaoSenha dados, Usuario logado) {
        if (!passwordEncoder.matches(dados.senhaAtual(), logado.getPassword())) {
            throw new RegraDeNegocioException("Senha atual inválida!");
        }

        if (!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");
        }

        var senhaCriptografa = passwordEncoder.encode(dados.novaSenha());
        logado.alterarSenha(senhaCriptografa);
    }

    @Transactional
    public void desativarUsuario(Long id, Usuario logado) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        if (hierarquiaService.usuarioNaoTemPermissoes(logado, usuario, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Você não tem permissão realizar essa operação!");
        }
        usuario.desativar();
    }

    @Transactional
    public Usuario adicionarPerfil(Long id, DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());
        usuario.adicionarPerfil(perfil);
        return usuario;
    }

    @Transactional
    public Usuario removerPerfil(Long id, DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());
        usuario.removerPerfil(perfil);
        return usuario;
    }

    @Transactional
    public void reativarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.reativar();
    }
}

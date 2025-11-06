package com.example.demo.services;

import com.example.demo.models.Usuario;
import com.example.demo.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsuarioService extends AbstractService<Usuario, Long> {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Cria um usuário. Valida nome e email básicos e impede email duplicado.
     */
    @Override
    public Usuario criar(Usuario usuario) {
        log.info("Tentando criar usuário: {}", usuario);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser null.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            log.warn("Nome inválido ao criar usuário: {}", usuario);
            throw new IllegalArgumentException("Nome deve ser informado.");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank() || !usuario.getEmail().contains("@")) {
            log.warn("Email inválido ao criar usuário: {}", usuario);
            throw new IllegalArgumentException("Email inválido.");
        }

        // impede email duplicado
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            log.warn("Tentativa de criar usuário com email já existente: {}", usuario.getEmail());
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso: {}", salvo.getId());
        return salvo;
    }

    /**
     * Busca usuário por id. Lança ResourceNotFoundException se não existir.
     */
    @Override
    public Usuario buscarPorId(Long id) {
        log.debug("Buscando usuário por id: {}", id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    /**
     * Retorna todos os usuários.
     */
    @Override
    public List<Usuario> buscarTodos() {
        log.debug("Buscando todos os usuários");
        return usuarioRepository.findAll();
    }

    /**
     * Atualiza nome e email do usuário. Valida existência e email (se for alterado, checa duplicidade).
     */
    @Override
    public Usuario atualizar(Long id, Usuario usuario) {
        log.info("Atualizando usuário id: {}", id);
        Usuario existente = buscarPorId(id);

        if (usuario == null) {
            throw new IllegalArgumentException("Dados para atualização não podem ser nulos.");
        }

        if (usuario.getNome() != null && !usuario.getNome().isBlank()) {
            existente.setNome(usuario.getNome());
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            if (!usuario.getEmail().contains("@")) {
                throw new IllegalArgumentException("Email inválido.");
            }
            // se trocar o email, verificar duplicidade
            if (!usuario.getEmail().equalsIgnoreCase(existente.getEmail())) {
                usuarioRepository.findByEmail(usuario.getEmail())
                        .ifPresent(u -> { throw new IllegalArgumentException("Email já cadastrado."); });
                existente.setEmail(usuario.getEmail());
            }
        }

        Usuario atualizado = usuarioRepository.save(existente);
        log.info("Usuário atualizado com sucesso: {}", atualizado.getId());
        return atualizado;
    }

    /**
     * Deleta usuário por id (lança ResourceNotFoundException se não existir).
     */
    @Override
    public void deletar(Long id) {
        log.warn("Deletando usuário id: {}", id);
        // valida existência
        buscarPorId(id);
        usuarioRepository.deleteById(id);
        log.info("Usuário deletado com sucesso: {}", id);
    }

    /**
     * Busca usuário por email (retorna Optional para uso flexível em serviços/testes).
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Busca usuários cujo nome contenha a string (útil em endpoints de busca simples).
     */
    public List<Usuario> buscarPorNomeLike(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome == null ? "" : nome);
    }
}

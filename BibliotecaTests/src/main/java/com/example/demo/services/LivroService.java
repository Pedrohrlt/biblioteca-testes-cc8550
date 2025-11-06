package com.example.demo.services;

import com.example.demo.models.Livro;
import com.example.demo.repositories.LivroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LivroService extends AbstractService<Livro, Long> {

    public static org.slf4j.Logger getLog() {
        return log;
    }

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @Override
    public Livro criar(Livro livro) {
        log.info("Tentando criar livro: {}", livro.getTitulo());

        if (livro.getTitulo() == null || livro.getTitulo().isBlank() ||
            livro.getAutor() == null || livro.getAutor().isBlank() ||
            livro.getPaginas() <= 0) {
            log.warn("Falha ao criar livro. Dados inválidos: {}", livro);
            throw new IllegalArgumentException("Título, autor e páginas devem ser válidos.");
        }

        Livro salvo = livroRepository.save(livro);
        log.info("Livro criado com sucesso: {}", salvo.getId());
        return salvo;
    }

    @Override
    public Livro buscarPorId(Long id) {
        log.debug("Buscando livro por id: {}", id);
        return checkNotNull(livroRepository.findById(id), "Livro não encontrado");
    }

    @Override
    public List<Livro> buscarTodos() {
        log.debug("Buscando todos os livros");
        return livroRepository.findAll();
    }

    @Override
    public Livro atualizar(Long id, Livro livro) {
        log.info("Atualizando livro id: {}", id);
        Livro existente = buscarPorId(id);
        existente.setTitulo(livro.getTitulo());
        existente.setAutor(livro.getAutor());
        existente.setPaginas(livro.getPaginas());
        Livro atualizado = livroRepository.save(existente);
        log.info("Livro atualizado com sucesso: {}", atualizado.getId());
        return atualizado;
    }

    @Override
    public void deletar(Long id) {
        log.warn("Deletando livro id: {}", id);
        livroRepository.deleteById(id);
        log.info("Livro deletado com sucesso: {}", id);
    }
}

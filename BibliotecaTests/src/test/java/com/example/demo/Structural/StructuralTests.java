package com.example.demo.Structural;

import com.example.demo.models.Emprestimo;
import com.example.demo.models.Livro;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.EmprestimoRepository;
import com.example.demo.repositories.LivroRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.services.EmprestimoService;
import com.example.demo.services.ResourceNotFoundException;
import com.example.demo.services.LivroService;
import com.example.demo.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes Estruturais (Caixa-Branca) - foco em cobertura de caminhos, branches e validações.
 *
 * Observações:
 * - Não carrega Spring context (rápido e isolado).
 * - Usa Mockito para mockar repositórios e forçar caminhos alternativos.
 * - Objetivo prático: exercitar validações, fluxos felizes e erros (throw).
 *
 * Esses testes são exemplos para alcançar cobertura de branches nos serviços centrais:
 * LivroService, UsuarioService e EmprestimoService.
 */
@ExtendWith(MockitoExtension.class)
public class StructuralTests {

    // ----------- LivroService tests -----------
    @Mock
    private LivroRepository livroRepository;

    private LivroService livroService;

    // ----------- UsuarioService tests -----------
    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    // ----------- EmprestimoService tests -----------
    @Mock
    private EmprestimoRepository emprestimoRepository;

    private EmprestimoService emprestimoService;

    @Captor
    private ArgumentCaptor<Livro> livroCaptor;

    @BeforeEach
    void setup() {
        livroService = new LivroService(livroRepository);
        usuarioService = new UsuarioService(usuarioRepository);
        emprestimoService = new EmprestimoService(emprestimoRepository, usuarioRepository, livroRepository);
    }

    // -------- LivroService - caminhos principais e validações --------

    @Test
    void criarLivro_valido_salvaERetorna() {
        Livro l = new Livro();
        l.setTitulo("T");
        l.setAutor("A");
        l.setPaginas(10);

        Livro saved = new Livro();
        saved.setId(1L);
        saved.setTitulo("T");
        saved.setAutor("A");
        saved.setPaginas(10);

        when(livroRepository.save(any(Livro.class))).thenReturn(saved);

        Livro result = livroService.criar(l);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(livroRepository).save(livroCaptor.capture());
        assertThat(livroCaptor.getValue().getTitulo()).isEqualTo("T");
    }

    @Test
    void criarLivro_invalido_lancaIllegalArgumentException() {
        Livro l = new Livro();
        l.setTitulo("");
        l.setAutor("");
        l.setPaginas(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> livroService.criar(l));
        assertThat(ex.getMessage()).containsIgnoringCase("Título, autor e páginas");
        verifyNoInteractions(livroRepository);
    }

    @Test
    void buscarPorId_existente_retornaLivro() {
        Livro l = new Livro();
        l.setId(5L);
        l.setTitulo("X");

        when(livroRepository.findById(5L)).thenReturn(Optional.of(l));

        Livro r = livroService.buscarPorId(5L);
        assertThat(r.getId()).isEqualTo(5L);
    }

    @Test
    void atualizarLivro_alteraCamposESalva() {
        Livro existing = new Livro();
        existing.setId(10L);
        existing.setTitulo("Old");
        existing.setAutor("A");
        existing.setPaginas(100);

        when(livroRepository.findById(10L)).thenReturn(Optional.of(existing));

        Livro payload = new Livro();
        payload.setTitulo("New");
        payload.setAutor("A2");
        payload.setPaginas(150);

        Livro updated = new Livro();
        updated.setId(10L);
        updated.setTitulo("New");
        updated.setAutor("A2");
        updated.setPaginas(150);

        when(livroRepository.save(any(Livro.class))).thenReturn(updated);

        Livro res = livroService.atualizar(10L, payload);

        assertThat(res.getTitulo()).isEqualTo("New");
        assertThat(res.getAutor()).isEqualTo("A2");
        assertThat(res.getPaginas()).isEqualTo(150);
        verify(livroRepository).save(any(Livro.class));
    }


    // -------- UsuarioService - caminhos críticos e duplicidade de email --------

    @Test
    void criarUsuario_valido_salva() {
        Usuario u = new Usuario();
        u.setNome("Pedro");
        u.setEmail("p@x.com");

        when(usuarioRepository.findByEmail("p@x.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        Usuario res = usuarioService.criar(u);
        assertThat(res.getId()).isEqualTo(1L);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void criarUsuario_emailDuplicado_lancaIllegalArgumentException() {
        Usuario u = new Usuario();
        u.setNome("X");
        u.setEmail("dup@ex.com");

        when(usuarioRepository.findByEmail("dup@ex.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.criar(u));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarUsuario_trocaEmail_VerificaDuplicidade() {
        Usuario existente = new Usuario();
        existente.setId(5L);
        existente.setNome("Old");
        existente.setEmail("old@a.com");

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("new@b.com")).thenReturn(Optional.of(new Usuario()));

        Usuario payload = new Usuario();
        payload.setEmail("new@b.com");

        // se já existe com esse email, deve lançar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(5L, payload));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorEmail_retornaOptional() {
        Usuario u = new Usuario();
        u.setEmail("z@x.com");
        when(usuarioRepository.findByEmail("z@x.com")).thenReturn(Optional.of(u));

        Optional<Usuario> r = usuarioService.buscarPorEmail("z@x.com");
        assertThat(r).isPresent();
    }

    // -------- EmprestimoService - caminhos complexos e branches --------

    @Test
    void criarEmprestimo_valido_salva() {
        Usuario u = new Usuario();
        u.setId(1L);

        Livro l = new Livro();
        l.setId(2L);

        Emprestimo e = new Emprestimo();
        e.setUsuario(u);
        e.setLivro(l);
        e.setDataEmprestimo(LocalDate.now());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(livroRepository.findById(2L)).thenReturn(Optional.of(l));
        when(emprestimoRepository.findByUsuarioAndDataDevolucaoIsNull(u)).thenReturn(new ArrayList<>());
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(inv -> {
            Emprestimo saved = inv.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        Emprestimo res = emprestimoService.criar(e);
        assertThat(res.getId()).isEqualTo(7L);
        verify(emprestimoRepository).save(any(Emprestimo.class));
    }


    @Test
    void criarEmprestimo_limiteExcedido_lanca() {
        Usuario u = new Usuario();
        u.setId(2L);
        Livro l = new Livro();
        l.setId(3L);
        Emprestimo e = new Emprestimo();
        e.setUsuario(u);
        e.setLivro(l);

        // simula 5 empréstimos abertos
        List<Emprestimo> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) list.add(new Emprestimo());

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(livroRepository.findById(3L)).thenReturn(Optional.of(l));
        when(emprestimoRepository.findByUsuarioAndDataDevolucaoIsNull(u)).thenReturn(list);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> emprestimoService.criar(e));
        assertThat(ex.getMessage()).containsIgnoringCase("5 empréstimos");
    }

    @Test
    void atualizarEmprestimo_dataDevolucaoAntes_lanca() {
        Emprestimo existente = new Emprestimo();
        existente.setId(20L);
        existente.setDataEmprestimo(LocalDate.of(2025, 1, 10));

        when(emprestimoRepository.findById(20L)).thenReturn(Optional.of(existente));

        Emprestimo payload = new Emprestimo();
        payload.setDataDevolucao(LocalDate.of(2024, 12, 1)); // antes da dataEmprestimo

        assertThrows(IllegalArgumentException.class, () -> emprestimoService.atualizar(20L, payload));
    }
}

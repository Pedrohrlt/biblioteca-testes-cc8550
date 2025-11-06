package com.example.demo.services;

import com.example.demo.models.Livro;
import com.example.demo.repositories.LivroRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    private Livro sampleLivro;

    @BeforeEach
    void setUp() {
        sampleLivro = new Livro();
        sampleLivro.setId(1L);
        sampleLivro.setTitulo("Dom Quixote");
        sampleLivro.setAutor("Miguel de Cervantes");
        sampleLivro.setPaginas(500);
    }

    // ---------- Helpers ----------
    static Stream<Arguments> validLivros() {
        return Stream.of(
                Arguments.of(new Livro(null, "Livro A", "Autor A", 100)),
                Arguments.of(new Livro(null, "Livro B", "Autor B", 200))
        );
    }

    static Stream<Arguments> invalidLivros() {
        return Stream.of(
                Arguments.of(new Livro(null, null, "Autor", 100)),
                Arguments.of(new Livro(null, "", "Autor", 100)),
                Arguments.of(new Livro(null, "Titulo", null, 100)),
                Arguments.of(new Livro(null, "Titulo", "", 100)),
                Arguments.of(new Livro(null, "Titulo", "Autor", 0)),
                Arguments.of(new Livro(null, "Titulo", "Autor", -5))
        );
    }

    // ---------- Testes de criação ----------
    @Test
    void testCriarLivro_valido() {
        when(livroRepository.save(any())).thenReturn(sampleLivro);

        Livro criado = livroService.criar(sampleLivro);

        assertNotNull(criado.getId());
        assertEquals("Dom Quixote", criado.getTitulo());
        verify(livroRepository).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidLivros")
    void testCriarLivro_invalido_deveLancar(Livro invalido) {
        assertThrows(IllegalArgumentException.class, () -> livroService.criar(invalido));
    }

    // ---------- Testes buscarPorId ----------
    @Test
    void testBuscarPorId_existente() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(sampleLivro));

        Livro l = livroService.buscarPorId(1L);
        assertEquals(1L, l.getId());
    }

    @Test
    void testBuscarPorId_naoExistente() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> livroService.buscarPorId(99L));
    }

    // ---------- Testes buscarTodos ----------
    @Test
    void testBuscarTodos() {
        List<Livro> lista = List.of(sampleLivro, new Livro(null, "Outro Livro", "Autor B", 200));
        when(livroRepository.findAll()).thenReturn(lista);

        List<Livro> resultado = livroService.buscarTodos();
        assertEquals(2, resultado.size());
    }

    // ---------- Testes atualização ----------
    @Test
    void testAtualizarLivro_nomeAutorPaginas() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(sampleLivro));
        when(livroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Livro dados = new Livro(null, "Novo Titulo", "Novo Autor", 600);
        Livro atualizado = livroService.atualizar(1L, dados);

        assertEquals("Novo Titulo", atualizado.getTitulo());
        assertEquals("Novo Autor", atualizado.getAutor());
        assertEquals(600, atualizado.getPaginas());
    }

    @Test
    void testAtualizarLivro_naoExistente() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());
        Livro dados = new Livro(null, "Titulo", "Autor", 100);
        assertThrows(RuntimeException.class, () -> livroService.atualizar(99L, dados));
    }

    // ---------- Testes deleção ----------
    @Test
    void testDeletar_existente() {
        doNothing().when(livroRepository).deleteById(1L);

        livroService.deletar(1L);

        verify(livroRepository).deleteById(1L);
    }

    // ---------- Testes limites ----------
    @Test
    void testCriarLivro_tituloAutorExtremos() {
        String tituloExtremo = "X".repeat(1000);
        String autorExtremo = "Y".repeat(500);

        Livro l = new Livro(null, tituloExtremo, autorExtremo, 100);
        when(livroRepository.save(any())).thenAnswer(inv -> {
            Livro x = inv.getArgument(0);
            x.setId(2L);
            return x;
        });

        Livro criado = livroService.criar(l);
        assertEquals(tituloExtremo, criado.getTitulo());
        assertEquals(autorExtremo, criado.getAutor());
        assertNotNull(criado.getId());
    }

}

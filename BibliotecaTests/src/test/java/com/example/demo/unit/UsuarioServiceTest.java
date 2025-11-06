package com.example.demo.services;

import com.example.demo.models.Usuario;
import com.example.demo.repositories.UsuarioRepository;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario sampleUsuario;

    @BeforeEach
    void setUp() {
        sampleUsuario = new Usuario("João", "joao@email.com");
        sampleUsuario.setId(1L);
    }

    // ---------- Helpers para parametrização ----------
    static Stream<Arguments> validUsuarios() {
        return Stream.of(
                Arguments.of(new Usuario("Ana", "ana@email.com")),
                Arguments.of(new Usuario("Carlos", "carlos@email.com")),
                Arguments.of(new Usuario("José", "jose@email.com"))
        );
    }

    static Stream<Arguments> invalidUsuarios() {
        return Stream.of(
                Arguments.of(new Usuario(null, "email@email.com")),
                Arguments.of(new Usuario("", "email@email.com")),
                Arguments.of(new Usuario("Nome", null)),
                Arguments.of(new Usuario("Nome", "")),
                Arguments.of(new Usuario("Nome", "invalid-email"))
        );
    }

    // ---------- Testes de criação ----------
    @Test
    void testCriarUsuario_valido() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenReturn(sampleUsuario);

        Usuario criado = usuarioService.criar(sampleUsuario);

        assertNotNull(criado.getId());
        assertEquals("João", criado.getNome());
        assertEquals("joao@email.com", criado.getEmail());
        verify(usuarioRepository).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidUsuarios")
    void testCriarUsuario_invalido_deveLancar(Usuario invalido) {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.criar(invalido));
    }

    @Test
    void testCriarUsuario_emailDuplicado() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUsuario));

        Usuario novo = new Usuario("João 2", "joao@email.com");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.criar(novo));
    }

    @Test
    void testCriarUsuario_null() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.criar(null));
    }

    // ---------- Testes de busca por ID ----------
    @Test
    void testBuscarPorId_existente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));

        Usuario u = usuarioService.buscarPorId(1L);
        assertEquals(1L, u.getId());
    }

    @Test
    void testBuscarPorId_naoExistente() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarPorId(2L));
    }

    // ---------- Testes de buscarTodos ----------
    @Test
    void testBuscarTodos() {
        List<Usuario> lista = List.of(sampleUsuario, new Usuario("Maria", "maria@email.com"));
        when(usuarioRepository.findAll()).thenReturn(lista);

        List<Usuario> resultado = usuarioService.buscarTodos();
        assertEquals(2, resultado.size());
    }

    // ---------- Testes de atualização ----------
    @Test
    void testAtualizar_nomeValido() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));
        when(usuarioRepository.save(any())).thenReturn(sampleUsuario);

        Usuario dados = new Usuario("João Atualizado", null);
        Usuario atualizado = usuarioService.atualizar(1L, dados);

        assertEquals("João Atualizado", atualizado.getNome());
        assertEquals("joao@email.com", atualizado.getEmail());
    }

    @Test
    void testAtualizar_emailValido() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenReturn(sampleUsuario);

        Usuario dados = new Usuario(null, "novo@email.com");
        Usuario atualizado = usuarioService.atualizar(1L, dados);

        assertEquals("novo@email.com", atualizado.getEmail());
    }

    @Test
    void testAtualizar_emailDuplicado() {
        Usuario outro = new Usuario("Outro", "outro@email.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));
        when(usuarioRepository.findByEmail("outro@email.com")).thenReturn(Optional.of(outro));

        Usuario dados = new Usuario(null, "outro@email.com");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(1L, dados));
    }

    @Test
    void testAtualizar_emailInvalido() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));

        Usuario dados = new Usuario(null, "invalid-email");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(1L, dados));
    }

    @Test
    void testAtualizar_usuarioNaoExistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        Usuario dados = new Usuario("Nome", "email@email.com");
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.atualizar(99L, dados));
    }

    @Test
    void testAtualizar_dadosNulos() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(1L, null));
    }

    // ---------- Testes de deleção ----------
    @Test
    void testDeletar_existente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(sampleUsuario));
        doNothing().when(usuarioRepository).deleteById(1L);

        assertDoesNotThrow(() -> usuarioService.deletar(1L));
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void testDeletar_naoExistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.deletar(99L));
    }

    // ---------- Testes buscarPorEmail ----------
    @Test
    void testBuscarPorEmail_existente() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUsuario));
        Optional<Usuario> u = usuarioService.buscarPorEmail("joao@email.com");
        assertTrue(u.isPresent());
        assertEquals("João", u.get().getNome());
    }

    @Test
    void testBuscarPorEmail_naoExistente() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());
        Optional<Usuario> u = usuarioService.buscarPorEmail("naoexiste@email.com");
        assertFalse(u.isPresent());
    }

    // ---------- Testes buscarPorNomeLike ----------
    @Test
    void testBuscarPorNomeLike_comString() {
        List<Usuario> lista = List.of(sampleUsuario);
        when(usuarioRepository.findByNomeContainingIgnoreCase("Jo")).thenReturn(lista);

        List<Usuario> resultado = usuarioService.buscarPorNomeLike("Jo");
        assertEquals(1, resultado.size());
    }

    @Test
    void testBuscarPorNomeLike_null() {
        List<Usuario> lista = List.of(sampleUsuario);
        when(usuarioRepository.findByNomeContainingIgnoreCase("")).thenReturn(lista);

        List<Usuario> resultado = usuarioService.buscarPorNomeLike(null);
        assertEquals(1, resultado.size());
    }
}

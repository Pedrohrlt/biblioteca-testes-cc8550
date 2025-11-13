package com.example.demo.unit;

import com.example.demo.models.Emprestimo;
import com.example.demo.models.Livro;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.EmprestimoRepository;
import com.example.demo.repositories.LivroRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.services.AbstractService;
import com.example.demo.services.EmprestimoService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Usuario usuario;
    private Livro livro;
    private Emprestimo emprestimo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("João", "joao@email.com");
        usuario.setId(1L);

        livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Livro Teste");
        livro.setAutor("Autor");
        livro.setPaginas(100);

        emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataDevolucao(null);
    }

    // ---------- Testes de criação ---------- (já existentes)
    // ... [seu código anterior aqui] ...

    // ---------- Testes Específicos por Tipo ----------

    @Test
    void testCriarEmprestimo_comVariosEmprestimosSimulados() {
        // simula 3 empréstimos abertos
        List<Emprestimo> abertos = Arrays.asList(new Emprestimo(), new Emprestimo(), new Emprestimo());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.findByUsuarioAndDataDevolucaoIsNull(usuario)).thenReturn(abertos);
        when(emprestimoRepository.save(any())).thenReturn(emprestimo);

        Emprestimo criado = emprestimoService.criar(emprestimo);
        assertNotNull(criado);
    }

    @Test
    void testCriarEmprestimo_repositorioFalha() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.findByUsuarioAndDataDevolucaoIsNull(usuario)).thenReturn(new ArrayList<>());
        when(emprestimoRepository.save(any())).thenThrow(new RuntimeException("Erro BD"));

        RuntimeException e = assertThrows(RuntimeException.class, () -> emprestimoService.criar(emprestimo));
        assertEquals("Erro BD", e.getMessage());
    }

    // ---------- Testes de Orientação a Objetos ----------
    static class TestAbstractService extends AbstractService<String, Long> {
        public String testCheckNotNull(Optional<String> value, String msg) {
            return checkNotNull(value, msg);
        }

        @Override
        public String criar(String entity) {
            return "";
        }

        @Override
        public String buscarPorId(Long aLong) {
            return "";
        }

        @Override
        public List<String> buscarTodos() {
            return List.of();
        }

        @Override
        public String atualizar(Long aLong, String entity) {
            return "";
        }

        @Override
        public void deletar(Long aLong) {

        }
    }

    @Test
    void testAbstractService_checkNotNull_valorPresente() {
        TestAbstractService service = new TestAbstractService();
        String result = service.testCheckNotNull(Optional.of("ok"), "Erro");
        assertEquals("ok", result);
    }

    @Test
    void testAbstractService_checkNotNull_valorAusente() {
        TestAbstractService service = new TestAbstractService();
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> service.testCheckNotNull(Optional.empty(), "Valor nulo"));
        assertEquals("Valor nulo", e.getMessage());
    }

    @Test
    void testPolimorfismo_abstractService() {
        AbstractService<String, Long> service = new TestAbstractService();
        String result = service.checkNotNull(Optional.of("polimorfismo"), "Erro");
        assertEquals("polimorfismo", result);
    }

    // ---------- Teste de Encapsulamento/GetterSetter ----------
    @Test
    void testUsuarioEncapsulamento() {
        Usuario u = new Usuario();
        u.setId(10L);
        u.setNome("Teste");
        u.setEmail("teste@email.com");

        assertEquals(10L, u.getId());
        assertEquals("Teste", u.getNome());
        assertEquals("teste@email.com", u.getEmail());
    }

}

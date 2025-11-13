package com.example.demo.integration;

import com.example.demo.models.Livro;
import com.example.demo.repositories.LivroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Livro livro1;
    private Livro livro2;
    private Livro livro3;

    @BeforeEach
    void setUp() {
        // limpa tabela para garantir estado previsível antes de cada teste
        livroRepository.deleteAll();

        livro1 = new Livro();
        livro1.setTitulo("A Programação em Java");
        livro1.setAutor("Autor X");
        livro1.setPaginas(150);

        livro2 = new Livro();
        livro2.setTitulo("Aprendendo Spring Boot");
        livro2.setAutor("Autor X");
        livro2.setPaginas(220);

        livro3 = new Livro();
        livro3.setTitulo("Design Patterns");
        livro3.setAutor("Autor Y");
        livro3.setPaginas(300);

        // salva diretamente no repositório para uso nos testes
        livroRepository.save(livro1);
        livroRepository.save(livro2);
        livroRepository.save(livro3);
    }

    @Test
    public void criarLivro_success_returnsCreated() throws Exception {
        Livro novo = new Livro();
        novo.setTitulo("Novo Livro");
        novo.setAutor("Autor Z");
        novo.setPaginas(120);

        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Novo Livro"))
                .andExpect(jsonPath("$.autor").value("Autor Z"));
    }

    @Test
    public void buscarPorId_found_returnsOk() throws Exception {
        Long id = livro1.getId();
        mockMvc.perform(get("/api/livros/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is(livro1.getTitulo())))
                .andExpect(jsonPath("$.autor", is(livro1.getAutor())));
    }


    @Test
    public void buscarTodos_returnsList() throws Exception {
        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    public void atualizarLivro_success_returnsOk() throws Exception {
        Long id = livro2.getId();
        Livro atualizado = new Livro();
        atualizado.setTitulo("Aprendizado Spring Boot Avançado");
        atualizado.setAutor(livro2.getAutor());
        atualizado.setPaginas(250);

        mockMvc.perform(put("/api/livros/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Aprendizado Spring Boot Avançado")))
                .andExpect(jsonPath("$.paginas", is(250)));
    }

    @Test
    public void repository_findByAutor_orderAsc() {
        List<Livro> encontrados = livroRepository.findByAutorOrderByTituloAsc("Autor X");
        assertThat(encontrados).isNotEmpty();

        // compara com Collator pt-BR para lidar com acentos/artigos corretamente
        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        for (int i = 0; i < encontrados.size() - 1; i++) {
            String t1 = encontrados.get(i).getTitulo();
            String t2 = encontrados.get(i + 1).getTitulo();
            int cmp = collator.compare(t1, t2);
            assertThat(cmp)
                    .withFailMessage("Lista não está em ordem ascendente: '%s' > '%s'", t1, t2)
                    .isLessThanOrEqualTo(0);
        }
    }

    @Test
    public void repository_findByAutor_orderDesc() {
        List<Livro> encontrados = livroRepository.findByAutorOrderByTituloDesc("Autor X");
        assertThat(encontrados).isNotEmpty();

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        for (int i = 0; i < encontrados.size() - 1; i++) {
            String t1 = encontrados.get(i).getTitulo();
            String t2 = encontrados.get(i + 1).getTitulo();
            int cmp = collator.compare(t1, t2);
            assertThat(cmp)
                    .withFailMessage("Lista não está em ordem descendente: '%s' < '%s'", t1, t2)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    public void criarVariosLivros_and_buscarTodos_countMatches() throws Exception {
        // insere dois novos via endpoint
        Livro l1 = new Livro();
        l1.setTitulo("Livro Extra 1");
        l1.setAutor("Autor Extra");
        l1.setPaginas(100);

        Livro l2 = new Livro();
        l2.setTitulo("Livro Extra 2");
        l2.setAutor("Autor Extra");
        l2.setPaginas(150);

        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(l1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(l2)))
                .andExpect(status().isCreated());

        // agora buscar todos deve retornar 5 (3 do setup + 2 novos)
        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(5)));
    }
}

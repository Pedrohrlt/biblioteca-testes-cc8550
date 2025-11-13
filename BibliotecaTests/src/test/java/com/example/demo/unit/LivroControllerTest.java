package com.example.demo.unit;

import com.example.demo.controllers.LivroController;
import com.example.demo.models.Livro;
import com.example.demo.services.LivroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivroController.class)
public class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LivroService livroService;

    @Autowired
    private ObjectMapper objectMapper;

    private Livro livro1;
    private Livro livro2;

    @BeforeEach
    public void setup() {
        livro1 = new Livro();
        livro1.setId(1L);
        livro1.setTitulo("Java Básico");
        livro1.setAutor("Autor A");
        livro1.setPaginas(200);

        livro2 = new Livro();
        livro2.setId(2L);
        livro2.setTitulo("Spring Boot");
        livro2.setAutor("Autor B");
        livro2.setPaginas(300);
    }

    @Test
    public void testCriarLivro() throws Exception {
        Mockito.when(livroService.criar(any(Livro.class))).thenReturn(livro1);

        mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(livro1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Java Básico"));
    }

    @Test
    public void testBuscarPorId() throws Exception {
        Mockito.when(livroService.buscarPorId(1L)).thenReturn(livro1);

        mockMvc.perform(get("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("Autor A"));
    }

    @Test
    public void testBuscarTodos() throws Exception {
        List<Livro> livros = Arrays.asList(livro1, livro2);
        Mockito.when(livroService.buscarTodos()).thenReturn(livros);

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testAtualizarLivro() throws Exception {
        Livro atualizado = new Livro();
        atualizado.setId(1L);
        atualizado.setTitulo("Java Avançado");
        atualizado.setAutor("Autor A");
        atualizado.setPaginas(250);

        Mockito.when(livroService.atualizar(eq(1L), any(Livro.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/livros/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Java Avançado"));
    }

    @Test
    public void testDeletarLivro() throws Exception {
        Mockito.doNothing().when(livroService).deletar(1L);

        mockMvc.perform(delete("/api/livros/1"))
                .andExpect(status().isNoContent());
    }
}

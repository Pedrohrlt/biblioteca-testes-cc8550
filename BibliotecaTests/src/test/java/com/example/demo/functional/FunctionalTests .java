package com.example.demo.functional;

import com.example.demo.models.Livro;
import com.example.demo.repositories.LivroRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes Funcionais (Caixa-Preta) - adaptados para o comportamento atual da aplicação:
 * NÃO será necessário alterar o código da aplicação (não criamos GlobalExceptionHandler).
 * Portanto, quando o serviço lança RuntimeException para "não encontrado" ou validação,
 * os testes esperam respostas 5xx (comportamento atual).
 *
 * Execução: SpringBootTest (RANDOM_PORT) + TestRestTemplate (chamadas HTTP reais).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FunctionalTests {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String base = "/api/livros";

    @BeforeEach
    public void beforeEach() {
        // limpa o estado antes de cada cenário funcional para garantir independência
        livroRepository.deleteAll();
    }

    // 1) Criar livro válido -> 201 Created + corpo com id e campos corretos
    @Test
    public void criarLivro_valido_retornaCreatedEBody() throws Exception {
        Livro novo = new Livro();
        novo.setTitulo("Funcional: Java Test");
        novo.setAutor("Autor Func");
        novo.setPaginas(123);

        ResponseEntity<String> r = rest.postForEntity(base, novo, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode node = objectMapper.readTree(r.getBody());
        assertThat(node.get("id").isNumber()).isTrue();
        assertThat(node.get("titulo").asText()).isEqualTo("Funcional: Java Test");
    }

    @Test
    public void buscarPorId_existente_retornaOk() throws Exception {
        // cria via controller para garantir que o formato do recurso seja o mesmo retornado pelo GET
        Livro novo = new Livro();
        novo.setTitulo("Livro B");
        novo.setAutor("Autor B");
        novo.setPaginas(200);

        ResponseEntity<String> createRes = rest.postForEntity(base, novo, String.class);
        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // extrai id da resposta
        JsonNode createdNode = objectMapper.readTree(createRes.getBody());
        Long id = createdNode.get("id").asLong();

        // busca via GET e valida campos
        ResponseEntity<String> getRes = rest.getForEntity(base + "/" + id, String.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode node = objectMapper.readTree(getRes.getBody());
        // garante que campo existe e não é nulo
        assertThat(node.hasNonNull("titulo")).withFailMessage("Resposta GET não contém 'titulo': %s", getRes.getBody()).isTrue();
        assertThat(node.get("titulo").asText()).isEqualTo("Livro B");
        assertThat(node.get("autor").asText()).isEqualTo("Autor B");
    }


    @Test
    public void buscarPorId_inexistente_retornaServerError() throws Exception {
        ResponseEntity<String> r = rest.getForEntity(base + "/999999", String.class);

        // garante que há erro do servidor (5xx)
        assertThat(r.getStatusCode().is5xxServerError()).isTrue();

        // se o corpo for o JSON de erro padrão do Spring, confirmar que "status" == 500
        if (r.hasBody() && r.getBody() != null && !r.getBody().isBlank()) {
            JsonNode node = objectMapper.readTree(r.getBody());
            // usa path para evitar NullPointer se campo ausente; path().asInt() devolve 0 por padrão
            int statusInBody = node.path("status").asInt(0);
            assertThat(statusInBody).isEqualTo(500);
        }
    }


    // 5) Listar todos -> 200 OK e quantidade condizente
    @Test
    public void listarTodos_retornaListComQuantidade() throws Exception {
        livroRepository.saveAll(List.of(
                new Livro(null, "L1", "A", 100),
                new Livro(null, "L2", "B", 110)
        ));

        ResponseEntity<String> r = rest.getForEntity(base, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode node = objectMapper.readTree(r.getBody());
        assertThat(node.isArray()).isTrue();
        assertThat(node.size()).isEqualTo(2);
    }

    // 6) Atualizar livro válido -> 200 OK com campos atualizados
    @Test
    public void atualizarLivro_valido_retornaOk() throws Exception {
        Livro salvo = livroRepository.save(new Livro(null, "Antes", "Autor", 90));

        Livro payload = new Livro();
        payload.setTitulo("Depois");
        payload.setAutor("Autor");
        payload.setPaginas(95);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Livro> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> r = rest.exchange(base + "/" + salvo.getId(), HttpMethod.PUT, entity, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode node = objectMapper.readTree(r.getBody());
        assertThat(node.get("titulo").asText()).isEqualTo("Depois");

        // verifica persistência
        Livro atual = livroRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atual.getTitulo()).isEqualTo("Depois");
    }


    // 8) Deletar livro -> 204 No Content e remoção do registro
    @Test
    public void deletarLivro_retornaNoContent_e_removido() throws Exception {
        Livro salvo = livroRepository.save(new Livro(null, "Para Deletar", "Autor D", 55));

        ResponseEntity<Void> r = rest.exchange(base + "/" + salvo.getId(), HttpMethod.DELETE, null, Void.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(livroRepository.findById(salvo.getId())).isEmpty();
    }

    // 10) Cenário de aceitação composto: criar, atualizar e buscar -> fluxo completo validado
    @Test
    public void fluxoCriarAtualizarBuscar_validaAceitacao() throws Exception {
        Livro novo = new Livro();
        novo.setTitulo("Aceite");
        novo.setAutor("QA");
        novo.setPaginas(77);

        // criar
        ResponseEntity<String> createRes = rest.postForEntity(base, novo, String.class);
        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = objectMapper.readTree(createRes.getBody()).get("id").asLong();

        // atualizar
        Livro upd = new Livro();
        upd.setTitulo("Aceite v2");
        upd.setAutor("QA");
        upd.setPaginas(88);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Livro> updateEntity = new HttpEntity<>(upd, headers);
        ResponseEntity<String> updateRes = rest.exchange(base + "/" + id, HttpMethod.PUT, updateEntity, String.class);

        // dependendo do comportamento interno, atualização pode falhar (5xx) — esperamos OK no caso válido
        assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // buscar
        ResponseEntity<String> getRes = rest.getForEntity(base + "/" + id, String.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode node = objectMapper.readTree(getRes.getBody());
        assertThat(node.get("titulo").asText()).isEqualTo("Aceite v2");
        assertThat(node.get("paginas").asInt()).isEqualTo(88);
    }
}

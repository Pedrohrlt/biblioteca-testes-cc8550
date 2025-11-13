package com.example.demo.controllers;

import com.example.demo.models.Livro;
import com.example.demo.services.LivroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @PostMapping
    public ResponseEntity<Livro> criarLivro(@RequestBody Livro livro) {
        return new ResponseEntity<>(livroService.criar(livro), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(livroService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<Livro>> buscarTodos() {
        return ResponseEntity.ok(livroService.buscarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable Long id, @RequestBody Livro livro) {
        return ResponseEntity.ok(livroService.atualizar(id, livro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable Long id) {
        livroService.deletar(id);
        return ResponseEntity.noContent().build();
    }
//
//    @GetMapping("/autor")
//    public ResponseEntity<List<Livro>> buscarPorAutor(@RequestParam String autor) {
//        return ResponseEntity.ok(livroService.buscarPorAutorOrdenado(autor, true));
//    }
}

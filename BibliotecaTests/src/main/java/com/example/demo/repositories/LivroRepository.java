package com.example.demo.repositories;

import com.example.demo.models.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByAutorOrderByTituloAsc(String autor);
    List<Livro> findByAutorOrderByTituloDesc(String autor);
}

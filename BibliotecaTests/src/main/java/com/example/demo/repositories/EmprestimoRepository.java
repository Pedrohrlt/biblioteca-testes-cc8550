package com.example.demo.repositories;

import com.example.demo.models.Emprestimo;
import com.example.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    // Retorna todos os empréstimos de um usuário que ainda não foram devolvidos
    List<Emprestimo> findByUsuarioAndDataDevolucaoIsNull(Usuario usuario);

    // Retorna todos os empréstimos de um usuário (independente do status)
    List<Emprestimo> findByUsuario(Usuario usuario);
}

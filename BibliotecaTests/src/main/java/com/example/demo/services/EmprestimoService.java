package com.example.demo.services;

import com.example.demo.models.Emprestimo;
import com.example.demo.models.Livro;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.EmprestimoRepository;
import com.example.demo.repositories.LivroRepository;
import com.example.demo.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService extends AbstractService<Emprestimo, Long> {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             UsuarioRepository usuarioRepository,
                             LivroRepository livroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
    }

    @Override
    public Emprestimo criar(Emprestimo emprestimo) {
        // valida usuário
        Usuario usuario = checkNotNull(usuarioRepository.findById(emprestimo.getUsuario().getId()),
                "Usuário não encontrado");

        // valida livro
        Livro livro = checkNotNull(livroRepository.findById(emprestimo.getLivro().getId()),
                "Livro não encontrado");

        // regra: usuário não pode ter mais de 5 empréstimos abertos
        List<Emprestimo> abertos = emprestimoRepository.findByUsuarioAndDataDevolucaoIsNull(usuario);
        if (abertos.size() >= 5) {
            throw new IllegalArgumentException("Usuário já possui 5 empréstimos abertos.");
        }

        // valida datas
        LocalDate dataEmprestimo = emprestimo.getDataEmprestimo();
        LocalDate dataDevolucao = emprestimo.getDataDevolucao();
        if (dataEmprestimo == null) dataEmprestimo = LocalDate.now();
        if (dataDevolucao != null && dataDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("Data de devolução não pode ser anterior à data do empréstimo.");
        }

        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(dataEmprestimo);

        return emprestimoRepository.save(emprestimo);
    }

    @Override
    public Emprestimo buscarPorId(Long id) {
        return checkNotNull(emprestimoRepository.findById(id), "Empréstimo não encontrado");
    }

    @Override
    public List<Emprestimo> buscarTodos() {
        return emprestimoRepository.findAll();
    }

    @Override
    public Emprestimo atualizar(Long id, Emprestimo emprestimo) {
        Emprestimo existente = buscarPorId(id);

        // só atualiza dataDevolucao e livro/usuario se passados
        if (emprestimo.getDataDevolucao() != null) {
            if (emprestimo.getDataDevolucao().isBefore(existente.getDataEmprestimo())) {
                throw new IllegalArgumentException("Data de devolução não pode ser anterior à data do empréstimo.");
            }
            existente.setDataDevolucao(emprestimo.getDataDevolucao());
        }

        if (emprestimo.getLivro() != null && emprestimo.getLivro().getId() != null) {
            Livro livro = checkNotNull(livroRepository.findById(emprestimo.getLivro().getId()), "Livro não encontrado");
            existente.setLivro(livro);
        }

        if (emprestimo.getUsuario() != null && emprestimo.getUsuario().getId() != null) {
            Usuario usuario = checkNotNull(usuarioRepository.findById(emprestimo.getUsuario().getId()), "Usuário não encontrado");
            existente.setUsuario(usuario);
        }

        return emprestimoRepository.save(existente);
    }

    @Override
    public void deletar(Long id) {
        buscarPorId(id); // valida existência
        emprestimoRepository.deleteById(id);
    }

    // consulta extra: empréstimos de um usuário
    public List<Emprestimo> buscarPorUsuario(Usuario usuario) {
        return emprestimoRepository.findByUsuario(usuario);
    }
}

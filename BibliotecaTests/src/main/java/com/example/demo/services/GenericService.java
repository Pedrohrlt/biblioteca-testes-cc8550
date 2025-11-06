package com.example.demo.services;

import java.util.List;

public interface GenericService<T, ID> {
    T criar(T entity);
    T buscarPorId(ID id);
    List<T> buscarTodos();
    T atualizar(ID id, T entity);
    void deletar(ID id);
}

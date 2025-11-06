package com.example.demo.services;

import java.util.Optional;

public abstract class AbstractService<T, ID> implements GenericService<T, ID> {

    protected <R> R checkNotNull(Optional<R> entity, String msg) {
        return entity.orElseThrow(() -> new RuntimeException(msg));
    }

}

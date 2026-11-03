package com.prontaentrega.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersistenceManager {

    @PersistenceContext
    private EntityManager entityManager;

    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
package com.prontaentrega.persistence;

import com.prontaentrega.utils.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@Tag("modelo")
public class PersistenceManagerTest extends AbstractIntegrationTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PersistenceManager persistenceManager;

    @Test
    void flushAndClear_debeEjecutarFlushYClear() {
        // When
        persistenceManager.flushAndClear();

        // Then
        InOrder inOrder = inOrder(entityManager);

        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).clear();

        verifyNoMoreInteractions(entityManager);
    }
}

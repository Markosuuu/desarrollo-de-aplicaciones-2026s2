package com.prontaentrega.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.persistence.repository.dao.EstadoEstrategiaCotizacionDAOSQL;
import com.prontaentrega.services.CotizacionService;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Comprueba serialización de mutaciones del estado y la snapshot isolation del recálculo. */
@SpringBootTest
@Tag("integracion")
class QuoteStrategyLockingTest extends AbstractIntegrationTest {
    @Autowired
    private EstadoEstrategiaCotizacionDAOSQL estadoDAO;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /** Una segunda transacción no adquiere el singleton hasta liberar el lock de la primera. */
    @Test
    void serializaOperacionesQueBloqueanEstadoGlobal() throws Exception {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        CountDownLatch firstHasLock = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> first = executor.submit(() -> transaction.execute(status -> {
            estadoDAO.buscarBloqueando("global");
            firstHasLock.countDown();
            try {
                if (!releaseFirst.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timeout esperando fin de test");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
            return true;
        }));

        try {
            assertThat(firstHasLock.await(5, TimeUnit.SECONDS)).isTrue();
            Future<Boolean> second = executor.submit(() -> transaction.execute(status ->
                    estadoDAO.buscarBloqueando("global").isPresent()));

            assertThatThrownBy(() -> second.get(150, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
            releaseFirst.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS)).isTrue();
            assertThat(second.get(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    /** El recálculo conserva un snapshot consistente a través de las páginas keyset. */
    @Test
    void recalculoUsaRepeatableRead() throws Exception {
        Transactional transactional = CotizacionService.class
                .getMethod("recalcularEstrategiaActiva")
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.isolation()).isEqualTo(Isolation.REPEATABLE_READ);
    }
}
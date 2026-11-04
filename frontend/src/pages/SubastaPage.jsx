import { useCallback, useEffect, useRef, useState } from 'react';

import { useAuth } from '../context/AuthContext';
import { fetchSubastas } from '../api/subastaApi';
import SubastaCard from '../components/SubastaCard';
import '../styles/subastaPage.css';

const PAGE_SIZE = 10;

export default function SubastasPage() {
  const { token } = useAuth();

  const [subastas, setSubastas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(true);

  const loadingRef = useRef(false);
  const hasMoreRef = useRef(true);
  const nextPageRef = useRef(1);

  const sentinelRef = useRef(null);

  const loadPage = useCallback(async (page) => {
    if (loadingRef.current || !hasMoreRef.current) return;

    loadingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      const response = await fetchSubastas({
        page,
        size: PAGE_SIZE,
        token,
      });

      const nuevasSubastas = response.subastas ?? [];

      setSubastas((prev) => [...prev, ...nuevasSubastas]);

      const hayMas = nuevasSubastas.length === PAGE_SIZE;

      hasMoreRef.current = hayMas;
      setHasMore(hayMas);

      nextPageRef.current = page + 1;
    } catch (err) {
      setError(err.message || 'Ocurrió un error al cargar las subastas.');
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  }, [token]);

  // Carga inicial.
  useEffect(() => {
    loadPage(1);
  }, [loadPage]);

  // Configuración del scroll infinito.
  useEffect(() => {
    if (loading || error || !hasMore || !sentinelRef.current) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          loadPage(nextPageRef.current);
        }
      },
      {
        rootMargin: '200px',
        threshold: 0,
      }
    );

    observer.observe(sentinelRef.current);

    return () => observer.disconnect();
  }, [loading, error, hasMore, loadPage]);

  const handleRetry = () => {
    loadPage(nextPageRef.current);
  };

  return (
    <main className="subastas-page">
      <header className="subastas-header">
        <h1>Subastas</h1>
        <p>Explorá las subastas disponibles en el mercado.</p>
      </header>

      <section className="subastas-list">
        {subastas.map((subasta) => (
          <SubastaCard
            key={subasta.id}
            subasta={subasta}
          />
        ))}
      </section>

      {loading && (
        <p className="subastas-message">
          Cargando subastas...
        </p>
      )}

      {error && (
        <div className="subastas-error">
          <p>{error}</p>
          <button type="button" onClick={handleRetry}>
            Reintentar
          </button>
        </div>
      )}

      {!loading && !error && hasMore && (
        <div
          ref={sentinelRef}
          className="subastas-observer"
        />
      )}

      {!loading && !error && !hasMore && subastas.length > 0 && (
        <p className="subastas-message subastas-end">
          Llegaste al final de las subastas.
        </p>
      )}

      {!loading && !error && subastas.length === 0 && (
        <p className="subastas-message">
          No hay subastas disponibles.
        </p>
      )}
    </main>
  );
}

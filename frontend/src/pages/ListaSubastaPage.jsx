import { useCallback, useEffect, useRef, useState } from "react";

import { useAuth } from "../context/AuthContext";
import { buscarSubastasPorNombre, fetchSubastas } from "../api/subastaApi";
import SubastaCard from "../components/SubastaCard";
import "../styles/listaSubastaPage.css";

const PAGE_SIZE = 10;

function normalizeSubastaResponse(response) {
  if (Array.isArray(response)) {
    return {
      subastas: response,
      page: 1,
      size: PAGE_SIZE,
      totalElements: response.length,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
    };
  }

  const subastas = Array.isArray(response?.subastas)
    ? response.subastas
    : Array.isArray(response?.content)
      ? response.content
      : [];

  return {
    subastas,
    page: Number(response?.page ?? 1),
    size: Number(response?.size ?? PAGE_SIZE),
    totalElements: Number(response?.totalElements ?? subastas.length),
    totalPages: Number(response?.totalPages ?? 1),
    hasNext: Boolean(response?.hasNext),
    hasPrevious: Boolean(response?.hasPrevious),
  };
}

export default function ListaSubastasPage() {
  const { token } = useAuth();

  const [subastas, setSubastas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(true);

  const [searchTerm, setSearchTerm] = useState("");

  const loadingRef = useRef(false);
  const hasMoreRef = useRef(true);
  const nextPageRef = useRef(1);
  const requestGenerationRef = useRef(0);

  const sentinelRef = useRef(null);

  const loadPage = useCallback(
    async (page) => {
      if (page !== 1 && (loadingRef.current || !hasMoreRef.current)) return;

      const requestGeneration =
        page === 1
          ? ++requestGenerationRef.current
          : requestGenerationRef.current;

      if (page === 1) {
        hasMoreRef.current = true;
        nextPageRef.current = 1;
        setHasMore(true);
        setSubastas([]);
      }

      loadingRef.current = true;
      setLoading(true);
      setError(null);

      try {
        const jugador = searchTerm.trim();
        const response = jugador
          ? await buscarSubastasPorNombre({
              page,
              size: PAGE_SIZE,
              user: jugador,
              jugador,
              token,
            })
          : await fetchSubastas({
              page,
              size: PAGE_SIZE,
              token,
            });

        if (requestGeneration !== requestGenerationRef.current) return;

        const payload = normalizeSubastaResponse(response);
        const nuevasSubastas = payload.subastas ?? [];

        setSubastas((prev) => (page === 1 ? nuevasSubastas : [...prev, ...nuevasSubastas]));

        const hayMas = payload.hasNext ?? nuevasSubastas.length === PAGE_SIZE;

        hasMoreRef.current = hayMas;
        setHasMore(hayMas);

        nextPageRef.current = hayMas ? page + 1 : page;
      } catch (err) {
        if (requestGeneration === requestGenerationRef.current) {
          setError(err.message || "Ocurrió un error al cargar las subastas.");
        }
      } finally {
        if (requestGeneration === requestGenerationRef.current) {
          loadingRef.current = false;
          setLoading(false);
        }
      }
    },
    [searchTerm, token],
  );

  useEffect(() => {
    void loadPage(1);
  }, [loadPage]);

  useEffect(() => {
    if (loading || error || !hasMore || !sentinelRef.current) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          void loadPage(nextPageRef.current);
        }
      },
      {
        rootMargin: "200px",
        threshold: 0,
      },
    );

    observer.observe(sentinelRef.current);

    return () => observer.disconnect();
  }, [loading, error, hasMore, loadPage]);

  const handleRetry = () => {
    void loadPage(nextPageRef.current);
  };

  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
  };

  return (
    <main className="subastas-page">
      <header className="subastas-header">
        <div>
          <h1>Subastas</h1>
          <p>Explorá las subastas disponibles en el mercado.</p>
        </div>
        <div>
          <label className="search-field" aria-label="Buscar jugadores">
            <span className="search-label">Buscar</span>
            <input
              type="search"
              value={searchTerm}
              onChange={handleSearch}
              placeholder="Nombre del jugador"
            />
          </label>
        </div>
      </header>

      <section className="subastas-list">
        {subastas.map((subasta) => (
          <SubastaCard key={subasta.id} subasta={subasta} />
        ))}
      </section>

      {loading && <p className="subastas-message">Cargando subastas...</p>}

      {error && (
        <div className="subastas-error">
          <p>{error}</p>
          <button type="button" onClick={handleRetry}>
            Reintentar
          </button>
        </div>
      )}

      {!loading && !error && hasMore && <div ref={sentinelRef} className="subastas-observer" />}

      {!loading && !error && !hasMore && subastas.length > 0 && (
        <p className="subastas-message subastas-end">Fin de las subastas activas</p>
      )}

      {!loading && !error && subastas.length === 0 && (
        <p className="subastas-message">No existen subastas activas actualmente</p>
      )}
    </main>
  );
}

import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { fetchSubasta, cerrarSubasta, ofertarSubasta } from "../api/subastaApi";

import "../styles/SubastaPage.css";

import InformacionSubasta from "../components/subasta/InformacionSubasta";
import EstadisticasJugador from "../components/subasta/EstadisticasJugador";
import DatosJugador from "../components/subasta/DatosJugador";
import HistorialOfertas from "../components/subasta/HistorialOfertas";
import CerrarSubasta from "../components/subasta/CerrarSubasta";
import RealizarOferta from "../components/subasta/RealizarOferta";

export function formatNumber(value, decimals = 2) {
  if (value == null) return "No disponible";

  return Number(value).toLocaleString("es-AR", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function getMinimumBid(currentOffer) {
  return currentOffer != null
    ? Number((Number(currentOffer) + 0.01).toFixed(2))
    : 0.01;
}

export default function SubastaPage() {
  const { id } = useParams();
  const { user, token } = useAuth();
  const navigate = useNavigate();

  const [subasta, setSubasta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showCloseConfirmation, setShowCloseConfirmation] = useState(false);
  const [showOfferForm, setShowOfferForm] = useState(false);

  const [puja, setPuja] = useState("");
  const [closing, setClosing] = useState(false);
  const [offering, setOffering] = useState(false);

  const [actionError, setActionError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Carga la información de la subasta.
  const loadSubasta = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetchSubasta(id, token);
      setSubasta(response);
    } catch (err) {
      setError(err.message || "No se pudo cargar la subasta.");
    } finally {
      setLoading(false);
    }
  }, [id, token]);

  useEffect(() => {
    void loadSubasta();
  }, [loadSubasta]);

  // Los errores y mensajes desaparecen automáticamente.
  useEffect(() => {
    if (!actionError && !successMessage) return;

    const timeout = setTimeout(() => {
      setActionError(null);
      setSuccessMessage(null);
    }, 5000);

    return () => clearTimeout(timeout);
  }, [actionError, successMessage]);

  // Cerrar la subasta.
  const handleCloseAuction = async () => {
    setClosing(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      await cerrarSubasta(id, token);
      void navigate("/profile");
    } catch (err) {
      setActionError(err.message || "No se pudo cerrar la subasta.");
      setShowCloseConfirmation(false);
    } finally {
      setClosing(false);
    }
  };

  // Realizar una oferta.
  const handleOffer = async (event) => {
    event.preventDefault();

    const numericBid = Number(puja);
    const minimumBid = getMinimumBid(subasta.ofertaActual);

    if (!Number.isFinite(numericBid) || numericBid < minimumBid) {
      setActionError(`La puja mínima debe ser ${formatNumber(minimumBid)}.`);
      return;
    }

    setOffering(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      await ofertarSubasta({
        subastaId: id,
        puja: numericBid,
        token,
      });

      setPuja("");
      setShowOfferForm(false);
      setSuccessMessage("¡Tu oferta se realizó correctamente!");

      await loadSubasta();
    } catch (err) {
      setActionError(err.message || "No se pudo realizar la oferta.");
    } finally {
      setOffering(false);
    }
  };

  if (loading) {
    return (
      <main className="subasta-detail-page">
        <p className="subasta-page-message">Cargando subasta...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="subasta-detail-page">
        <div className="subasta-page-error">
          <p>{error}</p>
          <button type="button" onClick={loadSubasta}>
            Reintentar
          </button>
        </div>
      </main>
    );
  }

  if (!subasta) return null;

  const { jugador, propietario, ofertasPrevias = [] } = subasta;

  const isOwner = user?.id === propietario?.id;
  const isActive = subasta.estado;

  const minimumBid = getMinimumBid(subasta.ofertaActual);

  return (
    <main className="subasta-detail-page">
      {/* Encabezado */}
      <header className="subasta-detail-header">
        <div>
          <span className="subasta-detail-eyebrow">MERCADO DE JUGADORES</span>

          <h1>{jugador?.nombre || "Subasta"}</h1>

          <p>
            {jugador?.equipo} · {jugador?.liga} · {jugador?.posicion}
          </p>
        </div>

        <span
          className={`subasta-detail-status ${isActive ? "active" : "inactive"}`}
        >
          {isActive ? "Subasta activa" : "Subasta finalizada"}
        </span>
      </header>

      {/* Información principal */}
      <InformacionSubasta
        jugador={jugador}
        propietario={propietario}
        subasta={subasta}
      />

      {/* Estadísticas del jugador */}
      <EstadisticasJugador jugador={jugador} />

      {/* Información adicional */}
      <DatosJugador jugador={jugador} />

      {/* Historial de ofertas */}
      <HistorialOfertas ofertasPrevias={ofertasPrevias} />

      {/* Mensajes de acciones TO_DO: Cambiar por toasty*/}
      {actionError && (
        <div className="subasta-action-feedback error" role="alert">
          {actionError}
        </div>
      )}

      {successMessage && (
        <div className="subasta-action-feedback success">{successMessage}</div>
      )}

      {/* Acciones del usuario */}
      {isActive && (
        <section className="subasta-actions">
          {isOwner ? (
            <CerrarSubasta
              showCloseConfirmation={showCloseConfirmation}
              setShowCloseConfirmation={setShowCloseConfirmation}
              closing={closing}
              handleCloseAuction={handleCloseAuction}
            />
          ) : (
            <RealizarOferta
              showOfferForm={showOfferForm}
              setShowOfferForm={setShowOfferForm}
              handleOffer={handleOffer}
              subasta={subasta}
              minimumBid={minimumBid}
              puja={puja}
              setPuja={setPuja}
              offering={offering}
              setActionError={setActionError}
            />
          )}
        </section>
      )}
    </main>
  );
}

import { useState } from "react";
import { getQuoteByDate } from "../api/playerApi";
import "../styles/playerPage.css"
import "../styles/mercadoPage.css"


export default function BuscadorCotizacionPorFecha({playerId, token}) {
    const [quoteDate, setQuoteDate] = useState('');
    const [historicalQuote, setHistoricalQuote] = useState(null);
    const [quoteLoading, setQuoteLoading] = useState(false);
    const [quoteError, setQuoteError] = useState('');

    const hoy = new Date();
    const fechaActual = [
        hoy.getFullYear(),
        String(hoy.getMonth() + 1).padStart(2, '0'),
        String(hoy.getDate()).padStart(2, '0')
    ].join('-');
    
    const handleQuoteSearch = async (e) => {
        e.preventDefault();

        if (!quoteDate) return;

        setQuoteLoading(true);
        setQuoteError('');
        setHistoricalQuote(null);

        try {
            const result = await getQuoteByDate({
                id: playerId,
                fecha: quoteDate,
                token,
            });

            setHistoricalQuote(result);
        } catch (error) {
            setQuoteError(error.message);
        } finally {
            setQuoteLoading(false);
        }
    };
    
    return(
        <div className="playerPage-quote-search">
            <span>
                Consultar cotización histórica
            </span>

            <form onSubmit={handleQuoteSearch}>
                <input
                type="date"
                value={quoteDate}
                max={fechaActual}
                onChange={(e) => setQuoteDate(e.target.value)}
                required
                />

                <button className="market-action-button" type="submit" disabled={quoteLoading}>
                {quoteLoading ? 'Buscando...' : 'Consultar'}
                </button>
            </form>
            
            {quoteError && (
                <p className="playerPage-quote-error">{quoteError}</p>
            )}

            {historicalQuote && (
                <div className="playerPage-quote-result">
                    <span>Cotización:</span>
                    <strong>{historicalQuote.precio.toFixed(2)}</strong>
                </div>
            )}
            </div>
    )
}
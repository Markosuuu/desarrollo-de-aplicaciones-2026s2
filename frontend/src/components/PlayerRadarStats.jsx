import { RadarChart } from "@mui/x-charts";
import { useTheme } from "../context/ThemeContext";

export default function PlayerRadar({ disparos, pases, regates, faltas, goles, asist, max }) {
    const { isDark } = useTheme();

    return (
        <RadarChart
            height={235}
            width={300}
            shape="sharp"
            fillArea="true"
            colors={[isDark ? "#61b4f8" : "#d21969"]}
            radar={{
                max: max,
                metrics: [
                    "Goles",
                    "Asistencias",
                    "%-Disparos",
                    "%-Pases",
                    "%-Regates",
                    "%-Faltas"
                ],
            }}
            series={[
                {
                    data: [disparos, pases, regates, faltas, asist, goles],
                    fillArea: true,
                },
            ]}
            sx={{
                color: isDark ? "#fa0606" : "#212121",

                "& text": {
                    fill: isDark ? "#ffffff" : "#212121",
                },

                "& .MuiRadarChart-gridStripe": {
                    stroke: isDark ? "#fdfafa58" : "#cccccc",
                },
                "& .MuiRadarChart-gridRadial": {
                    stroke: isDark ? "#00aeff" : "#a82e2e",
                },

                // Polígono de los datos
                "& .MuiRadarSeriesPlot-area": {
                    fillOpacity: 0.5,
                },
                "& .MuiRadarSeriesPlot-line": {
                    stroke: isDark ? "#90caf9" : "#d21919",
                    strokeWidth: 2,
                },
            }}
        />
    );
}
import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Sidebar from "./components/Sidebar";
import ToastNotifications from "./components/ToastNotifications";
import { useAuth } from "./context/AuthContext";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import PlayerPage from "./pages/PlayerPage";
import RankingPage from "./pages/RankingPage";
import UserPage from "./pages/UserPage";
import MercadoPage from "./pages/MercadoPage";
import OfertaPage from "./pages/OfertaPage";
import ListaSubastasPage from "./pages/ListaSubastaPage";
import SubastaPage from "./pages/SubastaPage";

export default function App() {
  const { isAuthenticated } = useAuth();

  return (
    <>
      <ToastNotifications />
      <Routes>
        <Route
          path="/login"
          element={
            <ProtectedRoute requireAuth={false}>
              <LoginPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/register"
          element={
            <ProtectedRoute requireAuth={false}>
              <RegisterPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <HomePage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/player/:id"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <PlayerPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <UserPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/mercado"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <MercadoPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/mercado/:id"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <OfertaPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />

        <Route
          path="/ranking"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <RankingPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/subasta/:id"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <SubastaPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="/subasta"
          element={
            <ProtectedRoute requireAuth>
              <Sidebar>
                <ListaSubastasPage />
              </Sidebar>
            </ProtectedRoute>
          }
        />
        <Route
          path="*"
          element={<Navigate to={isAuthenticated ? "/" : "/login"} replace />}
        />
      </Routes>
    </>
  );
}

import { Navigate, Outlet } from "react-router";
import { useAuth } from "../contexts/auth";

export default function ProtectedRoute() {
  const { isLogged } = useAuth();

  if (!isLogged) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />;
}

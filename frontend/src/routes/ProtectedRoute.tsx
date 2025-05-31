import { Navigate, Outlet } from "react-router";
import { useAuth } from "../contexts/auth";

type Props = {
  operatorOnly?: boolean;
}

export default function ProtectedRoute({ operatorOnly }: Props) {
  const { isLogged, user } = useAuth();

  if (!isLogged) {
    return <Navigate to="/login" replace />
  }

  if (operatorOnly && !user?.operator) {
    return <Navigate to="/not-found" replace />
  }


  return <Outlet />;
}

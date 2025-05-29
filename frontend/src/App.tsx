import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./pages/Home";
import "./index.css"
import NotFound from "./pages/NotFound";
import Stations from "./pages/StationDiscovery";
import Login from "./pages/Login";
import { AuthProvider } from "./contexts/auth";
import Signup from "./pages/Signup";
import ProtectedRoute from "./routes/ProtectedRoute";
import StationDetails from "./pages/StationDetails";
import ChargerInteraction from "./pages/ChargerInteraction";

const router = createBrowserRouter([
  {
    path: "/",
    children: [
      { index: true, element: <Home /> },
      { path: "login", element: <Login /> },
      { path: "/signup", element: <Signup /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: "stations", element: <Stations /> },
          { path: "stations/:postId", element: <StationDetails /> },
          { path: "/charger/:chargerId", element: <ChargerInteraction/>}
        ]
      },
      {
        path: "/*",
        element: <NotFound />
      },
    ]
  }
]);

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}

export default App

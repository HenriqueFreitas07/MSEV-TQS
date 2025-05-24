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

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />
  },
  {
    path: "/stations",
    element: <Stations />
  },
  {
    path: "/login",
    element: <Login />
  },
  {
    path: "/signup",
    element: <Signup />
  },
  {
    path: "/*",
    element: <NotFound />
  },
]);

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}

export default App

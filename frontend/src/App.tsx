import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./components/Home";
import "./index.css"
import StationDetails from "./pages/StationDetails";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />
  },
  {
    path: "/station/:postId",
    element: <StationDetails />
  },
]);

function App() {
  return <RouterProvider router={router} />
}

export default App

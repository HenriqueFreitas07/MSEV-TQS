import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./components/Home";
import "./index.css"
import StationDetails from "./app/station/StationDetails";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />
  },
    {
    path: "/station",
    element: <StationDetails />
  },
]);

function App() {
  return   <RouterProvider router={router} />
}

export default App

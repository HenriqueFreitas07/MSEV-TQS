import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./components/Home";
import "./index.css"

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />
  },
]);

function App() {
  return   <RouterProvider router={router} />
}

export default App

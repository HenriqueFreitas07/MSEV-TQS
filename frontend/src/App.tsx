import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./pages/Home";
import "./index.css"
import NotFound from "./pages/NotFound";
import  Stations from "./pages/Stations";

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
    path:"/*",
    element: <NotFound />
  } ,
]);

function App() {
  return   <RouterProvider router={router} />
}

export default App

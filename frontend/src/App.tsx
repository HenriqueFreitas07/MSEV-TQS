import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import Home from "./pages/Home";
import "./index.css"
import NotFound from "./pages/NotFound";
import StationDetails from "./pages/StationDetails";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />
  },
  {
    path:"/*",
    element: <NotFound />
  } ,
  {
    path: "/station/:postId",
    element: <StationDetails />
  },
]);

function App() {
  return <RouterProvider router={router} />
}

export default App

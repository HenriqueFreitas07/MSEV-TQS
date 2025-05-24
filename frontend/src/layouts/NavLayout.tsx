import { NavLink } from "react-router";
import { useAuth } from "../contexts/auth"

type Props = {
  children: React.ReactNode,
  title: string
}

export default function Main({ children, title }: Props) {
  const { isLogged, user } = useAuth();

  return (
    <>
      <div className="navbar bg-base-100 shadow-sm">
        <div className="flex-1">
          <NavLink to="/" className="btn btn-ghost text-xl">{title}</NavLink>
        </div>
        <div className="flex-none">
          <ul className="menu menu-horizontal px-1">
            <li>
              <NavLink to="/stations">Stations</NavLink>
            </li>
            {!isLogged ? (
              <li>
                <NavLink to="/login">Login</NavLink>
              </li>
            ) : (
              <li className="justify-center">
                Hi, {user!.name}
              </li>
            )}
          </ul>
        </div>
      </div>
      {
        children
      }
    </>
  )
}

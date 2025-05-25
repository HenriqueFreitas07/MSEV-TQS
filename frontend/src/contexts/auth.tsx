import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { User } from "../types/user"
import { AuthService, UserService } from "../requests";
import Cookies from "js-cookie";

type AuthContextData = {
  isLogged: boolean;
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true)

  async function loadUser() {
    if (!Cookies.get("logged")) {
      setLoading(false);
      return;
    }

    try {
      const user = await UserService.getSelfUser();
      setUser(user);
    } catch (error) {
      console.log("Invalid access token");
      console.log(error);
      Cookies.remove("logged");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUser();
  }, [])

  async function login(email: string, password: string) {
    await AuthService.login({ email, password });
    Cookies.set("logged", "1");
    await loadUser();
  }

  async function logout() {
    Cookies.remove("logged");
    await AuthService.logout();
    setUser(null);
  }

  if (loading) return <div />;

  return (
    <AuthContext.Provider value={{ isLogged: !!user, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext);

  return context;
}

export default AuthContext;

import { useState, type FormEvent } from "react";
import { useAuth } from "../contexts/auth";
import { NavLink, useNavigate } from "react-router";
import Swal from "sweetalert2";
import type { Error } from "../types/error";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { login } = useAuth();

  const navigate = useNavigate();

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    try {
      await login(email, password);
      navigate("/stations");
    } catch (error) {
      const err = error as Error;

      Swal.fire({
        icon: "error",
        title: "Invalid credentials",
        text: err.message
      });
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-base-200">
      <div className="card w-full max-w-sm shadow-2xl bg-base-100">
        <form className="card-body" onSubmit={handleLogin}>
          <h2 className="text-2xl font-bold text-center">Login</h2>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Email</span>
            </label>
            <input
              type="email"
              placeholder="email@example.com"
              className="input input-bordered w-full"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              data-testid="login-input"
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Password</span>
            </label>
            <input
              type="password"
              placeholder="••••••••"
              className="input input-bordered w-full"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              data-testid="password-input"
              required
            />
          </div>

          <div className="form-control mt-4">
            <button type="submit" className="btn btn-primary w-full" data-testid="login-btn">Login</button>
          </div>
          <p className="text-center text-sm mt-4">
            Don&apos;t have an account?{" "}
            <NavLink to="/signup" className="text-primary hover:underline" data-testid="signup-link">Sign up</NavLink>
          </p>
        </form>
      </div>
    </div>
  );
};

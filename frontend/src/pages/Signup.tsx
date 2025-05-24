import { useState, type FormEvent } from "react";
import { NavLink, useNavigate } from "react-router";
import Swal from "sweetalert2";
import { AuthService } from "../requests";

import type { Error } from "../types/error";
import { useAuth } from "../contexts/auth";

export default function Signup() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const navigate = useNavigate();

  const { login } = useAuth();

  async function handleSignup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (password !== confirmPassword) {
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "Passwords mismatch"
      });
      return;
    }

    try {
      await AuthService.signup({ name, email, password });
      await login(email, password);

      navigate("/");
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
        <form className="card-body" onSubmit={handleSignup}>
          <h2 className="text-2xl font-bold text-center">Sign up</h2>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Name</span>
            </label>
            <input
              type="text"
              placeholder="John Doe"
              className="input input-bordered w-full"
              value={name}
              onChange={(e) => setName(e.target.value)}
              data-testid="name-input"
              required
            />
          </div>

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

          <div className="form-control">
            <label className="label">
              <span className="label-text">Confirm Password</span>
            </label>
            <input
              type="password"
              placeholder="••••••••"
              className="input input-bordered w-full"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              data-testid="password-input"
              required
            />
          </div>

          <div className="form-control mt-4">
            <button type="submit" className="btn btn-primary w-full">Sign up</button>
          </div>
          <p className="text-center text-sm mt-4">
            Already have an account?{" "}
            <NavLink to="/login" className="text-primary hover:underline">Log in</NavLink>
          </p>
        </form>
      </div>
    </div>
  );
};

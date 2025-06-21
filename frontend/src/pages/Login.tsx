import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API } from "../api";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await API.post("/auth/login", {
        username,
        password,
      });
      localStorage.setItem("token", response.data.token);
      navigate("/app");
    } catch (err) {
      setError("Niepoprawne dane logowania");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col items-center justify-center p-4">
        <form
          onSubmit={handleLogin}
          className="bg-white p-6 rounded shadow-md w-full max-w-md"
        >
          <h2 className="text-2xl font-bold mb-4">Logowanie</h2>
          {error && <p className="text-red-500 mb-2">{error}</p>}
          <input
            type="text"
            placeholder="Nazwa użytkownika"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full p-2 mb-3 border rounded"
          />
          <input
            type="password"
            placeholder="Hasło"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full p-2 mb-3 border rounded"
          />
          <button className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition">
            Zaloguj
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;

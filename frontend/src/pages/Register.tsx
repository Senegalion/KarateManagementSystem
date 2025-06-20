import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const [form, setForm] = useState({
    username: "",
    email: "",
    city: "",
    street: "",
    number: "",
    postalCode: "",
    karateClubName: "",
    karateRank: "",
    role: "USER",
    password: "",
  });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/auth/register", form);
      navigate("/login");
    } catch (err) {
      setError("Błąd rejestracji. Upewnij się, że dane są poprawne.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col items-center justify-center p-4">
        <form
          onSubmit={handleSubmit}
          className="bg-white p-6 rounded shadow-md w-full max-w-lg"
        >
          <h2 className="text-2xl font-bold mb-4">Rejestracja</h2>
          {error && <p className="text-red-500 mb-2">{error}</p>}
          {Object.entries(form).map(([key, val]) => (
            <input
              key={key}
              name={key}
              placeholder={key}
              value={val}
              onChange={handleChange}
              className="w-full p-2 mb-3 border rounded"
              type={key === "password" ? "password" : "text"}
            />
          ))}
          <button className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600 transition">
            Zarejestruj się
          </button>
        </form>
      </div>
    </div>
  );
};

export default Register;

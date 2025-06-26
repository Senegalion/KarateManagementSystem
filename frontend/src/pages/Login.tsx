import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API } from "../api";
import ChangeClubButton from "../components/ChangeClubButton";
import { useTranslation } from "react-i18next";

const formatClubName = (club: string | null) => {
  if (!club) return "";
  return club
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

const Login = () => {
  const { t } = useTranslation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const karateClubName = localStorage.getItem("selectedClub");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!karateClubName) {
      setError("No club selected.");
      return;
    }

    try {
      const response = await API.post("/auth/login", {
        username,
        password,
        karateClubName,
      });
      localStorage.setItem("token", response.data.token);
      navigate("/app/dashboard");
    } catch {
      setError("Invalid login credentials or club mismatch.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <form
        onSubmit={handleLogin}
        className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md animate-fade-in"
        autoComplete="off"
      >
        <h2 className="text-3xl font-bold text-gray-800 mb-2 text-center">
          {t("login")}
        </h2>

        {karateClubName && (
          <p className="text-sm text-center text-gray-500 mb-4">
            {t("loggingInAs")}:
            <br />
            <span className="font-semibold text-blue-600">
              {formatClubName(karateClubName)}
            </span>
          </p>
        )}

        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}

        <input
          type="text"
          placeholder={t("username")}
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="w-full px-4 py-2 mb-4 border rounded-lg focus:outline-none focus:ring focus:ring-blue-300"
        />
        <input
          type="password"
          placeholder={t("password")}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full px-4 py-2 mb-4 border rounded-lg focus:outline-none focus:ring focus:ring-blue-300"
        />
        <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition mb-4">
          {t("login")}
        </button>
        <ChangeClubButton />
      </form>
    </div>
  );
};

export default Login;

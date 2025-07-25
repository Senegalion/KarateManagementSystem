import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API } from "../api";
import { useTranslation } from "react-i18next";

const clubOptions = [
  "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO",
  "PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE",
  "WOLOMINSKI_KLUB_SHORIN_RYU_KARATE",
  "MIECHOWSKI_KLUB_SHORIN_RYU_KARATE",
  "LUBELSKA_AKADEMIA_SPORTU",
  "GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE",
  "TARNOWSKA_AKADEMIA_KARATE_I_KOBUDO",
  "KLUB_OKINAWA_KARATE_DO_WARSZAWA",
];

const rankOptions = [
  "KYU_10",
  "KYU_9",
  "KYU_8",
  "KYU_7",
  "KYU_6",
  "KYU_5",
  "KYU_4",
  "KYU_3",
  "KYU_2",
  "KYU_1",
  "DAN_1",
  "DAN_2",
  "DAN_3",
  "DAN_4",
  "DAN_5",
  "DAN_6",
  "DAN_7",
  "DAN_8",
  "DAN_9",
  "DAN_10",
];

const Register = () => {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    username: "",
    email: "",
    city: "",
    street: "",
    number: "",
    postalCode: "",
    karateClubName: "",
    karateRank: "",
    password: "",
  });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await API.post("/auth/register", {
        ...form,
        role: "USER",
      });
      navigate("/login");
    } catch {
      setError("Registration failed. Please check your data.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-2xl animate-fade-in"
        autoComplete="off"
      >
        <h2 className="text-3xl font-bold text-gray-800 mb-6 text-center">
          {t("register")}
        </h2>
        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[
            "username",
            "email",
            "city",
            "street",
            "number",
            "postalCode",
            "password",
          ].map((field) => (
            <input
              key={field}
              type={field === "password" ? "password" : "text"}
              name={field}
              placeholder={t(field)}
              value={(form as any)[field]}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring focus:ring-blue-300"
              required
              autoComplete={
                field === "username"
                  ? "new-username"
                  : field === "password"
                  ? "new-password"
                  : "off"
              }
            />
          ))}

          <select
            name="karateClubName"
            value={form.karateClubName}
            onChange={handleChange}
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring focus:ring-blue-300"
            required
          >
            <option value="" disabled>
              {t("selectKarateClub")}
            </option>
            {clubOptions.map((club) => (
              <option key={club} value={club}>
                {t(club)}
              </option>
            ))}
          </select>

          <select
            name="karateRank"
            value={form.karateRank}
            onChange={handleChange}
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring focus:ring-blue-300"
            required
          >
            <option value="" disabled>
              {t("selectKarateRank")}
            </option>
            {rankOptions.map((rank) => (
              <option key={rank} value={rank}>
                {t(rank)}
              </option>
            ))}
          </select>
        </div>

        <button
          type="submit"
          className="mt-6 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
        >
          {t("register")}
        </button>
      </form>
    </div>
  );
};

export default Register;

import { useNavigate } from "react-router-dom";
import lodzLogo from "../assets/logos/clubs/lodz.png";
import piasecznoLogo from "../assets/logos/clubs/piaseczno.png";
import wolominLogo from "../assets/logos/clubs/wolomin.png";
import miechowLogo from "../assets/logos/clubs/miechow.png";
import lublinLogo from "../assets/logos/clubs/lublin.png";
import gdanskLogo from "../assets/logos/clubs/gdansk.png";
import tarnowLogo from "../assets/logos/clubs/tarnow.png";
import warszawaLogo from "../assets/logos/clubs/warszawa.png";

const clubLogos = [
  {
    name: "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO",
    logo: lodzLogo,
  },
  {
    name: "PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE",
    logo: piasecznoLogo,
  },
  {
    name: "WOLOMINSKI_KLUB_SHORIN_RYU_KARATE",
    logo: wolominLogo,
  },
  {
    name: "MIECHOWSKI_KLUB_SHORIN_RYU_KARATE",
    logo: miechowLogo,
  },
  {
    name: "LUBELSKA_AKADEMIA_SPORTU",
    logo: lublinLogo,
  },
  {
    name: "GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE",
    logo: gdanskLogo,
  },
  {
    name: "TARNOWSKA_AKADEMIA_KARATE_I_KOBUDO",
    logo: tarnowLogo,
  },
  {
    name: "KLUB_OKINAWA_KARATE_DO_WARSZAWA",
    logo: warszawaLogo,
  },
];

const SelectClub = () => {
  const navigate = useNavigate();

  const handleSelectClub = (clubName: string) => {
    localStorage.setItem("selectedClub", clubName);
    const params = new URLSearchParams(window.location.search);
    const redirectTo = params.get("redirect") || "/login";
    navigate(redirectTo);
  };

  return (
    <div className="min-h-screen bg-white flex flex-col items-center justify-center p-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">
        Wybierz klub karate
      </h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
        {clubLogos.map((club) => (
          <div
            key={club.name}
            onClick={() => handleSelectClub(club.name)}
            className="cursor-pointer flex flex-col items-center p-4 bg-gray-100 rounded-xl shadow hover:scale-105 transition"
          >
            <img
              src={club.logo}
              alt={club.name}
              className="w-24 h-24 object-contain mb-2"
            />
            <span className="text-sm text-center font-medium text-gray-700">
              {club.name.replace(/_/g, " ")}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SelectClub;

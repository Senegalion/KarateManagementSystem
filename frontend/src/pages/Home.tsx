import logo1 from "../assets/logo1.png";
import logo2 from "../assets/logo2.png";
import { useTranslation } from "react-i18next";

const Home = () => {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-100 to-gray-200 flex flex-col items-center justify-center text-center p-8">
      <div className="flex flex-col items-center gap-4 animate-fade-in">
        <h1 className="text-5xl font-extrabold text-gray-800 tracking-tight drop-shadow-sm">
          {t("homeTitle")}
        </h1>
        <p className="text-lg text-gray-600 max-w-xl animate-fade-in-delayed">
          {t("homeDescription")}
        </p>

        <div className="mt-8 flex flex-col sm:flex-row gap-8 items-center justify-center animate-fade-in-delayed">
          <img
            src={logo1}
            alt={t("appName") + " Logo 1"}
            className="w-40 h-40 object-contain opacity-90 grayscale hover:grayscale-0 transition-all duration-500 hover:scale-105 shadow-md rounded-xl"
          />
          <img
            src={logo2}
            alt={t("appName") + " Logo 2"}
            className="w-40 h-40 object-contain opacity-90 grayscale hover:grayscale-0 transition-all duration-500 hover:scale-105 shadow-md rounded-xl"
          />
        </div>
      </div>
    </div>
  );
};

export default Home;

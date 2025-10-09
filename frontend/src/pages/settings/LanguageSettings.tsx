import { useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import i18n from "../../i18n";
import { useTranslation } from "react-i18next";

type SettingsContextType = {
  selectedLanguage: string;
  setSelectedLanguage: (lang: string) => void;
};

const LanguageSettings = () => {
  const { t } = useTranslation();
  const { selectedLanguage, setSelectedLanguage } =
    useOutletContext<SettingsContextType>();

  const languages = [
    { code: "pl", label: "Polski" },
    { code: "en", label: "English" },
    { code: "cs", label: "Čeština" },
    { code: "fr", label: "Français" },
    { code: "ja", label: "日本語" },
  ];

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const lang = e.target.value;
    setSelectedLanguage(lang);
    localStorage.setItem("i18nextLng", lang);
    i18n.changeLanguage(lang);
  };

  useEffect(() => {
    const saved = localStorage.getItem("i18nextLng") || "en";
    setSelectedLanguage(saved);
    i18n.changeLanguage(saved);
  }, [setSelectedLanguage]);

  return (
    <div className="bg-white border rounded-2xl p-6 shadow-sm max-w-xl">
      <h2 className="text-xl font-semibold mb-2">🌐 {t("languageSettings")}</h2>
      <p className="text-sm text-gray-500 mb-4">{t("chooseLanguage")}</p>

      <select
        value={selectedLanguage}
        onChange={handleChange}
        className="w-full p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        {languages.map((lang) => (
          <option key={lang.code} value={lang.code}>
            {lang.label}
          </option>
        ))}
      </select>
    </div>
  );
};

export default LanguageSettings;

import { useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import i18n from "../../i18n";

type SettingsContextType = {
  selectedLanguage: string;
  setSelectedLanguage: (lang: string) => void;
};

const LanguageSettings = () => {
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
  }, []);

  return (
    <div className="bg-white p-6 rounded-xl shadow-md max-w-md">
      <h1 className="text-2xl font-bold mb-4">🌐 Language Settings</h1>
      <label className="block mb-2 text-sm font-medium text-gray-700">
        Choose your preferred language:
      </label>
      <select
        value={selectedLanguage}
        onChange={handleChange}
        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
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

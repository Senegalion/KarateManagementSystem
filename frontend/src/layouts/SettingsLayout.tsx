import { Outlet, useNavigate } from "react-router-dom";
import SettingsSidebar from "../components/SettingsSidebar";
import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";

const SettingsLayout = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [selectedLanguage, setSelectedLanguage] = useState("en");
  const [initialLanguage, setInitialLanguage] = useState("en");
  const [isSaving, setIsSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const savedLang = localStorage.getItem("i18nextLng") || "en";
    setSelectedLanguage(savedLang);
    setInitialLanguage(savedLang);
  }, []);

  const handleSave = async () => {
    if (selectedLanguage === initialLanguage) {
      navigate("/app/dashboard");
      return;
    }

    setIsSaving(true);
    setSaved(false);

    await new Promise((res) => setTimeout(res, 1000));
    localStorage.setItem("preferredLanguage", selectedLanguage);

    setIsSaving(false);
    setSaved(true);

    setTimeout(() => {
      navigate("/app/dashboard");
    }, 1000);
  };

  const handleCancel = () => {
    navigate("/app/dashboard");
  };

  const hasChanges = selectedLanguage !== initialLanguage;

  return (
    <div className="flex min-h-screen bg-gray-100 text-gray-900 relative">
      <SettingsSidebar />

      <main className="flex-1 p-6 animate-fade-in pb-20">
        <Outlet context={{ selectedLanguage, setSelectedLanguage }} />

        <div className="fixed bottom-6 right-6 space-y-2 z-10">
          {isSaving && (
            <div className="text-sm text-blue-600 animate-pulse">
              {t("saving")}
            </div>
          )}
          {saved && <div className="text-sm text-green-600">{t("saved")}</div>}
          <button
            onClick={hasChanges ? handleSave : handleCancel}
            className={`px-4 py-2 rounded-full shadow-lg transition ${
              hasChanges
                ? "bg-blue-600 text-white hover:bg-blue-700"
                : "bg-gray-400 text-white hover:bg-gray-500"
            }`}
          >
            {hasChanges ? `💾 ${t("saveAndReturn")}` : `↩️ ${t("dashboard")}`}
          </button>
        </div>
      </main>

      <button
        onClick={handleCancel}
        className="absolute top-4 right-4 bg-red-600 hover:bg-red-700 text-white rounded-full px-3 py-1 text-lg shadow-md transition z-50"
        title={t("exit")}
      >
        ❌
      </button>
    </div>
  );
};

export default SettingsLayout;

import { Outlet, useNavigate } from "react-router-dom";
import SettingsSidebar from "../components/SettingsSidebar";
import { useState } from "react";

const SettingsLayout = () => {
  const navigate = useNavigate();

  const [selectedLanguage, setSelectedLanguage] = useState("en");
  const [isSaving, setIsSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = async () => {
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

  return (
    <div className="flex min-h-screen bg-gray-100 text-gray-900 relative">
      <SettingsSidebar />
      <main className="flex-1 p-6 animate-fade-in pb-20">
        <Outlet context={{ selectedLanguage, setSelectedLanguage }} />

        <div className="fixed bottom-6 right-6 space-y-2">
          {isSaving && (
            <div className="text-sm text-blue-600 animate-pulse">Saving...</div>
          )}
          {saved && <div className="text-sm text-green-600">Saved ✅</div>}
          <button
            onClick={handleSave}
            className="bg-blue-600 text-white px-4 py-2 rounded-full shadow-lg hover:bg-blue-700 transition"
          >
            💾 Save and return to dashboard
          </button>
        </div>
      </main>
    </div>
  );
};

export default SettingsLayout;

import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useTranslation } from "react-i18next";

const tabLink =
  "block px-3 py-2 rounded-lg transition text-sm " +
  "hover:bg-blue-50 hover:text-blue-700";
const tabActive = "bg-blue-600 text-white hover:bg-blue-600 hover:text-white";

export default function SettingsLayout() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [selectedLanguage, setSelectedLanguage] = useState<string>(
    localStorage.getItem("i18nextLng") || "en"
  );

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={() => navigate("/app/dashboard")}
          className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border text-gray-700 hover:bg-gray-50"
        >
          <span>←</span>
          <span>{t("dashboard")}</span>
        </button>
        <h1 className="text-2xl font-bold text-gray-800">⚙️ {t("settings")}</h1>
        <div /> {/* spacer */}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-[220px_1fr] gap-6">
        {/* Left tabs */}
        <aside className="bg-white border rounded-2xl p-3">
          <nav className="space-y-1">
            <NavLink
              to="language"
              className={({ isActive }) =>
                `${tabLink} ${isActive ? tabActive : "text-gray-700"}`
              }
            >
              🌐 {t("languageSettings")}
            </NavLink>
            <NavLink
              to="/app/profile"
              className={({ isActive }) =>
                `${tabLink} ${isActive ? tabActive : "text-gray-700"}`
              }
            >
              👤 {t("profileSettings")}
            </NavLink>
            <NavLink
              to="notifications"
              className={({ isActive }) =>
                `${tabLink} ${isActive ? tabActive : "text-gray-700"}`
              }
            >
              🔔 {t("notificationSettings")}
            </NavLink>
            <NavLink
              to="security"
              className={({ isActive }) =>
                `${tabLink} ${isActive ? tabActive : "text-gray-700"}`
              }
            >
              🔐 {t("securitySettings")}
            </NavLink>
            <NavLink
              to="appearance"
              className={({ isActive }) =>
                `${tabLink} ${isActive ? tabActive : "text-gray-700"}`
              }
            >
              🖌️ {t("appearanceSettings")}
            </NavLink>
          </nav>
        </aside>

        {/* Right content */}
        <main>
          <Outlet context={{ selectedLanguage, setSelectedLanguage }} />
        </main>
      </div>
    </div>
  );
}

import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";

const SettingsSidebar = () => {
  const { t } = useTranslation();

  return (
    <aside className="w-64 bg-white shadow-md h-screen sticky top-0 p-6">
      <h2 className="text-xl font-bold mb-6">{t("settings")}</h2>
      <nav className="flex flex-col gap-3">
        <NavLink
          to="/settings/language"
          className={({ isActive }) =>
            isActive
              ? "text-blue-600 font-semibold"
              : "text-gray-700 hover:text-blue-600"
          }
        >
          🌐 {t("language")}
        </NavLink>
      </nav>
    </aside>
  );
};

export default SettingsSidebar;

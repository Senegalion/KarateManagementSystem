import { useTranslation } from "react-i18next";

export default function AppearanceSettings() {
  const { t } = useTranslation();
  return (
    <div className="bg-white border rounded-2xl p-6 shadow-sm max-w-xl">
      <h2 className="text-xl font-semibold mb-2">
        🖌️ {t("appearanceSettings")}
      </h2>
      <p className="text-sm text-gray-500 mb-4">
        {t("appearanceSettingsSubtitle")}
      </p>

      <div className="space-y-2">
        <label className="flex items-center gap-3">
          <input type="radio" name="theme" defaultChecked />
          <span>{t("lightMode")}</span>
        </label>
        <label className="flex items-center gap-3">
          <input type="radio" name="theme" />
          <span>{t("darkMode")}</span>
        </label>
        <label className="flex items-center gap-3">
          <input type="radio" name="theme" />
          <span>{t("systemMode")}</span>
        </label>
      </div>

      <div className="mt-5">
        <button className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700">
          {t("savePreferences")}
        </button>
      </div>
    </div>
  );
}

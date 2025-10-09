import { useTranslation } from "react-i18next";

export default function NotificationSettings() {
  const { t } = useTranslation();
  return (
    <div className="bg-white border rounded-2xl p-6 shadow-sm max-w-xl">
      <h2 className="text-xl font-semibold mb-2">
        🔔 {t("notificationSettings")}
      </h2>
      <p className="text-sm text-gray-500 mb-4">
        {t("notificationSettingsSubtitle")}
      </p>

      <div className="space-y-3">
        <label className="flex items-center gap-3">
          <input type="checkbox" className="h-4 w-4" defaultChecked />
          <span>{t("emailNotifications")}</span>
        </label>
        <label className="flex items-center gap-3">
          <input type="checkbox" className="h-4 w-4" defaultChecked />
          <span>{t("enrollmentReminders")}</span>
        </label>
        <label className="flex items-center gap-3">
          <input type="checkbox" className="h-4 w-4" defaultChecked />
          <span>{t("paymentReminders")}</span>
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

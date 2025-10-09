import { useState } from "react";
import { useTranslation } from "react-i18next";

export default function SecuritySettings() {
  const { t } = useTranslation();
  const [form, setForm] = useState({ current: "", next: "", confirm: "" });

  const onChange =
    (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [k]: e.target.value }));

  return (
    <div className="bg-white border rounded-2xl p-6 shadow-sm max-w-xl">
      <h2 className="text-xl font-semibold mb-2">🔐 {t("securitySettings")}</h2>
      <p className="text-sm text-gray-500 mb-4">
        {t("securitySettingsSubtitle")}
      </p>

      <div className="space-y-3">
        <div>
          <label className="block text-sm mb-1">{t("currentPassword")}</label>
          <input
            type="password"
            className="w-full p-2 border rounded-lg"
            value={form.current}
            onChange={onChange("current")}
          />
        </div>
        <div>
          <label className="block text-sm mb-1">{t("newPassword")}</label>
          <input
            type="password"
            className="w-full p-2 border rounded-lg"
            value={form.next}
            onChange={onChange("next")}
          />
        </div>
        <div>
          <label className="block text-sm mb-1">{t("confirmPassword")}</label>
          <input
            type="password"
            className="w-full p-2 border rounded-lg"
            value={form.confirm}
            onChange={onChange("confirm")}
          />
        </div>
      </div>

      <div className="mt-5">
        <button className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700">
          {t("updatePassword")}
        </button>
      </div>
    </div>
  );
}

import { useEffect, useMemo, useState } from "react";
import { API } from "../../api";
import { useTranslation } from "react-i18next";

type UserInfo = {
  username: string;
  email: string;
  city?: string;
  street?: string;
  number?: string;
  postalCode?: string;
  karateClubName?: string;
  karateRank?: string;
  roles?: string[];
};

type UpdateUserRequestDto = Partial<UserInfo>;
const emptyUser: UserInfo = {
  username: "",
  email: "",
  city: "",
  street: "",
  number: "",
  postalCode: "",
  karateClubName: "",
  karateRank: "",
  roles: [],
};
const IMMUTABLE_FIELDS: (keyof UserInfo)[] = ["karateClubName", "roles"];

const ProfileSettings = () => {
  const { t } = useTranslation();

  const [initial, setInitial] = useState<UserInfo>(emptyUser);
  const [form, setForm] = useState<UserInfo>(emptyUser);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState<null | "ok" | "err">(null);
  const [error, setError] = useState<string | null>(null);

  const token = localStorage.getItem("token");
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : undefined;

  useEffect(() => {
    if (!token) {
      window.location.href = "/login";
      return;
    }
    setLoading(true);
    API.get("/users/me", { headers: authHeaders })
      .then((res) => {
        const data: UserInfo = res.data ?? {};
        const merged: UserInfo = { ...emptyUser, ...data };
        setInitial(merged);
        setForm(merged);
        setError(null);
      })
      .catch(() => setError(t("failedToLoadUser") || "Failed to load user."))
      .finally(() => setLoading(false));
  }, [t, token]);

  const hasChanges = useMemo(
    () => JSON.stringify(form) !== JSON.stringify(initial),
    [form, initial]
  );

  const onChange =
    (key: keyof UserInfo) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      setForm((f) => ({ ...f, [key]: e.target.value }));
    };

  const handleSave = async () => {
    if (!token) return;
    try {
      setSaving(true);
      setSaved(null);

      const diff: UpdateUserRequestDto = {};
      (Object.keys(form) as (keyof UserInfo)[]).forEach((k) => {
        if (IMMUTABLE_FIELDS.includes(k)) return;
        if (form[k] !== initial[k]) (diff as any)[k] = form[k];
      });

      if (Object.keys(diff).length === 0) {
        setSaved("ok");
        setSaving(false);
        return;
      }

      await API.patch("/users/me", diff, { headers: authHeaders });
      setInitial(form);
      setSaved("ok");
    } catch {
      setSaved("err");
    } finally {
      setSaving(false);
      setTimeout(() => setSaved(null), 2000);
    }
  };

  const handleReset = () => setForm(initial);

  const [confirmDelete, setConfirmDelete] = useState("");
  const canDelete = confirmDelete === form.username;

  const handleDelete = async () => {
    if (!token || !canDelete) return;
    if (!window.confirm(t("confirmDeleteAccount") || "Are you sure?")) return;
    try {
      await API.delete("/users/me", { headers: authHeaders });
      localStorage.removeItem("token");
      window.location.href = "/login";
    } catch {
      alert(t("failedToDeleteAccount") || "Failed to delete account.");
    }
  };

  if (loading) return <div className="p-6">{t("loading")}...</div>;
  if (error) return <div className="p-6 text-red-600">{error}</div>;

  return (
    <div className="bg-white border rounded-2xl p-6 shadow-sm max-w-4xl">
      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-1">
          👤 {t("profileSettings")}
        </h2>
        <p className="text-sm text-gray-500">{t("profileSettingsSubtitle")}</p>
      </div>

      {/* FORM */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("username")}
          </label>
          <input
            className="w-full p-2 border rounded-lg"
            value={form.username}
            onChange={onChange("username")}
          />
        </div>
        <div>
          <label className="block mb-1 text-sm font-medium">{t("email")}</label>
          <input
            type="email"
            className="w-full p-2 border rounded-lg"
            value={form.email}
            onChange={onChange("email")}
          />
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium">{t("city")}</label>
          <input
            className="w-full p-2 border rounded-lg"
            value={form.city || ""}
            onChange={onChange("city")}
          />
        </div>
        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("postalCode")}
          </label>
          <input
            className="w-full p-2 border rounded-lg"
            value={form.postalCode || ""}
            onChange={onChange("postalCode")}
          />
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("street")}
          </label>
          <input
            className="w-full p-2 border rounded-lg"
            value={form.street || ""}
            onChange={onChange("street")}
          />
        </div>
        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("number")}
          </label>
          <input
            className="w-full p-2 border rounded-lg"
            value={form.number || ""}
            onChange={onChange("number")}
          />
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("karateClubName")}
          </label>
          <input
            className="w-full p-2 border rounded-lg bg-gray-50 text-gray-700"
            value={form.karateClubName || ""}
            readOnly
            aria-readonly="true"
          />
          <p className="text-xs text-gray-500 mt-1">{t("fieldIsImmutable")}</p>
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium">
            {t("karateRank")}
          </label>
          <input
            className="w-full p-2 border rounded-lg bg-gray-50 text-gray-700"
            value={form.karateRank || ""}
            readOnly
            aria-readonly="true"
          />
        </div>

        <div className="md:col-span-2">
          <label className="block mb-1 text-sm font-medium">{t("roles")}</label>
          <div className="p-2 border rounded-lg bg-gray-50 text-sm">
            {(form.roles ?? []).join(", ") || t("none")}
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="mt-6 flex flex-wrap gap-2">
        <button
          onClick={handleSave}
          disabled={!hasChanges || saving}
          className={`px-4 py-2 rounded-lg text-white ${
            hasChanges ? "bg-blue-600 hover:bg-blue-700" : "bg-gray-400"
          }`}
        >
          {saving ? t("saving") : t("save")}
        </button>
        <button
          onClick={handleReset}
          disabled={!hasChanges || saving}
          className="px-4 py-2 rounded-lg border hover:bg-gray-50"
        >
          {t("reset")}
        </button>
        {saved === "ok" && (
          <span className="text-green-600 self-center">{t("saved")}</span>
        )}
        {saved === "err" && (
          <span className="text-red-600 self-center">{t("saveFailed")}</span>
        )}
      </div>

      {/* Danger zone */}
      <div className="mt-10 border-t pt-6">
        <h3 className="text-lg font-semibold text-red-600 mb-2">
          {t("dangerZone")}
        </h3>
        <p className="text-sm text-gray-600 mb-3">
          {t("typeUsernameToConfirmDelete")}
        </p>
        <input
          className="w-full md:w-1/2 p-2 border rounded-lg mb-3"
          placeholder={form.username}
          value={confirmDelete}
          onChange={(e) => setConfirmDelete(e.target.value)}
        />
        <button
          onClick={handleDelete}
          disabled={!canDelete}
          className={`px-4 py-2 rounded-lg text-white ${
            canDelete ? "bg-red-600 hover:bg-red-700" : "bg-red-300"
          }`}
        >
          {t("deleteAccount")}
        </button>
      </div>
    </div>
  );
};

export default ProfileSettings;

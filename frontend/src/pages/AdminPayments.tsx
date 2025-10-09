import { useEffect, useMemo, useState } from "react";
import { API } from "../api";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";

type ClubUser = { userId: number; username?: string; email: string };
type UnpaidSummaryDto = {
  months: string[];
  monthlyFee: number | string;
  total: number | string;
};
type PaymentHistoryItemDto = {
  paymentId: number;
  provider: string;
  providerOrderId: string | null;
  currency: string;
  amount: number | string;
  status: "PENDING" | "PAID" | "CANCELLED";
  createdAt: string;
  paidAt: string | null;
  months: string[];
};

const AdminPayments = () => {
  const { t } = useTranslation();

  const [users, setUsers] = useState<ClubUser[]>([]);
  const [userId, setUserId] = useState<number | "">("");

  const [unpaid, setUnpaid] = useState<UnpaidSummaryDto | null>(null);
  const [history, setHistory] = useState<PaymentHistoryItemDto[]>([]);
  const [manualSel, setManualSel] = useState<Record<string, boolean>>({});

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [hasLoaded, setHasLoaded] = useState(false);

  useEffect(() => {
    const clubName = localStorage.getItem("selectedClub");
    if (!clubName) return;
    API.get(`/users/by-club?clubName=${encodeURIComponent(clubName)}`)
      .then((r) => setUsers(r.data))
      .catch(() => setUsers([]));
  }, []);

  useEffect(() => {
    setUnpaid(null);
    setHistory([]);
    setManualSel({});
    setErr(null);
    setHasLoaded(false);
  }, [userId]);

  const usersOptions = useMemo(
    () =>
      users
        .slice()
        .sort((a, b) =>
          a.userId < b.userId ? -1 : a.userId > b.userId ? 1 : 0
        ),
    [users]
  );

  const loadForUser = async () => {
    if (!userId) return;
    setLoading(true);
    setErr(null);
    try {
      const [u, h] = await Promise.all([
        API.get(`/payments/admin/payments/user/${userId}/unpaid`),
        API.get(`/payments/admin/payments/user/${userId}/history`),
      ]);
      setUnpaid(u.data);
      setHistory(h.data);
      const pre: Record<string, boolean> = {};
      (u.data?.months ?? []).forEach((m: string) => (pre[m] = true));
      setManualSel(pre);
      setHasLoaded(true);
    } catch {
      setErr(t("failedToLoad") || "Failed to load");
      setHasLoaded(false);
    } finally {
      setLoading(false);
    }
  };

  const toggleManual = (m: string) => {
    setManualSel((s) => ({ ...s, [m]: !s[m] }));
  };

  const doManualPayment = async () => {
    if (!userId) return;
    const months = Object.keys(manualSel).filter((m) => manualSel[m]);
    if (months.length === 0) return;
    setErr(null);
    try {
      await API.post("/payments/admin/payments/manual", { userId, months });
      await loadForUser();
    } catch {
      setErr(t("manualPaymentFailed") || "Manual payment failed");
    }
  };

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">🏦 {t("paymentsAdmin")}</h1>

      <div className="bg-white border rounded-xl p-5 mb-6">
        <div className="flex flex-col md:flex-row gap-3 items-center">
          <select
            className="px-3 py-2 border rounded w-full md:w-2/3"
            value={userId}
            onChange={(e) =>
              setUserId(e.target.value ? Number(e.target.value) : "")
            }
          >
            <option value="">{t("selectUser")}</option>
            {usersOptions.map((u) => (
              <option key={u.userId} value={u.userId}>
                #{u.userId} • {u.username ?? u.email}
              </option>
            ))}
          </select>
          <button
            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-400"
            disabled={!userId || loading}
            onClick={loadForUser}
          >
            {t("load")}
          </button>
        </div>
      </div>

      {loading ? (
        <p>{t("loading")}...</p>
      ) : err ? (
        <p className="text-red-600">{err}</p>
      ) : hasLoaded ? (
        <>
          <section className="bg-white border rounded-xl p-5 mb-6">
            <h2 className="text-xl font-semibold mb-3">{t("unpaidForUser")}</h2>
            {!unpaid || unpaid.months.length === 0 ? (
              <p className="text-green-700">{t("noUnpaidMonths")}</p>
            ) : (
              <>
                <div className="grid md:grid-cols-2 gap-2 mb-3">
                  {unpaid.months.map((m) => (
                    <label
                      key={m}
                      className="flex items-center gap-3 border rounded px-3 py-2"
                    >
                      <input
                        type="checkbox"
                        checked={!!manualSel[m]}
                        onChange={() => toggleManual(m)}
                      />
                      <span className="font-medium">{m}</span>
                      <span className="ml-auto text-sm text-gray-500">
                        {t("monthlyFee")}: {unpaid.monthlyFee}
                      </span>
                    </label>
                  ))}
                </div>
                <div className="flex items-center gap-3">
                  <button
                    className="px-4 py-2 rounded bg-emerald-600 text-white hover:bg-emerald-700"
                    onClick={doManualPayment}
                  >
                    ✅ {t("bookManualPayment")}
                  </button>
                  <div className="ml-auto text-sm">
                    {t("total")}: <b>{unpaid.total}</b>
                  </div>
                </div>
              </>
            )}
          </section>

          <section className="bg-white border rounded-xl p-5">
            <h2 className="text-xl font-semibold mb-3">
              {t("paymentHistory")}
            </h2>
            {history.length === 0 ? (
              <p className="text-gray-600">{t("noPaymentsYet")}</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm border rounded-xl overflow-hidden">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="p-2 text-left">{t("date")}</th>
                      <th className="p-2 text-left">{t("provider")}</th>
                      <th className="p-2 text-left">{t("amount")}</th>
                      <th className="p-2 text-left">{t("status")}</th>
                      <th className="p-2 text-left">{t("months")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((h) => (
                      <tr key={h.paymentId} className="border-t">
                        <td className="p-2">
                          {dayjs(h.createdAt).format("YYYY-MM-DD HH:mm")}
                        </td>
                        <td className="p-2">{h.provider}</td>
                        <td className="p-2">
                          {h.amount} {h.currency}
                        </td>
                        <td className="p-2">
                          {h.status === "PAID"
                            ? "✅"
                            : h.status === "PENDING"
                            ? "⏳"
                            : "❌"}{" "}
                          {h.status}
                        </td>
                        <td className="p-2">{h.months.join(", ")}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      ) : null}
    </div>
  );
};

export default AdminPayments;

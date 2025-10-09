import { useEffect, useMemo, useState } from "react";
import { API } from "../api";
import { useTranslation } from "react-i18next";
import type {
  UnpaidSummaryDto,
  PaymentHistoryItemDto,
  CreateOrderResponse,
} from "../types/payments";
import dayjs from "dayjs";

const MyPayments = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<UnpaidSummaryDto | null>(null);
  const [history, setHistory] = useState<PaymentHistoryItemDto[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setError(null);
    setLoading(true);
    try {
      const [s, h] = await Promise.all([
        API.get(`/payments/me/unpaid`),
        API.get(`/payments/me/history`),
      ]);
      setSummary(s.data);
      setSelected((s.data?.months as string[]) ?? []);
      setHistory(Array.isArray(h.data) ? h.data : []);
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 401) {
        setError(t("notLoggedIn") || "Not logged in");
      } else if (status === 403) {
        setError(t("noPermission") || "No permission");
      } else {
        setError(t("failedToLoad") || "Failed to load");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const totalSelected = useMemo(() => {
    if (!summary) return 0;
    const fee = Number(summary.monthlyFee || 0);
    return fee * selected.length;
  }, [selected, summary]);

  const toggleMonth = (ym: string) => {
    setSelected((prev) =>
      prev.includes(ym) ? prev.filter((m) => m !== ym) : [...prev, ym]
    );
  };

  const payNow = async () => {
    if (selected.length === 0) return;
    try {
      const origin = window.location.origin;
      const returnUrl = `${origin}/app/payments/return`;
      const cancelUrl = `${origin}/app/payments/cancel`;

      const res = await API.post<any, { data: CreateOrderResponse }>(
        `/payments/me/create-order`,
        { months: selected, returnUrl, cancelUrl }
      );
      window.location.href = res.data.approvalUrl;
    } catch (e: any) {
      const status = e?.response?.status;
      setError(
        status === 403
          ? t("noPermission") || "No permission"
          : t("paymentCreateFailed") || "Failed to create payment"
      );
    }
  };

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">💳 {t("myPayments")}</h1>

      {loading ? (
        <p>{t("loading")}...</p>
      ) : error ? (
        <p className="text-red-600">{error}</p>
      ) : (
        <>
          <section className="bg-white border rounded-xl p-5 mb-6">
            <h2 className="text-xl font-semibold mb-3">{t("unpaidSummary")}</h2>
            {summary && summary.months.length > 0 ? (
              <>
                <p className="text-sm text-gray-600 mb-3">
                  {t("monthlyFee")}: <b>{summary.monthlyFee}</b> | {t("total")}:{" "}
                  <b>{summary.total}</b>
                </p>
                <div className="flex flex-wrap gap-2 mb-4">
                  {summary.months.map((ym) => (
                    <label
                      key={ym}
                      className={
                        "px-3 py-1 border rounded cursor-pointer " +
                        (selected.includes(ym)
                          ? "bg-blue-600 text-white border-blue-600"
                          : "bg-white")
                      }
                    >
                      <input
                        className="mr-2"
                        type="checkbox"
                        checked={selected.includes(ym)}
                        onChange={() => toggleMonth(ym)}
                      />
                      {ym}
                    </label>
                  ))}
                </div>
                <div className="flex items-center gap-4">
                  <div className="text-sm text-gray-700">
                    {t("toPayNow")}:{" "}
                    <b>
                      {totalSelected} {t("currencyShort") || ""}
                    </b>
                  </div>
                  <button
                    className="px-4 py-2 rounded bg-green-600 text-white hover:bg-green-700 disabled:bg-gray-400"
                    disabled={selected.length === 0}
                    onClick={payNow}
                  >
                    {t("payNow")}
                  </button>
                </div>
              </>
            ) : (
              <p className="text-gray-500">{t("noUnpaidMonths")}</p>
            )}
          </section>

          <section className="bg-white border rounded-xl p-5">
            <h2 className="text-xl font-semibold mb-3">
              {t("paymentHistory")}
            </h2>
            {history.length === 0 ? (
              <p className="text-gray-500">{t("noResults")}</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm border rounded-xl overflow-hidden">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="p-2 text-left">ID</th>
                      <th className="p-2 text-left">{t("provider")}</th>
                      <th className="p-2 text-left">{t("amount")}</th>
                      <th className="p-2 text-left">{t("status")}</th>
                      <th className="p-2 text-left">{t("createdAt")}</th>
                      <th className="p-2 text-left">{t("paidAt")}</th>
                      <th className="p-2 text-left">{t("months")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((p) => (
                      <tr key={p.paymentId} className="border-t">
                        <td className="p-2">{p.paymentId}</td>
                        <td className="p-2">
                          {p.provider === "PAYPAL" ? "PayPal" : "Manual"}
                        </td>
                        <td className="p-2">
                          {p.amount} {p.currency}
                        </td>
                        <td className="p-2">{p.status}</td>
                        <td className="p-2">
                          {dayjs(p.createdAt).format("YYYY-MM-DD HH:mm")}
                        </td>
                        <td className="p-2">
                          {p.paidAt
                            ? dayjs(p.paidAt).format("YYYY-MM-DD HH:mm")
                            : "-"}
                        </td>
                        <td className="p-2">{p.months.join(", ")}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
};

export default MyPayments;

import { useEffect, useState } from "react";
import { API } from "../api";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useNavigate } from "react-router-dom";

const PaymentReturn = () => {
  const { t } = useTranslation();
  const { search } = useLocation();
  const navigate = useNavigate();
  const [status, setStatus] = useState<"OK" | "ALREADY" | "ERROR" | null>(null);
  const [msg, setMsg] = useState<string>("");

  useEffect(() => {
    const qp = new URLSearchParams(search);
    const token = qp.get("token");
    if (!token) {
      setStatus("ERROR");
      setMsg(t("noOrderToken") || "Missing order token");
      return;
    }
    API.post(`/payments/capture/${token}`)
      .then((res) => {
        if (res.data?.status === "PAID") {
          setStatus("OK");
          setMsg(t("paymentSuccess") || "Payment completed");
        } else if (res.data?.status === "ALREADY_PAID") {
          setStatus("ALREADY");
          setMsg(t("paymentAlready") || "Payment already captured");
        } else {
          setStatus("ERROR");
          setMsg(t("paymentUnknownState") || "Unknown payment state");
        }
      })
      .catch(() => {
        setStatus("ERROR");
        setMsg(t("paymentFailed") || "Payment failed");
      });
  }, []);

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-4">💳 {t("paymentStatus")}</h1>
      {status === null ? (
        <p>{t("loading")}...</p>
      ) : (
        <>
          <p className={status === "ERROR" ? "text-red-600" : "text-green-700"}>
            {msg}
          </p>
          <div className="mt-4 flex gap-3">
            <button
              className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700"
              onClick={() => navigate("/app/payments")}
            >
              {t("goToPayments")}
            </button>
            <Link
              to="/app/dashboard"
              className="px-4 py-2 rounded border hover:bg-gray-50"
            >
              {t("goToDashboard")}
            </Link>
          </div>
        </>
      )}
    </div>
  );
};

export default PaymentReturn;

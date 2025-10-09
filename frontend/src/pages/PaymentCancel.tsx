import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

const PaymentCancel = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-4">💳 {t("paymentCancelled")}</h1>
      <p className="text-gray-700 mb-4">{t("youCancelledPayment")}</p>
      <button
        className="px-4 py-2 rounded bg-blue-600 text-white"
        onClick={() => navigate("/app/my-payments")}
      >
        {t("goBack")}
      </button>
    </div>
  );
};
export default PaymentCancel;

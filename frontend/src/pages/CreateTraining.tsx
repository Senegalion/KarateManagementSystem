import { useState, useEffect } from "react";
import { API } from "../api";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

type Training = {
  id?: number;
  description: string;
  startTime: string;
  endTime: string;
};

const CreateTraining = () => {
  const { t } = useTranslation();
  const [description, setDescription] = useState("");
  const [startTime, setStartTime] = useState(""); // YYYY-MM-DDTHH:mm
  const [endTime, setEndTime] = useState(""); // YYYY-MM-DDTHH:mm
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [trainings, setTrainings] = useState<Training[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    API.get("/trainings", {
      headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
    }).then((res) => setTrainings(res.data));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Walidacja: pola ustawione i koniec > początek
    if (!startTime || !endTime) {
      setError(t("pleaseFillAllFields") || "Please fill all fields.");
      return;
    }
    const start = dayjs(startTime);
    const end = dayjs(endTime);
    if (!(end.valueOf() > start.valueOf())) {
      setError(
        t("endMustBeAfterStart") || "End time must be after start time."
      );
      return;
    }

    setLoading(true);
    try {
      const payload = {
        description,
        startTime: start.format("YYYY-MM-DDTHH:mm:ss"),
        endTime: end.format("YYYY-MM-DDTHH:mm:ss"),
      };

      await API.post("/trainings/create", payload, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });

      setLoading(false);
      navigate("/app/dashboard");
    } catch (err: any) {
      setLoading(false);
      setError(
        err?.response?.data?.message ||
          t("failedToCreateTraining") ||
          "Failed to create training."
      );
    }
  };

  return (
    <div className="p-6 animate-fade-in max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-5 gap-8">
      <div className="md:col-span-2 bg-white rounded shadow p-6">
        <h1 className="text-3xl font-bold mb-6 text-center">
          {t("createNewTraining")}
        </h1>

        {error && (
          <div className="mb-4 p-2 bg-red-100 text-red-700 rounded text-center font-medium">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block mb-1 font-semibold" htmlFor="description">
              {t("description")}
            </label>
            <input
              id="description"
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              placeholder={t("enterTrainingDescription")}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 font-semibold" htmlFor="startTime">
                {t("startTime") || "Start time"}
              </label>
              <input
                id="startTime"
                type="datetime-local"
                value={startTime}
                onChange={(e) => {
                  const val = e.target.value;
                  setStartTime(val);
                  // jeśli end ustawiony i przypadkiem <= start, wyczyść end
                  if (
                    endTime &&
                    dayjs(endTime).valueOf() <= dayjs(val).valueOf()
                  ) {
                    setEndTime("");
                  }
                }}
                required
                className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
              />
            </div>

            <div>
              <label className="block mb-1 font-semibold" htmlFor="endTime">
                {t("endTime") || "End time"}
              </label>
              <input
                id="endTime"
                type="datetime-local"
                value={endTime}
                min={startTime || undefined} // prosty guard po stronie UI
                onChange={(e) => setEndTime(e.target.value)}
                required
                className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
              />
              {!!startTime &&
                !!endTime &&
                dayjs(endTime).valueOf() <= dayjs(startTime).valueOf() && (
                  <p className="text-xs text-red-600 mt-1">
                    {t("endMustBeAfterStart") ||
                      "End time must be after start time."}
                  </p>
                )}
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full px-4 py-3 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-green-300 transition font-semibold"
          >
            {loading
              ? t("creating") || "Creating..."
              : t("createTraining") || "Create training"}
          </button>
        </form>
      </div>

      <div className="md:col-span-3 bg-white rounded shadow p-6">
        <h2 className="text-xl font-semibold mb-4">
          {t("recentCreatedTrainings")}
        </h2>
        {trainings.length === 0 ? (
          <p className="text-gray-500 text-sm">{t("noTrainingsYet")}</p>
        ) : (
          <ul className="space-y-2 text-sm max-h-[400px] overflow-y-auto">
            {trainings
              .slice(-5)
              .reverse()
              .map((tr, idx) => (
                <li
                  key={tr.id ?? `${tr.startTime}-${idx}`}
                  className="p-2 border border-gray-300 rounded bg-gray-50"
                >
                  <span className="font-medium">
                    {dayjs(tr.startTime).format("MMM DD, YYYY HH:mm")} –{" "}
                    {dayjs(tr.endTime).format("HH:mm")}
                  </span>{" "}
                  – {tr.description}
                </li>
              ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default CreateTraining;

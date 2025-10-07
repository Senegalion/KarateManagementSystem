import { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import { API } from "../api";
import { useTranslation } from "react-i18next";
import { useEnrollments } from "../hooks/useEnrollments";
import { useNavigate } from "react-router-dom";

type Training = {
  id: number;
  description: string;
  startTime: string;
  endTime?: string;
};

const toTrainings = (arr: any[]): Training[] => {
  if (!Array.isArray(arr)) return [];
  return arr
    .filter((t) => t && typeof t.description === "string")
    .map((t) => {
      const id = (t.id as number) ?? (t.trainingSessionId as number);
      const startTime = (t.startTime as string) ?? (t.date as string) ?? "";
      const endTime = (t.endTime as string) ?? undefined;
      return { id, description: t.description as string, startTime, endTime };
    })
    .filter((t) => !!t.id && !!t.startTime) as Training[];
};

const MyTrainings = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const {
    data: enrollments,
    loading: enrollLoading,
    unenroll,
  } = useEnrollments();

  const [allTrainings, setAllTrainings] = useState<Training[]>([]);
  const [trainLoading, setTrainLoading] = useState(true);
  const [trainError, setTrainError] = useState<string | null>(null);

  useEffect(() => {
    setTrainLoading(true);
    setTrainError(null);
    API.get("/trainings")
      .then((res) => setAllTrainings(toTrainings(res.data)))
      .catch(() =>
        setTrainError(t("failedToLoadTrainings") || "Failed to load trainings")
      )
      .finally(() => setTrainLoading(false));
  }, [t]);

  const byId = useMemo(() => {
    const m = new Map<number, Training>();
    allTrainings.forEach((tr) => m.set(tr.id, tr));
    return m;
  }, [allTrainings]);

  const myTrainings: Training[] = useMemo(() => {
    return enrollments
      .filter((e) => e.status !== "CANCELLED")
      .map((e) => byId.get(e.trainingId))
      .filter(Boolean) as Training[];
  }, [enrollments, byId]);

  const now = dayjs();
  const upcoming = myTrainings
    .filter((t) => dayjs(t.startTime).isAfter(now))
    .sort((a, b) => dayjs(a.startTime).diff(dayjs(b.startTime)));

  const past = myTrainings
    .filter((t) => dayjs(t.startTime).isBefore(now))
    .sort((a, b) => dayjs(b.startTime).diff(dayjs(a.startTime)));

  const loading = enrollLoading || trainLoading;

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">{t("myTrainings")}</h1>

      {loading ? (
        <p>{t("loading")}...</p>
      ) : trainError ? (
        <p className="text-red-600">{trainError}</p>
      ) : (
        <>
          <section className="bg-white border rounded-xl p-5 mb-6">
            <h2 className="text-xl font-semibold mb-3">{t("upcoming")}</h2>
            {upcoming.length === 0 ? (
              <p className="text-gray-500 text-sm">{t("noTrainings")}</p>
            ) : (
              <ul className="space-y-2">
                {upcoming.map((tr) => (
                  <li
                    key={tr.id}
                    className="p-3 border rounded flex items-center justify-between"
                  >
                    <div>
                      <div className="font-medium">{tr.description}</div>
                      <div className="text-xs text-gray-500">
                        {dayjs(tr.startTime).format("MMM DD, YYYY HH:mm")}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button
                        className="px-3 py-1 text-sm border rounded hover:bg-gray-50"
                        onClick={() => navigate("/app/calendar")}
                        title={t("openCalendar")}
                      >
                        {t("openCalendar")}
                      </button>
                      <button
                        className="px-3 py-1 text-sm rounded bg-red-600 text-white hover:bg-red-700"
                        onClick={() => unenroll(tr.id)}
                      >
                        {t("unenroll")}
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="bg-white border rounded-xl p-5">
            <h2 className="text-xl font-semibold mb-3">{t("pastTrainings")}</h2>
            {past.length === 0 ? (
              <p className="text-gray-500 text-sm">{t("noPastTrainings")}</p>
            ) : (
              <ul className="space-y-2">
                {past.map((tr) => (
                  <li key={tr.id} className="p-3 border rounded">
                    <div className="font-medium">{tr.description}</div>
                    <div className="text-xs text-gray-500">
                      {dayjs(tr.startTime).format("MMM DD, YYYY HH:mm")}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  );
};

export default MyTrainings;

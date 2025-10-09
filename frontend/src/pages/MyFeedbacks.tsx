import { useEffect, useState } from "react";
import { API } from "../api";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import type { Feedback } from "../hooks/useFeedbacks";

type Row = {
  trainingSessionId: number;
  startTime?: string;
  description?: string;
} & Partial<Feedback>;

const MyFeedbacks = () => {
  const { t } = useTranslation();
  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        try {
          const { data } = await API.get<Array<Row>>("/feedbacks/me");
          setRows(data);
          return;
        } catch {
          const [enr, allTr] = await Promise.all([
            API.get("/enrollments/me"),
            API.get("/trainings"),
          ]);
          const byId = new Map<number, any>();
          (allTr.data as any[]).forEach((t) => {
            const id = t.id ?? t.trainingSessionId;
            if (id) byId.set(id, t);
          });

          const uniqTrainingIds = Array.from(
            new Set(
              (enr.data as any[])
                .map((e) => e.training?.trainingSessionId ?? e.trainingId)
                .filter(Boolean)
            )
          ) as number[];

          const results: Row[] = [];
          for (const tid of uniqTrainingIds) {
            try {
              const { data } = await API.get<Feedback>(`/feedbacks/${tid}`);
              const tInfo = byId.get(tid) ?? {};
              results.push({
                trainingSessionId: tid,
                description: tInfo.description,
                startTime: tInfo.startTime ?? tInfo.date,
                comment: data.comment,
                starRating: data.starRating,
              });
            } catch {}
          }
          setRows(
            results.sort(
              (a, b) =>
                dayjs(b.startTime).valueOf() - dayjs(a.startTime).valueOf()
            )
          );
        }
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">{t("myFeedbacks")}</h1>
      {loading ? (
        <p>{t("loading")}...</p>
      ) : rows.length === 0 ? (
        <p className="text-gray-500">{t("noFeedbacksYet")}</p>
      ) : (
        <div className="space-y-3">
          {rows.map((r) => (
            <div
              key={r.trainingSessionId}
              className="border rounded-xl p-4 bg-white"
            >
              <div className="flex items-center justify-between">
                <div className="font-semibold">
                  {r.description ?? `#${r.trainingSessionId}`}
                </div>
                <div className="text-sm text-gray-500">
                  {r.startTime
                    ? dayjs(r.startTime).format("YYYY-MM-DD HH:mm")
                    : ""}
                </div>
              </div>
              <div className="mt-2 text-lg">
                {r.starRating
                  ? "★".repeat(r.starRating) + "☆".repeat(5 - r.starRating)
                  : "—"}
              </div>
              <p className="mt-1 text-gray-800 whitespace-pre-wrap">
                {r.comment ?? t("noFeedbackForThisTraining")}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
export default MyFeedbacks;

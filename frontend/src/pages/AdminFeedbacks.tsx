import { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { API } from "../api";
import {
  listFeedbacksByTraining,
  listFeedbacksByUser,
  createFeedbackForUserAndSession,
  getFeedbackForUserAndSession,
  type Feedback,
  type FeedbackExt,
} from "../hooks/useFeedbacks";

type Training = { id: number; description: string; startTime: string };
type ClubUser = {
  userId: number;
  username?: string;
  email: string;
  karateRank?: string;
};
type EnrollmentRow = {
  userId: number;
  email: string;
  karateRank?: string;
  enrolledAt?: string;
};

const mapTrainings = (arr: any[]): Training[] =>
  (Array.isArray(arr) ? arr : [])
    .map((t) => ({
      id: (t.id as number) ?? (t.trainingSessionId as number),
      description: String(t.description ?? ""),
      startTime: String(t.startTime ?? t.date ?? ""),
    }))
    .filter((t) => t.id && t.startTime);

export default function AdminFeedbacks() {
  const { t } = useTranslation();

  const [trainings, setTrainings] = useState<Training[]>([]);
  const [users, setUsers] = useState<ClubUser[]>([]);
  const [loadingLists, setLoadingLists] = useState(true);

  const [trainingSel, setTrainingSel] = useState<number | "">("");
  const [participants, setParticipants] = useState<EnrollmentRow[]>([]);
  const [loadingPart, setLoadingPart] = useState(false);
  const [fbModal, setFbModal] = useState<null | {
    userId: number;
    trainingId: number;
  }>(null);
  const [fbForm, setFbForm] = useState<Feedback>({
    comment: "",
    starRating: 5,
  });
  const [fbSaving, setFbSaving] = useState(false);
  const [fbLoadErr, setFbLoadErr] = useState<string | null>(null);

  const [byTrainingSel, setByTrainingSel] = useState<number | "">("");
  const [byUserSel, setByUserSel] = useState<number | "">("");
  const [listByTraining, setListByTraining] = useState<FeedbackExt[]>([]);
  const [listByUser, setListByUser] = useState<FeedbackExt[]>([]);
  const [loadA, setLoadA] = useState(false);
  const [loadB, setLoadB] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setLoadingLists(true);
        const clubName = localStorage.getItem("selectedClub");
        const [trRes, usersRes] = await Promise.all([
          API.get("/trainings"),
          clubName
            ? API.get(`/users/by-club?clubName=${encodeURIComponent(clubName)}`)
            : Promise.resolve({ data: [] }),
        ]);
        setTrainings(mapTrainings(trRes.data));
        setUsers(
          (usersRes.data as any[]).map((u) => ({
            userId: u.userId,
            username: u.username,
            email: u.email,
            karateRank: u.karateRank,
          }))
        );
      } finally {
        setLoadingLists(false);
      }
    })();
  }, []);

  const trainingsOptions = useMemo(
    () =>
      trainings
        .slice()
        .sort(
          (a, b) => dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf()
        ),
    [trainings]
  );

  const loadParticipants = async () => {
    if (!trainingSel) return;
    setLoadingPart(true);
    try {
      const res = await API.get(`/enrollments/training/${trainingSel}`);
      const mapped = (Array.isArray(res.data) ? res.data : []).map(
        (e: any) => ({
          userId: e.user?.userId ?? 0,
          email: e.user?.email ?? "",
          karateRank: e.user?.karateRank,
          enrolledAt: e.enrolledAt,
        })
      ) as EnrollmentRow[];
      setParticipants(mapped);
    } finally {
      setLoadingPart(false);
    }
  };

  const openFbModal = async (userId: number, trainingId: number) => {
    setFbLoadErr(null);
    setFbForm({ comment: "", starRating: 5 });
    setFbModal({ userId, trainingId });
    try {
      const existing = await getFeedbackForUserAndSession(userId, trainingId);
      if (existing) setFbForm(existing);
    } catch (e) {}
  };
  const saveFeedback = async () => {
    if (!fbModal) return;
    try {
      setFbSaving(true);
      await createFeedbackForUserAndSession(
        fbModal.userId,
        fbModal.trainingId,
        fbForm
      );
      setFbModal(null);
      if (byTrainingSel === fbModal.trainingId) await handleLoadByTraining();
      if (byUserSel === fbModal.userId) await handleLoadByUser();
    } catch (e: any) {
      setFbLoadErr(e?.response?.data?.message ?? "Save failed");
    } finally {
      setFbSaving(false);
    }
  };

  const handleLoadByTraining = async () => {
    if (!byTrainingSel) return;
    setLoadA(true);
    try {
      setListByTraining(await listFeedbacksByTraining(byTrainingSel as number));
    } finally {
      setLoadA(false);
    }
  };
  const handleLoadByUser = async () => {
    if (!byUserSel) return;
    setLoadB(true);
    try {
      setListByUser(await listFeedbacksByUser(byUserSel as number));
    } finally {
      setLoadB(false);
    }
  };

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">⭐ {t("feedbacksAdmin")}</h1>

      <section className="bg-white border rounded-xl p-5 mb-8">
        <h2 className="text-xl font-semibold mb-4">{t("giveFeedback")}</h2>
        <div className="flex flex-col md:flex-row gap-3 mb-4">
          <select
            className="px-3 py-2 border rounded w-full md:w-2/3"
            disabled={loadingLists}
            value={trainingSel}
            onChange={(e) =>
              setTrainingSel(e.target.value ? Number(e.target.value) : "")
            }
          >
            <option value="">{t("selectTraining")}</option>
            {trainingsOptions.map((tr) => (
              <option key={tr.id} value={tr.id}>
                #{tr.id} • {dayjs(tr.startTime).format("YYYY-MM-DD HH:mm")} —{" "}
                {tr.description}
              </option>
            ))}
          </select>
          <button
            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-400"
            disabled={!trainingSel}
            onClick={loadParticipants}
          >
            {t("loadParticipants")}
          </button>
        </div>

        {loadingPart ? (
          <p>{t("loading")}...</p>
        ) : participants.length === 0 ? (
          <p className="text-gray-500">{t("noParticipants")}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border rounded-xl overflow-hidden">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">{t("userId")}</th>
                  <th className="p-2 text-left">{t("email")}</th>
                  <th className="p-2 text-left">{t("karateRank")}</th>
                  <th className="p-2 text-left"></th>
                </tr>
              </thead>
              <tbody>
                {participants.map((p) => (
                  <tr key={p.userId} className="border-t">
                    <td className="p-2">{p.userId}</td>
                    <td className="p-2">{p.email}</td>
                    <td className="p-2">{p.karateRank ?? "-"}</td>
                    <td className="p-2">
                      <button
                        className="px-3 py-1 rounded bg-emerald-600 text-white hover:bg-emerald-700"
                        onClick={() =>
                          openFbModal(p.userId, trainingSel as number)
                        }
                      >
                        {t("giveOrEditFeedback")}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="bg-white border rounded-xl p-5 mb-8">
        <h2 className="text-xl font-semibold mb-4">
          {t("feedbacksByTraining")}
        </h2>
        <div className="flex gap-3 mb-4">
          <select
            className="px-3 py-2 border rounded"
            value={byTrainingSel}
            onChange={(e) =>
              setByTrainingSel(e.target.value ? Number(e.target.value) : "")
            }
          >
            <option value="">{t("selectTraining")}</option>
            {trainingsOptions.map((tr) => (
              <option key={tr.id} value={tr.id}>
                #{tr.id} • {dayjs(tr.startTime).format("YYYY-MM-DD HH:mm")} —{" "}
                {tr.description}
              </option>
            ))}
          </select>
          <button
            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-400"
            disabled={!byTrainingSel}
            onClick={handleLoadByTraining}
          >
            {t("load")}
          </button>
        </div>
        {loadA ? (
          <p>{t("loading")}...</p>
        ) : listByTraining.length === 0 ? (
          <p className="text-gray-500">{t("noResults")}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border rounded-xl overflow-hidden">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">{t("feedbackId")}</th>
                  <th className="p-2 text-left">{t("userId")}</th>
                  <th className="p-2 text-left">{t("stars")}</th>
                  <th className="p-2 text-left">{t("comment")}</th>
                </tr>
              </thead>
              <tbody>
                {listByTraining.map((f) => (
                  <tr key={`ft-${f.feedbackId}`} className="border-t">
                    <td className="p-2">{f.feedbackId}</td>
                    <td className="p-2">{f.userId}</td>
                    <td className="p-2">⭐ {f.starRating}/5</td>
                    <td className="p-2">{f.comment}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="bg-white border rounded-xl p-5">
        <h2 className="text-xl font-semibold mb-4">{t("feedbacksByUser")}</h2>
        <div className="flex gap-3 mb-4">
          <select
            className="px-3 py-2 border rounded"
            value={byUserSel}
            onChange={(e) =>
              setByUserSel(e.target.value ? Number(e.target.value) : "")
            }
          >
            <option value="">{t("selectUser")}</option>
            {users
              .slice()
              .sort((a, b) => a.userId - b.userId)
              .map((u) => (
                <option key={u.userId} value={u.userId}>
                  #{u.userId} • {u.username ?? u.email}
                </option>
              ))}
          </select>
          <button
            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-400"
            disabled={!byUserSel}
            onClick={handleLoadByUser}
          >
            {t("load")}
          </button>
        </div>
        {loadB ? (
          <p>{t("loading")}...</p>
        ) : listByUser.length === 0 ? (
          <p className="text-gray-500">{t("noResults")}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border rounded-xl overflow-hidden">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">{t("feedbackId")}</th>
                  <th className="p-2 text-left">{t("trainingId")}</th>
                  <th className="p-2 text-left">{t("stars")}</th>
                  <th className="p-2 text-left">{t("comment")}</th>
                </tr>
              </thead>
              <tbody>
                {listByUser.map((f) => (
                  <tr key={`fu-${f.feedbackId}`} className="border-t">
                    <td className="p-2">{f.feedbackId}</td>
                    <td className="p-2">{f.trainingSessionId}</td>
                    <td className="p-2">⭐ {f.starRating}/5</td>
                    <td className="p-2">{f.comment}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {fbModal && (
        <div className="fixed inset-0 bg-black/20 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl p-5 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-3">
              {t("giveOrEditFeedback")}
            </h3>

            <label className="block text-sm mb-1">{t("stars")} (1–5)</label>
            <input
              type="number"
              min={1}
              max={5}
              className="w-24 p-2 border rounded mb-3"
              value={fbForm.starRating}
              onChange={(e) =>
                setFbForm((f) => ({
                  ...f,
                  starRating: Math.max(
                    1,
                    Math.min(5, Number(e.target.value) || 1)
                  ),
                }))
              }
            />

            <label className="block text-sm mb-1">{t("comment")}</label>
            <textarea
              className="w-full p-2 border rounded h-24"
              value={fbForm.comment}
              onChange={(e) =>
                setFbForm((f) => ({ ...f, comment: e.target.value }))
              }
            />

            {fbLoadErr && (
              <p className="text-red-600 text-sm mt-2">{fbLoadErr}</p>
            )}

            <div className="mt-4 flex gap-2 justify-end">
              <button
                className="px-3 py-1 rounded border"
                onClick={() => setFbModal(null)}
              >
                {t("cancel")}
              </button>
              <button
                className="px-4 py-1.5 rounded bg-emerald-600 text-white hover:bg-emerald-700 disabled:bg-gray-400"
                disabled={fbSaving || !fbForm.comment.trim()}
                onClick={saveFeedback}
              >
                {fbSaving ? t("saving") : t("save")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

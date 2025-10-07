import { useEffect, useMemo, useState } from "react";
import { API } from "../api";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

type Training = {
  id: number;
  description: string;
  startTime: string;
  endTime?: string;
};

type ClubUser = {
  userId: number;
  username?: string;
  email: string;
  karateRank?: string;
};

type EnrollmentRow = {
  enrollmentId: number;
  userId: number;
  email: string;
  karateRank?: string;
  trainingId: number;
  description: string;
  startTime: string;
  endTime?: string;
  enrolledAt?: string;
};

const mapTrainings = (arr: any[]): Training[] =>
  (Array.isArray(arr) ? arr : [])
    .filter((t) => t && typeof t.description === "string")
    .map((t) => ({
      id: (t.id as number) ?? (t.trainingSessionId as number),
      description: t.description as string,
      startTime: (t.startTime as string) ?? (t.date as string) ?? "",
      endTime: t.endTime as string | undefined,
    }))
    .filter((t) => !!t.id && !!t.startTime);

const mapUsers = (arr: any[]): ClubUser[] =>
  (Array.isArray(arr) ? arr : []).map((u: any) => ({
    userId: u.userId as number,
    username: u.username as string | undefined,
    email: u.email as string,
    karateRank: u.karateRank as string | undefined,
  }));

const mapEnrollments = (arr: any[]): EnrollmentRow[] =>
  (Array.isArray(arr) ? arr : [])
    .map((e: any) => {
      const training = e.training ?? {};
      const user = e.user ?? {};
      const trainingId =
        (training.trainingSessionId as number) ??
        (e.trainingId as number) ??
        undefined;
      const enrollmentId = e.enrollmentId as number | undefined;

      if (!trainingId || !enrollmentId) return null;

      return {
        enrollmentId,
        userId: (user.userId as number) ?? 0,
        email: (user.email as string) ?? "",
        karateRank: user.karateRank as string | undefined,
        trainingId,
        description: (training.description as string) ?? "",
        startTime: (training.startTime as string) ?? "",
        endTime: training.endTime as string | undefined,
        enrolledAt: e.enrolledAt as string | undefined,
      } as EnrollmentRow;
    })
    .filter(Boolean) as EnrollmentRow[];

const AdminEnrollments = () => {
  const { t } = useTranslation();

  const [trainings, setTrainings] = useState<Training[]>([]);
  const [users, setUsers] = useState<ClubUser[]>([]);
  const [loadingLists, setLoadingLists] = useState(true);

  const [trainingIdSel, setTrainingIdSel] = useState<number | "">("");
  const [userIdSel, setUserIdSel] = useState<number | "">("");

  const [byTraining, setByTraining] = useState<EnrollmentRow[]>([]);
  const [byUser, setByUser] = useState<EnrollmentRow[]>([]);
  const [loadingTraining, setLoadingTraining] = useState(false);
  const [loadingUser, setLoadingUser] = useState(false);
  const [errorTraining, setErrorTraining] = useState<string | null>(null);
  const [errorUser, setErrorUser] = useState<string | null>(null);

  useEffect(() => {
    const loadLists = async () => {
      try {
        setLoadingLists(true);

        const [trRes, usersRes] = await Promise.all([
          API.get("/trainings"),
          (async () => {
            const clubName = localStorage.getItem("selectedClub");
            if (clubName) {
              return API.get(
                `/users/by-club?clubName=${encodeURIComponent(clubName)}`
              );
            }
            return { data: [] };
          })(),
        ]);

        setTrainings(mapTrainings(trRes.data));
        setUsers(mapUsers(usersRes.data));
      } catch {
      } finally {
        setLoadingLists(false);
      }
    };
    loadLists();
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

  const usersOptions = useMemo(
    () =>
      users
        .slice()
        .sort((a, b) =>
          a.userId < b.userId ? -1 : a.userId > b.userId ? 1 : 0
        ),
    [users]
  );

  const loadByTraining = async () => {
    if (!trainingIdSel) return;
    try {
      setErrorTraining(null);
      setLoadingTraining(true);
      const res = await API.get(`/enrollments/training/${trainingIdSel}`);
      setByTraining(mapEnrollments(res.data));
    } catch {
      setErrorTraining(t("failedToLoad") || "Failed to load");
    } finally {
      setLoadingTraining(false);
    }
  };

  const loadByUser = async () => {
    if (!userIdSel) return;
    try {
      setErrorUser(null);
      setLoadingUser(true);
      const res = await API.get(`/enrollments/user/${userIdSel}`);
      setByUser(mapEnrollments(res.data));
    } catch {
      setErrorUser(t("failedToLoad") || "Failed to load");
    } finally {
      setLoadingUser(false);
    }
  };

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">{t("enrollmentsAdmin")}</h1>

      <section className="bg-white border rounded-xl p-5 mb-8">
        <h2 className="text-xl font-semibold mb-4">{t("byTraining")}</h2>

        <div className="flex flex-col md:flex-row gap-3 mb-4">
          <select
            className="px-3 py-2 border rounded w-full md:w-2/3"
            disabled={loadingLists}
            value={trainingIdSel}
            onChange={(e) =>
              setTrainingIdSel(e.target.value ? Number(e.target.value) : "")
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
            disabled={!trainingIdSel || loadingTraining}
            onClick={loadByTraining}
          >
            {t("load")}
          </button>
        </div>

        {loadingTraining ? (
          <p>{t("loading")}...</p>
        ) : errorTraining ? (
          <p className="text-red-600">{errorTraining}</p>
        ) : byTraining.length === 0 ? (
          <p className="text-gray-500">{t("noResults")}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border rounded-xl overflow-hidden">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">{t("enrollmentId")}</th>
                  <th className="p-2 text-left">{t("userId")}</th>
                  <th className="p-2 text-left">{t("email")}</th>
                  <th className="p-2 text-left">{t("karateRank")}</th>
                  <th className="p-2 text-left">{t("enrolledAt")}</th>
                </tr>
              </thead>
              <tbody>
                {byTraining.map((row) => (
                  <tr key={`tr-${row.enrollmentId}`} className="border-t">
                    <td className="p-2">{row.enrollmentId}</td>
                    <td className="p-2">{row.userId}</td>
                    <td className="p-2">{row.email}</td>
                    <td className="p-2">{row.karateRank ?? "-"}</td>
                    <td className="p-2">
                      {row.enrolledAt
                        ? dayjs(row.enrolledAt).format("YYYY-MM-DD HH:mm")
                        : "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="bg-white border rounded-xl p-5">
        <h2 className="text-xl font-semibold mb-4">{t("byUser")}</h2>

        <div className="flex flex-col md:flex-row gap-3 mb-4">
          <select
            className="px-3 py-2 border rounded w-full md:w-2/3"
            disabled={loadingLists}
            value={userIdSel}
            onChange={(e) =>
              setUserIdSel(e.target.value ? Number(e.target.value) : "")
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
            disabled={!userIdSel || loadingUser}
            onClick={loadByUser}
          >
            {t("load")}
          </button>
        </div>

        {loadingUser ? (
          <p>{t("loading")}...</p>
        ) : errorUser ? (
          <p className="text-red-600">{errorUser}</p>
        ) : byUser.length === 0 ? (
          <p className="text-gray-500">{t("noResults")}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm border rounded-xl overflow-hidden">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">{t("enrollmentId")}</th>
                  <th className="p-2 text-left">{t("trainingId")}</th>
                  <th className="p-2 text-left">{t("description")}</th>
                  <th className="p-2 text-left">{t("startTime")}</th>
                  <th className="p-2 text-left">{t("endTime")}</th>
                  <th className="p-2 text-left">{t("enrolledAt")}</th>
                </tr>
              </thead>
              <tbody>
                {byUser.map((row) => (
                  <tr key={`u-${row.enrollmentId}`} className="border-t">
                    <td className="p-2">{row.enrollmentId}</td>
                    <td className="p-2">{row.trainingId}</td>
                    <td className="p-2">{row.description}</td>
                    <td className="p-2">
                      {row.startTime
                        ? dayjs(row.startTime).format("YYYY-MM-DD HH:mm")
                        : "-"}
                    </td>
                    <td className="p-2">
                      {row.endTime
                        ? dayjs(row.endTime).format("YYYY-MM-DD HH:mm")
                        : "-"}
                    </td>
                    <td className="p-2">
                      {row.enrolledAt
                        ? dayjs(row.enrolledAt).format("YYYY-MM-DD HH:mm")
                        : "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default AdminEnrollments;

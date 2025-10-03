import { useEffect, useState, useContext } from "react";
import { API } from "../api";
import dayjs from "dayjs";
import { SearchContext } from "../context/SearchContext";
import { isAdmin } from "../utils/auth";
import { useTranslation } from "react-i18next";

type Training = {
  id?: number;
  description: string;
  startTime: string;
  endTime?: string;
};

const Dashboard = () => {
  const { t } = useTranslation();
  const [trainings, setTrainings] = useState<Training[]>([]);
  const searchContext = useContext(SearchContext);
  const search = searchContext?.search ?? "";

  useEffect(() => {
    API.get("/trainings", {
      headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
    })
      .then((res) => {
        const data = res.data;
        if (!Array.isArray(data)) return;

        const mapped: Training[] = data
          .filter((x) => x && typeof x.description === "string")
          .map((x) => ({
            id:
              (x.id as number) ?? (x.trainingSessionId as number) ?? undefined,
            description: x.description as string,
            startTime: (x.startTime as string) ?? (x.date as string) ?? "",
            endTime: (x.endTime as string) ?? undefined,
          }))
          .filter((x) => !!x.startTime);

        setTrainings(mapped);
      })
      .catch((err) => {
        console.error("Failed to load trainings", err);
      });
  }, []);

  const filteredTrainings = trainings.filter((t) =>
    t.description.toLowerCase().includes(search.toLowerCase())
  );

  const now = dayjs();

  const upcoming = filteredTrainings
    .filter((t) => dayjs(t.startTime).valueOf() > now.valueOf())
    .sort((a, b) => dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf())
    .slice(0, 5);

  const past = filteredTrainings
    .filter((t) => dayjs(t.startTime).valueOf() <= now.valueOf())
    .sort(
      (a, b) => dayjs(b.startTime).valueOf() - dayjs(a.startTime).valueOf()
    );

  const last = past[0];

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">{t("dashboard")}</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div className="bg-white border shadow-sm rounded-xl p-5">
          <h2 className="text-xl font-semibold mb-3">{t("quickOverview")}</h2>
          <ul className="text-sm text-gray-700 space-y-1">
            <li>
              📊 {t("totalTrainings")}: {filteredTrainings.length}
            </li>
            <li>
              ⏭️ {t("upcoming")}: {upcoming.length}
            </li>
            <li>
              ⏪ {t("lastSession")}:{" "}
              {last
                ? `${dayjs(last.startTime).format("MMM DD, YYYY HH:mm")}${
                    last.endTime
                      ? ` – ${dayjs(last.endTime).format("HH:mm")}`
                      : ""
                  }`
                : t("none")}
            </li>
          </ul>
        </div>

        <div className="bg-white border shadow-sm rounded-xl p-5">
          <h2 className="text-xl font-semibold mb-3">{t("shortcuts")}</h2>
          <div className="flex flex-col gap-2">
            <a
              href="/app/calendar"
              className="px-4 py-2 bg-blue-600 text-white rounded-lg text-center hover:bg-blue-700 transition"
            >
              📅 {t("openCalendar")}
            </a>
            {isAdmin() && (
              <a
                href="/app/trainings/new"
                className="px-4 py-2 bg-green-600 text-white rounded-lg text-center hover:bg-green-700 transition"
              >
                ➕ {t("addNewTraining")}
              </a>
            )}
          </div>
        </div>
      </div>

      <div className="bg-white border shadow-sm rounded-xl p-5 mb-6">
        <h2 className="text-xl font-semibold mb-4">{t("upcomingTrainings")}</h2>
        {upcoming.length === 0 ? (
          <p className="text-gray-500 text-sm">{t("noTrainings")}</p>
        ) : (
          <ul className="space-y-2 text-sm">
            {upcoming.map((t, idx) => (
              <li
                key={t.id ?? `${t.startTime}-${idx}`}
                className="p-2 bg-blue-50 border border-blue-200 rounded"
              >
                <span className="font-medium">
                  {dayjs(t.startTime).format("MMM DD, YYYY HH:mm")}
                  {t.endTime ? ` – ${dayjs(t.endTime).format("HH:mm")}` : ""}
                </span>{" "}
                – {t.description}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="bg-white border shadow-sm rounded-xl p-5">
        <h2 className="text-xl font-semibold mb-4">{t("pastTrainings")}</h2>
        {past.length === 0 ? (
          <p className="text-gray-500 text-sm">{t("noPastTrainings")}</p>
        ) : (
          <ul className="space-y-2 text-sm">
            {past.map((t, idx) => (
              <li
                key={t.id ?? `${t.startTime}-${idx}`}
                className="p-2 bg-gray-50 border border-gray-300 rounded"
              >
                <span className="font-medium">
                  {dayjs(t.startTime).format("MMM DD, YYYY HH:mm")}
                  {t.endTime ? ` – ${dayjs(t.endTime).format("HH:mm")}` : ""}
                </span>{" "}
                – {t.description}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Dashboard;

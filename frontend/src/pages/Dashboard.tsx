import { useEffect, useState, useContext } from "react";
import { API } from "../api";
import dayjs from "dayjs";
import { SearchContext } from "../context/SearchContext";
import { isAdmin } from "../utils/auth";
import { useTranslation } from "react-i18next";

type Training = {
  id: number;
  description: string;
  date: string;
};

const Dashboard = () => {
  const { t } = useTranslation();
  const [trainings, setTrainings] = useState<Training[]>([]);
  const searchContext = useContext(SearchContext);
  const search = searchContext?.search ?? "";

  useEffect(() => {
    API.get("/users/trainings", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    }).then((res) => {
      setTrainings(res.data);
    });
  }, []);

  const filteredTrainings = trainings.filter((t) =>
    t.description.toLowerCase().includes(search.toLowerCase())
  );

  const now = dayjs();

  const upcoming = filteredTrainings
    .filter((t) => dayjs(t.date).isAfter(now))
    .sort((a, b) => dayjs(a.date).diff(dayjs(b.date)))
    .slice(0, 5);

  const past = filteredTrainings
    .filter((t) => dayjs(t.date).isBefore(now))
    .sort((a, b) => dayjs(b.date).diff(dayjs(a.date)));

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
              {last ? dayjs(last.date).format("MMM DD, YYYY HH:mm") : t("none")}
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
            {upcoming.map((t) => (
              <li
                key={t.id}
                className="p-2 bg-blue-50 border border-blue-200 rounded"
              >
                <span className="font-medium">
                  {dayjs(t.date).format("MMM DD, YYYY HH:mm")}
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
            {past.map((t) => (
              <li
                key={t.id}
                className="p-2 bg-gray-50 border border-gray-300 rounded"
              >
                <span className="font-medium">
                  {dayjs(t.date).format("MMM DD, YYYY HH:mm")}
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

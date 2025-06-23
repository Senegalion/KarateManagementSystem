import { useEffect, useState } from "react";
import dayjs from "dayjs";
import { API } from "../api";

type Training = {
  id: number;
  description: string;
  date: string;
};

const TrainingCalendar = () => {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [currentDate, setCurrentDate] = useState(dayjs());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const today = dayjs().format("YYYY-MM-DD");

  useEffect(() => {
    setLoading(true);
    setError(null);

    API.get("/users/trainings", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    })
      .then((res) => {
        const data = res.data;

        if (!Array.isArray(data)) {
          throw new Error("Invalid response format");
        }

        const validTrainings = data
          .filter(
            (t) =>
              t &&
              typeof t.trainingSessionId === "number" &&
              typeof t.description === "string" &&
              typeof t.date === "string"
          )
          .map((t) => ({
            id: t.trainingSessionId,
            description: t.description,
            date: t.date,
          }));

        setTrainings(validTrainings);
      })
      .catch((err) => {
        console.error("Failed to load trainings", err);
        setError("Failed to load trainings.");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const startOfMonth = currentDate.startOf("month");
  const endOfMonth = currentDate.endOf("month");
  const daysInMonth = endOfMonth.date();
  const startDay = startOfMonth.day();

  const days = [];

  for (let i = 0; i < startDay; i++) {
    days.push(<div key={`empty-${i}`} />);
  }

  for (let i = 1; i <= daysInMonth; i++) {
    const date = startOfMonth.add(i - 1, "day").format("YYYY-MM-DD");
    const isToday = date === today;

    const dayTrainings = trainings.filter(
      (t) => typeof t.date === "string" && t.date.startsWith(date)
    );

    days.push(
      <div
        key={i}
        className={`border p-2 rounded-xl shadow-sm h-32 overflow-y-auto bg-white relative transition-transform hover:scale-[1.01] ${
          isToday ? "bg-yellow-50 border-yellow-300 ring-2 ring-yellow-400" : ""
        }`}
      >
        <div className="text-sm font-bold mb-1">
          {i}
          {isToday && (
            <span className="ml-1 text-xs text-yellow-600 font-medium">
              (today)
            </span>
          )}
        </div>
        {dayTrainings.map((t) => (
          <div
            key={t.id}
            className="text-xs bg-blue-100 text-blue-800 rounded px-1 py-0.5 mb-1 truncate"
            title={t.description}
          >
            {t.description}
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="p-4 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">
        Training Calendar
      </h1>

      {loading ? (
        <p>Loading...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <>
          <div className="flex items-center justify-between mb-6">
            <button
              onClick={() => setCurrentDate(currentDate.subtract(1, "month"))}
              className="px-4 py-1.5 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition font-medium shadow-sm"
            >
              ← Prev
            </button>
            <h2 className="text-xl font-semibold text-gray-700">
              {currentDate.format("MMMM YYYY")}
            </h2>
            <button
              onClick={() => setCurrentDate(currentDate.add(1, "month"))}
              className="px-4 py-1.5 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition font-medium shadow-sm"
            >
              Next →
            </button>
          </div>

          <div className="grid grid-cols-7 gap-3 text-sm text-gray-600 mb-2">
            {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((d) => (
              <div key={d} className="font-bold text-center uppercase">
                {d}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-3">{days}</div>
        </>
      )}
    </div>
  );
};

export default TrainingCalendar;

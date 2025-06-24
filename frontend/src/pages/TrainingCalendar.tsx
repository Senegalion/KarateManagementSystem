import { useEffect, useState, useContext } from "react";
import dayjs from "dayjs";
import { API } from "../api";
import { SearchContext } from "../context/SearchContext";

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
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMounted, setModalMounted] = useState(false);
  const today = dayjs().format("YYYY-MM-DD");

  const searchContext = useContext(SearchContext);
  const search = searchContext?.search ?? "";

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

        if (!Array.isArray(data)) throw new Error("Invalid response format");

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

  // Filtrujemy wg search
  const filteredTrainings = trainings.filter((t) =>
    t.description.toLowerCase().includes(search.toLowerCase())
  );

  const handleMonthChange = (direction: "prev" | "next") => {
    setSelectedDate(null);
    setModalVisible(false);
    setModalMounted(false);
    setCurrentDate((prev) =>
      direction === "prev" ? prev.subtract(1, "month") : prev.add(1, "month")
    );
  };

  const handleDayClick = (date: string) => {
    if (selectedDate && selectedDate !== date) {
      setModalVisible(false);
      setModalMounted(false);
      setTimeout(() => {
        setSelectedDate(date);
        setModalMounted(true);
        setModalVisible(true);
      }, 200);
    } else {
      setSelectedDate(date);
      setModalMounted(true);
      setModalVisible(true);
    }
  };

  const closeModal = () => {
    setModalVisible(false);
    setTimeout(() => {
      setModalMounted(false);
      setSelectedDate(null);
    }, 200);
  };

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

    const dayTrainings = filteredTrainings.filter(
      (t) => typeof t.date === "string" && t.date.startsWith(date)
    );

    days.push(
      <div
        key={i}
        onClick={() => handleDayClick(date)}
        className={`border p-2 rounded-xl shadow-sm h-32 bg-white relative cursor-pointer transition-transform duration-200 hover:scale-[1.01] ${
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
        {dayTrainings.slice(0, 2).map((t) => (
          <div
            key={t.id}
            className="text-xs bg-blue-100 text-blue-800 rounded px-1 py-0.5 mb-1 truncate"
            title={t.description}
          >
            {t.description}
          </div>
        ))}
        {dayTrainings.length > 2 && (
          <div className="text-xs text-blue-500">
            +{dayTrainings.length - 2} more
          </div>
        )}
      </div>
    );
  }

  const selectedTrainings =
    selectedDate &&
    filteredTrainings.filter((t) => t.date.startsWith(selectedDate));

  return (
    <div className="p-4 relative">
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
              onClick={() => handleMonthChange("prev")}
              className="px-4 py-1.5 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition font-medium shadow-sm"
            >
              ← Prev
            </button>
            <h2 className="text-xl font-semibold text-gray-700">
              {currentDate.format("MMMM YYYY")}
            </h2>
            <button
              onClick={() => handleMonthChange("next")}
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

          <div
            key={currentDate.format("YYYY-MM")}
            className="grid grid-cols-7 gap-3 animate-fade-in"
          >
            {days}
          </div>
        </>
      )}

      {modalMounted && (
        <div className="fixed inset-0 z-50 flex items-center justify-center pointer-events-none">
          <div
            className={`bg-white border border-gray-300 rounded-2xl shadow-2xl p-6 w-full max-w-md pointer-events-auto transform transition-all duration-300 ${
              modalVisible
                ? "opacity-100 translate-y-0 animate-fade-in"
                : "opacity-0 translate-y-2"
            }`}
          >
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-800">
                {dayjs(selectedDate!).format("DD MMMM YYYY")}
              </h3>
              <button
                onClick={closeModal}
                className="text-sm text-gray-500 hover:text-red-500"
              >
                ✕
              </button>
            </div>
            <div className="space-y-2 max-h-80 overflow-y-auto">
              {selectedTrainings && selectedTrainings.length > 0 ? (
                selectedTrainings.map((t) => (
                  <div
                    key={t.id}
                    className="bg-blue-100 text-blue-900 rounded px-3 py-2 text-sm shadow-sm"
                  >
                    {t.description}
                  </div>
                ))
              ) : (
                <p className="text-sm text-gray-500">
                  No trainings on this day.
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrainingCalendar;

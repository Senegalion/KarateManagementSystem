import { useEffect, useState, useContext, type JSX } from "react";
import dayjs from "dayjs";
import { API } from "../api";
import { SearchContext } from "../context/SearchContext";
import { useTranslation } from "react-i18next";
import { useEnrollments } from "../hooks/useEnrollments";
import { isAdmin } from "../utils/auth";
import { useNavigate } from "react-router-dom";
import FeedbackModal from "../components/FeedbackModal";
import { useFeedback } from "../hooks/useFeedbacks";

type Training = {
  id?: number;
  description: string;
  startTime: string;
  endTime?: string;
};

const TrainingCalendar = () => {
  const { t } = useTranslation();
  const { isEnrolled, enroll, unenroll } = useEnrollments();
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

  const navigate = useNavigate();

  const { getForTraining, loading: fbLoading } = useFeedback();
  const [fbOpen, setFbOpen] = useState(false);
  const [fbData, setFbData] = useState<{
    comment: string;
    starRating: number;
  } | null>(null);
  const [fbNotFound, setFbNotFound] = useState(false);
  const [fbTitle, setFbTitle] = useState("");

  useEffect(() => {
    setLoading(true);
    setError(null);

    API.get("/trainings")
      .then((res) => {
        const data = res.data;
        if (!Array.isArray(data)) throw new Error("Invalid response format");

        const mapped: Training[] = data
          .filter((t) => t && typeof t.description === "string")
          .map((t) => ({
            id:
              (t.id as number) ?? (t.trainingSessionId as number) ?? undefined,
            description: t.description as string,
            startTime: (t.startTime as string) ?? (t.date as string) ?? "",
            endTime: (t.endTime as string) ?? undefined,
          }))
          .filter((t) => !!t.startTime);

        setTrainings(mapped);
      })
      .catch((err) => {
        console.error("Failed to load trainings", err);
        setError("Failed to load trainings.");
      })
      .finally(() => setLoading(false));
  }, []);

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

  const startOfMonth = currentDate.startOf("month");
  const endOfMonth = currentDate.endOf("month");
  const daysInMonth = endOfMonth.date();
  const startDay = startOfMonth.day();

  const days: JSX.Element[] = [];

  for (let i = 0; i < startDay; i++) {
    days.push(<div key={`empty-${i}`} />);
  }

  for (let i = 1; i <= daysInMonth; i++) {
    const date = startOfMonth.add(i - 1, "day").format("YYYY-MM-DD");
    const isToday = date === today;

    const dayTrainings = filteredTrainings.filter(
      (t) => dayjs(t.startTime).format("YYYY-MM-DD") === date
    );

    days.push(
      <div
        key={date}
        onClick={() => handleDayClick(date)}
        className={`border p-2 rounded-xl shadow-sm h-32 bg-white relative cursor-pointer transition-transform duration-200 hover:scale-[1.01] ${
          isToday ? "bg-yellow-50 border-yellow-300 ring-2 ring-yellow-400" : ""
        }`}
      >
        <div className="text-sm font-bold mb-1">
          {i}
          {isToday && (
            <span className="ml-1 text-xs text-yellow-600 font-medium">
              ({t("today")})
            </span>
          )}
        </div>

        {dayTrainings.slice(0, 2).map((tr, idx) => {
          const enrolled = !!tr.id && isEnrolled(tr.id);
          return (
            <div
              key={tr.id ?? `${tr.startTime}-${idx}`}
              className={
                "text-xs rounded px-1 py-0.5 mb-1 truncate " +
                (enrolled
                  ? "bg-green-100 text-green-800"
                  : "bg-blue-100 text-blue-800")
              }
              title={tr.description}
            >
              {tr.description} {enrolled && "✓"}
            </div>
          );
        })}

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
    filteredTrainings.filter(
      (t) => dayjs(t.startTime).format("YYYY-MM-DD") === selectedDate
    );

  return (
    <div className="p-4 relative">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">
        {t("trainingCalendar")}
      </h1>

      {loading ? (
        <p>{t("loading")}...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <>
          <div className="flex items-center justify-between mb-6">
            <button
              onClick={() => handleMonthChange("prev")}
              className="px-4 py-1.5 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition font-medium shadow-sm"
            >
              {t("prev")}
            </button>
            <h2 className="text-xl font-semibold text-gray-700">
              {currentDate.format("MMMM YYYY")}
            </h2>
            <button
              onClick={() => handleMonthChange("next")}
              className="px-4 py-1.5 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition font-medium shadow-sm"
            >
              {t("next")}
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

      {modalMounted && selectedDate && (
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
                {dayjs(selectedDate).format("DD MMMM YYYY")}
              </h3>
              <button
                onClick={() => {
                  setModalVisible(false);
                  setTimeout(() => {
                    setModalMounted(false);
                    setSelectedDate(null);
                  }, 200);
                }}
                className="text-sm text-gray-500 hover:text-red-500"
              >
                ✕
              </button>
            </div>
            <div className="space-y-2 max-h-80 overflow-y-auto">
              {selectedTrainings && selectedTrainings.length > 0 ? (
                selectedTrainings.map((tr, idx) => {
                  const enrolled = !!tr.id && isEnrolled(tr.id);
                  const isPast = dayjs(tr.startTime).isBefore(dayjs());
                  return (
                    <div
                      key={tr.id ?? `${tr.startTime}-${idx}`}
                      className="bg-blue-50 border border-blue-200 rounded px-3 py-2 text-sm shadow-sm flex items-center justify-between"
                    >
                      <div>
                        <div className="font-medium">
                          {dayjs(tr.startTime).format("HH:mm")} –{" "}
                          {tr.description}
                        </div>
                        {enrolled && (
                          <div className="text-xs text-green-700 mt-0.5">
                            ✓ {t("enrolled")}
                          </div>
                        )}
                      </div>
                      {tr.id && (
                        <div className="flex items-center gap-2">
                          <button
                            className={
                              "px-3 py-1 rounded text-white " +
                              (enrolled
                                ? "bg-red-600 hover:bg-red-700"
                                : "bg-green-600 hover:bg-green-700")
                            }
                            onClick={(e) => {
                              e.stopPropagation();
                              enrolled ? unenroll(tr.id!) : enroll(tr.id!);
                            }}
                          >
                            {enrolled ? t("unenroll") : t("enroll")}
                          </button>

                          {isPast && (
                            <button
                              className="px-3 py-1 text-sm rounded border hover:bg-gray-50"
                              onClick={async (e) => {
                                e.stopPropagation();
                                setFbTitle(
                                  `${dayjs(tr.startTime).format(
                                    "DD.MM.YYYY HH:mm"
                                  )} – ${tr.description}`
                                );
                                const res = await getForTraining(tr.id!);
                                setFbData(res.feedback);
                                setFbNotFound(!res.exists);
                                setFbOpen(true);
                              }}
                            >
                              {t("viewFeedback")}
                            </button>
                          )}

                          {isAdmin() && (
                            <button
                              className="px-3 py-1 text-sm rounded border hover:bg-gray-50"
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(
                                  `/app/enrollments?trainingId=${tr.id}`
                                );
                              }}
                            >
                              {t("viewParticipants")}
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })
              ) : (
                <p className="text-sm text-gray-500">
                  {t("noTrainingsOnThisDay")}
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      <FeedbackModal
        open={fbOpen}
        onClose={() => setFbOpen(false)}
        title={fbTitle}
        feedback={fbData}
        loading={fbLoading}
        notFound={fbNotFound}
      />
    </div>
  );
};

export default TrainingCalendar;

import { useState, useEffect, useMemo } from "react";
import { API } from "../api";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

type Training = {
  trainingSessionId: number;
  description: string;
  startTime: string;
  endTime: string;
};

type RecurringTrainingRequest = {
  fromDate: string;
  toDate: string;
  startTime: string;
  endTime: string;
  daysOfWeek: Array<
    | "MONDAY"
    | "TUESDAY"
    | "WEDNESDAY"
    | "THURSDAY"
    | "FRIDAY"
    | "SATURDAY"
    | "SUNDAY"
  >;
  description: string;
  skipConflicts: boolean;
};

const WEEKDAYS: Array<{ key: RecurringTrainingRequest["daysOfWeek"][number]; label: string }> =
  [
    { key: "MONDAY", label: "Mon" },
    { key: "TUESDAY", label: "Tue" },
    { key: "WEDNESDAY", label: "Wed" },
    { key: "THURSDAY", label: "Thu" },
    { key: "FRIDAY", label: "Fri" },
    { key: "SATURDAY", label: "Sat" },
    { key: "SUNDAY", label: "Sun" },
  ];

const CreateTraining = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [description, setDescription] = useState("");

  const [mode, setMode] = useState<"single" | "recurring">("single");

  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  const [fromDate, setFromDate] = useState(dayjs().format("YYYY-MM-DD"));
  const [toDate, setToDate] = useState(dayjs().add(8, "week").format("YYYY-MM-DD"));
  const [recStart, setRecStart] = useState("18:00");
  const [recEnd, setRecEnd] = useState("19:00");
  const [daysOfWeek, setDaysOfWeek] = useState<RecurringTrainingRequest["daysOfWeek"]>([
    "TUESDAY",
    "THURSDAY",
  ]);
  const [skipConflicts, setSkipConflicts] = useState(true);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [trainings, setTrainings] = useState<Training[]>([]);

  const authHeaders = useMemo(
    () => ({ Authorization: `Bearer ${localStorage.getItem("token")}` }),
    []
  );

  useEffect(() => {
    API.get("/trainings", { headers: authHeaders }).then((res) => setTrainings(res.data));
  }, [authHeaders]);

  const toggleDay = (d: RecurringTrainingRequest["daysOfWeek"][number]) => {
    setDaysOfWeek((prev) =>
      prev.includes(d) ? prev.filter((x) => x !== d) : [...prev, d]
    );
  };

  const validateSingle = () => {
    if (!startTime || !endTime) {
      return t("pleaseFillAllFields") || "Please fill all fields.";
    }
    const start = dayjs(startTime);
    const end = dayjs(endTime);
    if (!(end.valueOf() > start.valueOf())) {
      return t("endMustBeAfterStart") || "End time must be after start time.";
    }
    return null;
  };

  const validateRecurring = () => {
    if (!fromDate || !toDate || !recStart || !recEnd) {
      return t("pleaseFillAllFields") || "Please fill all fields.";
    }
    if (dayjs(toDate).isBefore(dayjs(fromDate), "day")) {
      return "toDate must be the same or after fromDate.";
    }
    const s = dayjs(`2000-01-01T${recStart}:00`);
    const e = dayjs(`2000-01-01T${recEnd}:00`);
    if (!(e.valueOf() > s.valueOf())) {
      return t("endMustBeAfterStart") || "End time must be after start time.";
    }
    if (daysOfWeek.length === 0) {
      return "Select at least one weekday.";
    }

    const maxDays = 366;
    const diffDays = dayjs(toDate).diff(dayjs(fromDate), "day") + 1;
    if (diffDays > maxDays) {
      return `Date range is too large (${diffDays} days). Limit it to ${maxDays} days.`;
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const validationError = mode === "single" ? validateSingle() : validateRecurring();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      if (mode === "single") {
        const start = dayjs(startTime);
        const end = dayjs(endTime);

        const payload = {
          description,
          startTime: start.format("YYYY-MM-DDTHH:mm:ss"),
          endTime: end.format("YYYY-MM-DDTHH:mm:ss"),
        };

        await API.post("/trainings/create", payload, { headers: authHeaders });
      } else {
        const payload: RecurringTrainingRequest = {
          description,
          fromDate,
          toDate,
          startTime: recStart,
          endTime: recEnd,
          daysOfWeek,
          skipConflicts,
        };

        await API.post("/trainings/create/recurring", payload, { headers: authHeaders });
      }

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

        <div className="mb-6 flex gap-2">
          <button
            type="button"
            onClick={() => setMode("single")}
            className={`flex-1 px-4 py-2 rounded font-semibold border ${
              mode === "single" ? "bg-gray-900 text-white" : "bg-white hover:bg-gray-50"
            }`}
          >
            Single
          </button>
          <button
            type="button"
            onClick={() => setMode("recurring")}
            className={`flex-1 px-4 py-2 rounded font-semibold border ${
              mode === "recurring" ? "bg-gray-900 text-white" : "bg-white hover:bg-gray-50"
            }`}
          >
            Recurring
          </button>
        </div>

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

          {mode === "single" ? (
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
                    if (endTime && dayjs(endTime).valueOf() <= dayjs(val).valueOf()) {
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
                  min={startTime || undefined}
                  onChange={(e) => setEndTime(e.target.value)}
                  required
                  className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
                />
                {!!startTime &&
                  !!endTime &&
                  dayjs(endTime).valueOf() <= dayjs(startTime).valueOf() && (
                    <p className="text-xs text-red-600 mt-1">
                      {t("endMustBeAfterStart") || "End time must be after start time."}
                    </p>
                  )}
              </div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1 font-semibold">From date</label>
                  <input
                    type="date"
                    value={fromDate}
                    onChange={(e) => setFromDate(e.target.value)}
                    className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
                  />
                </div>
                <div>
                  <label className="block mb-1 font-semibold">To date</label>
                  <input
                    type="date"
                    value={toDate}
                    min={fromDate || undefined}
                    onChange={(e) => setToDate(e.target.value)}
                    className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
                  />
                </div>
              </div>

              <div>
                <label className="block mb-2 font-semibold">Days of week</label>
                <div className="flex flex-wrap gap-2">
                  {WEEKDAYS.map((d) => (
                    <button
                      key={d.key}
                      type="button"
                      onClick={() => toggleDay(d.key)}
                      className={`px-3 py-2 rounded border text-sm font-semibold ${
                        daysOfWeek.includes(d.key)
                          ? "bg-blue-600 text-white"
                          : "bg-white hover:bg-gray-50"
                      }`}
                    >
                      {d.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1 font-semibold">Start time</label>
                  <input
                    type="time"
                    value={recStart}
                    onChange={(e) => {
                      const v = e.target.value;
                      setRecStart(v);
                      if (recEnd && dayjs(`2000-01-01T${recEnd}:00`).valueOf() <= dayjs(`2000-01-01T${v}:00`).valueOf()) {
                        setRecEnd("");
                      }
                    }}
                    className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
                  />
                </div>

                <div>
                  <label className="block mb-1 font-semibold">End time</label>
                  <input
                    type="time"
                    value={recEnd}
                    onChange={(e) => setRecEnd(e.target.value)}
                    className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
                  />
                </div>
              </div>

              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={skipConflicts}
                  onChange={(e) => setSkipConflicts(e.target.checked)}
                />
                Skip conflicts (if training already exists)
              </label>
            </>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full px-4 py-3 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-green-300 transition font-semibold"
          >
            {loading
              ? t("creating") || "Creating..."
              : mode === "single"
              ? t("createTraining") || "Create training"
              : "Create recurring trainings"}
          </button>
        </form>
      </div>

      <div className="md:col-span-3 bg-white rounded shadow p-6">
        <h2 className="text-xl font-semibold mb-4">{t("recentCreatedTrainings")}</h2>
        {trainings.length === 0 ? (
          <p className="text-gray-500 text-sm">{t("noTrainingsYet")}</p>
        ) : (
          <ul className="space-y-2 text-sm max-h-[400px] overflow-y-auto">
            {trainings
              .slice(-5)
              .reverse()
              .map((tr) => (
                <li
                  key={tr.trainingSessionId}
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

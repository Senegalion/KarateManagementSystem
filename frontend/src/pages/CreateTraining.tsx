import { useState, useEffect } from "react";
import { API } from "../api";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

type Training = {
  id: number;
  description: string;
  date: string;
};

const CreateTraining = () => {
  const { t } = useTranslation();
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [trainings, setTrainings] = useState<Training[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    API.get("/users/trainings", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    }).then((res) => {
      setTrainings(res.data);
    });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await API.post(
        "/users/trainings",
        { description, date },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      setLoading(false);
      navigate("/app/dashboard");
    } catch (err: any) {
      setLoading(false);
      setError(err.response?.data?.message || t("failedToCreateTraining"));
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

          <div>
            <label className="block mb-1 font-semibold" htmlFor="date">
              {t("dateAndTime")}
            </label>
            <input
              id="date"
              type="datetime-local"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-blue-300"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full px-4 py-3 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-green-300 transition font-semibold"
          >
            {loading ? t("creating") : t("createTraining")}
          </button>
        </form>
      </div>

      <div className="md:col-span-3 bg-white rounded shadow p-6">
        <h2 className="text-xl font-semibold mb-4">
          {t("recentCreatedTrainings")}
        </h2>
        {trainings.length === 0 ? (
          <p className="text-gray-500 text-sm">{t("noTrainingsYet")}</p>
        ) : (
          <ul className="space-y-2 text-sm max-h-[400px] overflow-y-auto">
            {trainings
              .slice(-5)
              .reverse()
              .map((t) => (
                <li
                  key={t.id}
                  className="p-2 border border-gray-300 rounded bg-gray-50"
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

export default CreateTraining;

import { useEffect, useState } from "react";
import { API } from "../api";

export type Enrollment = {
  trainingId: number;
  status?: "ACTIVE" | "CANCELLED";
  enrolledAt?: string;
};

type BackendEnrollment =
  | {
      enrollmentId: number;
      user?: { userId: number };
      training?: {
        trainingSessionId: number;
        startTime?: string;
        endTime?: string;
        description?: string;
      };
      enrolledAt?: string;
      status?: "ACTIVE" | "CANCELLED";
    }
  | {
      trainingId: number;
      enrolledAt?: string;
      status?: "ACTIVE" | "CANCELLED";
    };

const toClient = (arr: unknown): Enrollment[] => {
  if (!Array.isArray(arr)) return [];
  return (arr as BackendEnrollment[])
    .map((e) => {
      const flatId = (e as any).trainingId as number | undefined;

      const nestedId = (e as any)?.training?.trainingSessionId as
        | number
        | undefined;

      const trainingId = flatId ?? nestedId;
      if (!trainingId) return null;

      return {
        trainingId,
        status: (e as any).status ?? "ACTIVE",
        enrolledAt: (e as any).enrolledAt ?? undefined,
      } as Enrollment;
    })
    .filter(Boolean) as Enrollment[];
};

export const useEnrollments = () => {
  const [data, setData] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = () => {
    setLoading(true);
    setError(null);
    API.get("/enrollments/me")
      .then((res) => setData(toClient(res.data)))
      .catch(() => setError("Failed to load enrollments"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    refresh();
  }, []);

  const isEnrolled = (trainingId: number) =>
    data.some((e) => e.trainingId === trainingId && e.status !== "CANCELLED");

  const enroll = async (trainingId: number) => {
    await API.post(`/enrollments/me/${trainingId}`);
    refresh();
  };

  const unenroll = async (trainingId: number) => {
    await API.delete(`/enrollments/me/${trainingId}`);
    refresh();
  };

  return { data, loading, error, refresh, isEnrolled, enroll, unenroll };
};

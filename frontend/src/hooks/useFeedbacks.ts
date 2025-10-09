import { useState } from "react";
import { API } from "../api";

export type Feedback = { comment: string; starRating: number };
export type FeedbackExt = Feedback & {
  feedbackId: number;
  userId: number;
  trainingSessionId: number;
};

export const getMyFeedbackForSession = async (trainingSessionId: number) => {
  const res = await API.get<Feedback>(`/feedbacks/${trainingSessionId}`);
  return res.data;
};

export const getFeedbackForUserAndSession = async (
  userId: number,
  trainingSessionId: number
) => {
  const res = await API.get<Feedback>(
    `/feedbacks/admin/${userId}/${trainingSessionId}`
  );
  return res.data;
};

export const createFeedbackForUserAndSession = async (
  userId: number,
  trainingSessionId: number,
  payload: Feedback
) => {
  const res = await API.post<Feedback>(
    `/feedbacks/${userId}/${trainingSessionId}`,
    payload
  );
  return res.data;
};

export const listFeedbacksByUser = async (userId: number) => {
  const res = await API.get<FeedbackExt[]>(
    `/feedbacks/admin/by-user/${userId}`
  );
  return res.data;
};
export const listFeedbacksByTraining = async (trainingSessionId: number) => {
  const res = await API.get<FeedbackExt[]>(
    `/feedbacks/admin/by-training/${trainingSessionId}`
  );
  return res.data;
};

export const useFeedback = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getForTraining = async (trainingSessionId: number) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await API.get<Feedback>(
        `/feedbacks/${trainingSessionId}`
      );
      return { feedback: data, exists: true };
    } catch (e: any) {
      if (e?.response?.status === 404) return { feedback: null, exists: false };
      setError("Failed to load feedback");
      return { feedback: null, exists: false };
    } finally {
      setLoading(false);
    }
  };

  return { getForTraining, loading, error };
};

export const fetchMyFeedbacks = async () => {
  const { data } = await API.get<
    Array<{ trainingSessionId: number } & Feedback>
  >("/feedbacks/me");
  return data;
};

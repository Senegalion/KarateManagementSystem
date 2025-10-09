import React from "react";

type Props = {
  open: boolean;
  onClose: () => void;
  title: string;
  feedback?: { comment: string; starRating: number } | null;
  loading?: boolean;
  notFound?: boolean;
};

const FeedbackModal: React.FC<Props> = ({
  open,
  onClose,
  title,
  feedback,
  loading,
  notFound,
}) => {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/10">
      <div className="bg-white border rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold">{title}</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-red-500"
          >
            ✕
          </button>
        </div>

        {loading ? (
          <p>Loading…</p>
        ) : notFound ? (
          <p className="text-sm text-gray-600">
            Brak feedbacku dla tego treningu.
          </p>
        ) : feedback ? (
          <div className="space-y-3">
            <div className="text-xl">
              {"★".repeat(feedback.starRating)}
              {"☆".repeat(5 - feedback.starRating)}
            </div>
            <p className="text-gray-800 whitespace-pre-wrap">
              {feedback.comment}
            </p>
          </div>
        ) : (
          <p className="text-sm text-red-600">
            Nie udało się pobrać feedbacku.
          </p>
        )}
      </div>
    </div>
  );
};

export default FeedbackModal;

type Props = { rating: number; size?: "sm" | "md" };
export default function FeedbackStars({ rating, size = "sm" }: Props) {
  const s = size === "sm" ? "text-xs" : "text-sm";
  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-yellow-100 text-yellow-800 ${s}`}
    >
      ⭐ {rating}/5
    </span>
  );
}

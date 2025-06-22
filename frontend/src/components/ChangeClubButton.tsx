import { useNavigate } from "react-router-dom";

const ChangeClubButton = () => {
  const navigate = useNavigate();
  return (
    <button
      type="button"
      onClick={() => navigate("/login?changeClub=true")}
      className="w-full py-2 rounded-lg border-2 border-blue-600 text-blue-600 hover:bg-blue-100 transition"
    >
      Change Club
    </button>
  );
};

export default ChangeClubButton;

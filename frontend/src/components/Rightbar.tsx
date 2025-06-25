import { FaUserCircle, FaCog, FaSignOutAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

const Rightbar = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };
  return (
    <aside className="w-16 bg-white shadow-md h-screen sticky top-0 flex flex-col items-center gap-6 py-6">
      <button
        title="Profile"
        className="text-gray-600 hover:text-blue-600 text-xl"
      >
        <FaUserCircle />
      </button>
      <button
        title="Settings"
        className="text-gray-600 hover:text-blue-600 text-xl"
        onClick={() => navigate("/settings")}
      >
        <FaCog />
      </button>
      <button
        title="Logout"
        className="text-gray-600 hover:text-red-600 text-xl mt-auto mb-4"
        onClick={handleLogout}
      >
        <FaSignOutAlt />
      </button>
    </aside>
  );
};

export default Rightbar;

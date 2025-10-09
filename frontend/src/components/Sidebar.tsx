import { NavLink } from "react-router-dom";
import { isAdmin } from "../utils/auth";
import { useTranslation } from "react-i18next";

const Sidebar = () => {
  const { t } = useTranslation();
  const navItemClass = ({ isActive }: { isActive: boolean }) =>
    `block px-4 py-2 rounded transition ${
      isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-blue-100"
    }`;

  return (
    <aside className="w-64 bg-white p-4 shadow-md h-screen sticky top-0">
      <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
        🥋 {t("appName")}
      </h2>
      <nav className="space-y-2">
        <NavLink to="/app/dashboard" className={navItemClass}>
          {"🏠 " + t("dashboard")}
        </NavLink>
        {isAdmin() && (
          <NavLink to="/app/users" className={navItemClass}>
            {"👥 " + t("users")}
          </NavLink>
        )}
        <NavLink to="/app/calendar" className={navItemClass}>
          {"📅 " + t("calendar")}
        </NavLink>
        {isAdmin() && (
          <NavLink to="/app/trainings/new" className={navItemClass}>
            {"🆕 " + t("createTraining")}
          </NavLink>
        )}
        {isAdmin() && (
          <NavLink to="/app/enrollments" className={navItemClass}>
            {"📋 " + t("enrollmentsAdmin")}
          </NavLink>
        )}
        <NavLink to="/app/my-trainings" className={navItemClass}>
          {"💪 " + t("myTrainings")}
        </NavLink>
        {isAdmin() && (
          <NavLink to="/app/feedbacks" className={navItemClass}>
            {"🗒️ " + t("feedbacksAdmin")}
          </NavLink>
        )}
        <NavLink to="/app/my-feedbacks" className={navItemClass}>
          {"⭐ " + t("myFeedbacks")}
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;

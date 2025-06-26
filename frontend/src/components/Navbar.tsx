import { Link, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

function Navbar() {
  const location = useLocation();
  const { t } = useTranslation();

  const linkClass = (path: string) =>
    `px-4 py-2 rounded-md font-medium ${
      location.pathname === path
        ? "bg-blue-600 text-white"
        : "text-blue-600 hover:bg-blue-100"
    }`;

  return (
    <nav className="w-full bg-white shadow-md p-4 mb-6">
      <div className="max-w-6xl mx-auto flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-blue-700">
          {t("appName")}
        </Link>

        <div className="space-x-2">
          <Link to="/" className={linkClass("/")}>
            {t("home")}
          </Link>
          <Link to="/login" className={linkClass("/login")}>
            {t("login")}
          </Link>
          <Link to="/register" className={linkClass("/register")}>
            {t("register")}
          </Link>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;

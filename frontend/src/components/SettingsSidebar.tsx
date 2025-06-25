import { NavLink } from "react-router-dom";

const SettingsSidebar = () => {
  return (
    <aside className="w-64 bg-white shadow-md h-screen sticky top-0 p-6">
      <h2 className="text-xl font-bold mb-6">Settings</h2>
      <nav className="flex flex-col gap-3">
        <NavLink
          to="/settings/language"
          className={({ isActive }) =>
            isActive
              ? "text-blue-600 font-semibold"
              : "text-gray-700 hover:text-blue-600"
          }
        >
          🌐 Language
        </NavLink>
        {/* Możesz dodać inne zakładki */}
      </nav>
    </aside>
  );
};

export default SettingsSidebar;

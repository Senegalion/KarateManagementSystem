import { NavLink } from "react-router-dom";

const Sidebar = () => {
  const navItemClass = ({ isActive }: { isActive: boolean }) =>
    `block px-4 py-2 rounded transition ${
      isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-blue-100"
    }`;

  return (
    <aside className="w-64 bg-white p-4 shadow-md h-screen sticky top-0">
      <h2 className="text-xl font-bold mb-4">Karate App</h2>
      <nav className="space-y-2">
        <NavLink to="/app/dashboard" className={navItemClass}>
          Dashboard
        </NavLink>
        <NavLink to="/app/calendar" className={navItemClass}>
          Calendar
        </NavLink>
        <NavLink to="/app/trainings/new" className={navItemClass}>
          Create Training
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;

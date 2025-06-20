import { Outlet } from "react-router-dom";
import Sidebar from "../components/Sidebar";

const AppLayout = () => {
  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <main className="flex-1 p-6">
        <Outlet />
      </main>
    </div>
  );
};

export default AppLayout;

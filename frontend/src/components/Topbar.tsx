import { useState } from "react";

const Topbar = () => {
  const [search, setSearch] = useState("");

  return (
    <header className="w-full bg-white shadow px-6 py-3 flex justify-between items-center sticky top-0 z-10">
      <div className="flex-1 flex justify-center">
        <input
          type="text"
          placeholder="Search features..."
          className="w-1/2 px-4 py-2 border rounded shadow-sm focus:outline-none focus:ring focus:ring-blue-300"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>
    </header>
  );
};

export default Topbar;

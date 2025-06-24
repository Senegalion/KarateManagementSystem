import { useState, useEffect, useRef } from "react";
import { useSearch } from "../context/SearchContext";
import { useNavigate } from "react-router-dom";

const pages = [
  { name: "Dashboard", path: "/app/dashboard" },
  { name: "Calendar", path: "/app/calendar" },
];

const Topbar = () => {
  const { search, setSearch } = useSearch();
  const [filtered, setFiltered] = useState<typeof pages>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const navigate = useNavigate();

  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLUListElement>(null);
  const [inputWidth, setInputWidth] = useState<number>(0);

  useEffect(() => {
    if (search.trim() === "") {
      setFiltered([]);
      setShowSuggestions(false);
      return;
    }

    const filt = pages.filter((p) =>
      p.name.toLowerCase().includes(search.toLowerCase())
    );

    setFiltered(filt);
    setShowSuggestions(filt.length > 0);
  }, [search]);

  useEffect(() => {
    if (inputRef.current) {
      setInputWidth(inputRef.current.offsetWidth);
    }
  }, [search]);

  const handleSelect = (path: string) => {
    navigate(path);
    setSearch("");
    setShowSuggestions(false);
  };

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        inputRef.current &&
        !inputRef.current.contains(e.target as Node) &&
        listRef.current &&
        !listRef.current.contains(e.target as Node)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  return (
    <header className="w-full bg-white shadow px-6 py-3 flex justify-between items-center sticky top-0 z-10">
      <div className="flex-1 flex justify-center relative">
        <input
          ref={inputRef}
          type="text"
          placeholder="Search features..."
          className="w-1/2 px-4 py-2 border rounded shadow-sm focus:outline-none focus:ring focus:ring-blue-300"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onFocus={() => {
            if (search.trim() === "") {
              setFiltered(pages);
              setShowSuggestions(true);
            }
            if (inputRef.current) {
              setInputWidth(inputRef.current.offsetWidth);
            }
          }}
        />
        {showSuggestions && (
          <ul
            ref={listRef}
            style={{ width: inputWidth }}
            className="absolute top-full mt-1 bg-white border rounded shadow-md max-h-48 overflow-auto z-20"
          >
            {filtered.map((item) => (
              <li
                key={item.path}
                onClick={() => handleSelect(item.path)}
                className="px-4 py-2 cursor-pointer hover:bg-blue-100"
              >
                {item.name}
              </li>
            ))}
          </ul>
        )}
      </div>
    </header>
  );
};

export default Topbar;

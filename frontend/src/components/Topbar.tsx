import { useState, useEffect, useRef, useMemo } from "react";
import { useSearch } from "../context/SearchContext";
import { isAdmin } from "../utils/auth";
import { useTranslation } from "react-i18next";

type Page = { name: string; path: string };

const Topbar = () => {
  const { t } = useTranslation();
  const { search, setSearch } = useSearch();
  const [inputValue, setInputValue] = useState(search);
  const [filtered, setFiltered] = useState<Page[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLUListElement>(null);
  const [inputWidth, setInputWidth] = useState<number>(0);

  const pages = useMemo<Page[]>(() => {
    const base: Page[] = [
      { name: t("dashboard"), path: "/app/dashboard" },
      ...(isAdmin() ? [{ name: t("users"), path: "/app/users" }] : []),
      { name: t("calendar"), path: "/app/calendar" },
      ...(isAdmin()
        ? [{ name: t("createTraining"), path: "/app/trainings/new" }]
        : []),
      ...(isAdmin()
        ? [{ name: t("enrollmentsAdmin"), path: "/app/enrollments" }]
        : []),
      { name: t("myTrainings"), path: "/app/my-trainings" },
      ...(isAdmin()
        ? [{ name: t("feedbacksAdmin"), path: "/app/feedbacks" }]
        : []),
      { name: t("myFeedbacks"), path: "/app/my-feedbacks" },
      { name: t("myPayments"), path: "/app/my-payments" },
      ...(isAdmin()
        ? [{ name: t("paymentsAdmin"), path: "/app/payments" }]
        : []),
      { name: t("profile"), path: "/app/profile" },
      { name: t("settings"), path: "/settings/language" },
    ];
    return base;
  }, [t]);

  useEffect(() => {
    if (inputValue.trim() === "") {
      setFiltered([]);
      setShowSuggestions(false);
      return;
    }
    const q = inputValue.toLowerCase();
    const filt = pages.filter((p) => p.name.toLowerCase().includes(q));
    setFiltered(filt);
    setShowSuggestions(filt.length > 0);
  }, [inputValue, pages]);

  useEffect(() => {
    if (inputRef.current) setInputWidth(inputRef.current.offsetWidth);
  }, [inputValue]);

  useEffect(() => {
    setInputValue(search);
  }, [search]);

  const handleSelect = (path: string) => {
    setSearch(inputValue);
    window.location.href = path;
    setShowSuggestions(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && filtered.length > 0) {
      setSearch(inputValue);
      window.location.href = filtered[0].path;
      setShowSuggestions(false);
    }
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
          placeholder={t("searchFeatures")}
          className="w-1/2 px-4 py-2 border rounded shadow-sm focus:outline-none focus:ring focus:ring-blue-300"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onFocus={() => {
            if (inputValue.trim() === "") {
              setFiltered(pages);
              setShowSuggestions(true);
            }
            if (inputRef.current) setInputWidth(inputRef.current.offsetWidth);
          }}
          onKeyDown={handleKeyDown}
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

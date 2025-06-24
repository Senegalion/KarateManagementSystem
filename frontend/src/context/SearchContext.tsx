import { createContext, useContext, useState, type ReactNode } from "react";

type SearchContextType = {
  search: string;
  setSearch: (value: string) => void;
};

export const SearchContext = createContext<SearchContextType | undefined>(
  undefined
);

export const SearchProvider = ({ children }: { children: ReactNode }) => {
  const [search, setSearch] = useState("");

  return (
    <SearchContext.Provider value={{ search, setSearch }}>
      {children}
    </SearchContext.Provider>
  );
};

export const useSearch = () => {
  const context = useContext(SearchContext);
  if (!context) {
    throw new Error("useSearch must be used within a SearchProvider");
  }
  return context;
};

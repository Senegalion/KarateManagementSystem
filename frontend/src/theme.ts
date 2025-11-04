export type Theme = "light" | "dark" | "system";

const KEY = "theme";

const prefersDark = () =>
  window.matchMedia("(prefers-color-scheme: dark)").matches;

const computeDark = (t: Theme) =>
  t === "dark" || (t === "system" && prefersDark());

export const getSavedTheme = (): Theme => {
  const t = localStorage.getItem(KEY) as Theme | null;
  return t === "light" || t === "dark" || t === "system" ? t : "system";
};

export const applyTheme = (t: Theme) => {
  const html = document.documentElement;
  const isDark = computeDark(t);
  html.classList.toggle("dark", isDark);
  html.setAttribute("data-theme", t);
};

export const setTheme = (t: Theme) => {
  localStorage.setItem(KEY, t);
  applyTheme(t);
  window.dispatchEvent(
    new CustomEvent("themechange", { detail: { theme: t } })
  );
};

export const initThemeWatcher = () => {
  const mql = window.matchMedia("(prefers-color-scheme: dark)");
  const handler = () => {
    if (getSavedTheme() === "system") applyTheme("system");
  };
  if (mql.addEventListener) mql.addEventListener("change", handler);
  else mql.addListener(handler);
  return () => {
    if (mql.removeEventListener) mql.removeEventListener("change", handler);
    else mql.removeListener(handler);
  };
};

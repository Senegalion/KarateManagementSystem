import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import ProtectedRoute from "./components/ProtectedRoute";
import AppLayout from "./layouts/AppLayout";
import Dashboard from "./pages/Dashboard";
import SelectClub from "./pages/SelectClub";
import RequireClub from "./components/RequireClub";
import TrainingCalendar from "./pages/TrainingCalendar";
import { SearchProvider } from "./context/SearchContext";
import CreateTraining from "./pages/CreateTraining";
import SettingsLayout from "./layouts/SettingsLayout";
import LanguageSettings from "./pages/settings/LanguageSettings";
import RequireAdmin from "./components/RequireAdmin";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <SearchProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />}>
            <Route index element={<Home />} />
            <Route path="select-club" element={<SelectClub />} />
            <Route
              path="login"
              element={
                <RequireClub>
                  <Login />
                </RequireClub>
              }
            />
            <Route path="register" element={<Register />} />
          </Route>

          <Route
            path="/app"
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="calendar" element={<TrainingCalendar />} />
            <Route
              path="trainings/new"
              element={
                <RequireAdmin>
                  <CreateTraining />
                </RequireAdmin>
              }
            />
          </Route>

          <Route path="/settings" element={<SettingsLayout />}>
            <Route index element={<Navigate to="language" replace />} />
            <Route path="language" element={<LanguageSettings />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </SearchProvider>
  </React.StrictMode>
);

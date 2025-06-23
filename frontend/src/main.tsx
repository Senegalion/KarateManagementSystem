import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import ProtectedRoute from "./components/ProtectedRoute";
import AppLayout from "./layouts/AppLayout";
import Dashboard from "./pages/Dashboard";
import SelectClub from "./pages/SelectClub";
import RequireClub from "./components/RequireClub";
import TrainingCalendar from "./pages/TrainingCalendar";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
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
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="calendar" element={<TrainingCalendar />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);

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
import UsersList from "./pages/UsersList";
import ProfileSettings from "./pages/settings/ProfileSettings";
import MyTrainings from "./pages/MyTrainings";
import AdminEnrollments from "./pages/AdminEnrollments";
import AdminFeedbacks from "./pages/AdminFeedbacks";
import MyFeedbacks from "./pages/MyFeedbacks";
import MyPayments from "./pages/MyPayments";
import AdminPayments from "./pages/AdminPayments";
import PaymentReturn from "./pages/PaymentReturn";
import PaymentCancel from "./pages/PaymentCancel";
import NotificationSettings from "./pages/settings/NotificationSettings";
import AppearanceSettings from "./pages/settings/AppearanceSettings";
import SecuritySettings from "./pages/settings/SecuritySettings";

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
            <Route path="profile" element={<ProfileSettings />} />
            <Route
              path="users"
              element={
                <RequireAdmin>
                  <UsersList />
                </RequireAdmin>
              }
            />
            <Route path="calendar" element={<TrainingCalendar />} />
            <Route path="my-trainings" element={<MyTrainings />} />
            <Route
              path="enrollments"
              element={
                <RequireAdmin>
                  <AdminEnrollments />
                </RequireAdmin>
              }
            />
            <Route
              path="trainings/new"
              element={
                <RequireAdmin>
                  <CreateTraining />
                </RequireAdmin>
              }
            />
            <Route
              path="feedbacks"
              element={
                <RequireAdmin>
                  <AdminFeedbacks />
                </RequireAdmin>
              }
            />
            <Route path="my-feedbacks" element={<MyFeedbacks />} />
            <Route path="my-payments" element={<MyPayments />} />

            <Route
              path="payments"
              element={
                <RequireAdmin>
                  <AdminPayments />
                </RequireAdmin>
              }
            />
          </Route>

          <Route path="/app/payments/return" element={<PaymentReturn />} />
          <Route path="/app/payments/cancel" element={<PaymentCancel />} />

          <Route path="/settings" element={<SettingsLayout />}>
            <Route index element={<Navigate to="language" replace />} />
            <Route path="language" element={<LanguageSettings />} />
            <Route path="notifications" element={<NotificationSettings />} />
            <Route path="security" element={<SecuritySettings />} />
            <Route path="appearance" element={<AppearanceSettings />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </SearchProvider>
  </React.StrictMode>
);

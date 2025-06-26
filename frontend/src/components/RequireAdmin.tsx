import { Navigate } from "react-router-dom";
import { isAdmin } from "../utils/auth";
import type { JSX } from "react";

const RequireAdmin = ({ children }: { children: JSX.Element }) => {
  if (!isAdmin()) {
    return <Navigate to="/app/dashboard" replace />;
  }
  return children;
};

export default RequireAdmin;

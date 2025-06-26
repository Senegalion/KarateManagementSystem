import { jwtDecode } from "jwt-decode";

export type JwtPayload = {
  sub: string;
  roles: string[];
  exp: number;
  iat: number;
};

export const getUserRoles = (): string[] => {
  const token = localStorage.getItem("token");
  if (!token) return [];

  try {
    const decoded = jwtDecode<JwtPayload>(token);
    return decoded.roles || [];
  } catch {
    return [];
  }
};

export const isAdmin = (): boolean => {
  const roles = getUserRoles();
  return roles.includes("ROLE_ADMIN");
};

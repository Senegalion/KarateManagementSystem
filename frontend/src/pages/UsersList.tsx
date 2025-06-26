import { useEffect, useState } from "react";
import { API } from "../api";
import { useTranslation } from "react-i18next";

type User = {
  userId: number;
  username: string;
  email: string;
  roles: string[];
  karateRank: string;
};

const UsersList = () => {
  const { t } = useTranslation();
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const clubName = localStorage.getItem("selectedClub");
    const token = localStorage.getItem("token");

    if (!clubName) {
      setError(t("noValidClubSelected"));
      setLoading(false);
      return;
    }

    API.get(`/users/by-club?clubName=${encodeURIComponent(clubName)}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        setUsers(res.data);
        setError(null);
      })
      .catch((err) => {
        console.error(err);
        setError(t("failedToFetchUsers"));
      })
      .finally(() => setLoading(false));
  }, [t]);

  if (loading) {
    return <div className="p-6">{t("loadingUsers")}</div>;
  }

  if (error) {
    return <div className="p-6 text-red-500">{error}</div>;
  }

  const admins = users.filter((u) => u.roles.includes("ROLE_ADMIN"));
  const nonAdmins = users.filter((u) => !u.roles.includes("ROLE_ADMIN"));

  const renderTable = (title: string, data: User[]) => (
    <div className="mb-10">
      <h2 className="text-2xl font-semibold mb-4">{title}</h2>
      {data.length === 0 ? (
        <p className="text-gray-500">
          {t("no")} {title.toLowerCase()} {t("found")}.
        </p>
      ) : (
        <table className="w-full table-auto text-sm border shadow-sm rounded-xl overflow-hidden">
          <thead>
            <tr className="bg-gray-100 text-left">
              <th className="p-2">{t("username")}</th>
              <th className="p-2">{t("email")}</th>
              <th className="p-2">{t("roles")}</th>
              <th className="p-2">{t("karateRank")}</th>
            </tr>
          </thead>
          <tbody>
            {data.map((u) => (
              <tr key={u.userId} className="border-t">
                <td className="p-2">{u.username}</td>
                <td className="p-2">{u.email}</td>
                <td className="p-2">
                  {u.roles
                    .map((r) =>
                      r
                        .replace("ROLE_", "")
                        .toLowerCase()
                        .replace(/^\w/, (c) => c.toUpperCase())
                    )
                    .join(", ")}
                </td>
                <td className="p-2">
                  {u.karateRank
                    .toLowerCase()
                    .replace("_", " ")
                    .replace(/^\w/, (c) => c.toUpperCase())}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );

  return (
    <div className="p-6 animate-fade-in">
      <h1 className="text-3xl font-bold mb-6">{t("clubMembersOverview")}</h1>
      <div className="bg-white border shadow-sm rounded-xl p-5">
        {renderTable(t("admins"), admins)}
        {renderTable(t("users"), nonAdmins)}
      </div>
    </div>
  );
};

export default UsersList;

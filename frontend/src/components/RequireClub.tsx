import { Navigate, useLocation } from "react-router-dom";

const RequireClub = ({ children }: { children: React.ReactNode }) => {
  const club = localStorage.getItem("selectedClub");
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const wantsToChangeClub = params.get("changeClub") === "true";

  if (!club || wantsToChangeClub) {
    return (
      <Navigate
        to={`/select-club?redirect=${encodeURIComponent(location.pathname)}`}
        replace
      />
    );
  }

  return <>{children}</>;
};

export default RequireClub;

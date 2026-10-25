import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { ledgerService } from "../api/ledgerService";
import { useAuth } from "./AuthContext";

const PeriodContext = createContext(null);

export const PeriodProvider = ({ children }) => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId;

  const [activePeriod, setActivePeriod] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchActivePeriod = useCallback(async () => {
    if (!Number.isFinite(Number(apartmentId)) || Number(apartmentId) <= 0) {
      return;
    }

    try {
      setLoading(true);
      const data = await ledgerService.getActiveLedgerPeriod(apartmentId);
      setActivePeriod(data);
    } catch (error) {
      console.error("Failed to load active period globally:", error);
    } finally {
      setLoading(false);
    }
  }, [apartmentId]);

  useEffect(() => {
    if (user) {
      fetchActivePeriod();
    }
  }, [fetchActivePeriod, user]);

  return (
    <PeriodContext.Provider
      value={{ activePeriod, refreshPeriod: fetchActivePeriod, loading }}>
      {children}
    </PeriodContext.Provider>
  );
};

export const usePeriod = () => useContext(PeriodContext);

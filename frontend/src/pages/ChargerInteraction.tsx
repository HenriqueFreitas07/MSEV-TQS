

import { useEffect, useState } from "react";
import { useParams } from "react-router";
import NavLayout from "../layouts/NavLayout";
import { ChargerService } from "../requests.ts";

import type { Charger } from "../types/Charger.tsx";

function ChargerDetails() {
  const { chargerId } = useParams();
  const [charger, setCharger] = useState<Charger | null>(null);
  const [loading, setLoading] = useState(true);
  const [charging, setCharging] = useState(false);

  const fetchCharger = async () => {
    if (!chargerId) return;
    setLoading(true);
    try {
      const fetched = await ChargerService.getChargerById(chargerId);
      setCharger(fetched);
    } catch (error) {
      console.error("Error fetching charger:", error);
    }
    setLoading(false);
  };

  const handleLock = async () => {
    if (!charger) return;
    try {
      await ChargerService.lockCharger(charger.id);
      await fetchCharger(); 
      setCharging(true);
    } catch (err) {
      console.error("Failed to lock charger", err);
    }
  };

  const handleUnlock = async () => {
    if (!charger) return;
    try {
      await ChargerService.unlockCharger(charger.id);
      await fetchCharger(); 
      setCharging(false);
    } catch (err) {
      console.error("Failed to unlock charger", err);
    }
  };

  useEffect(() => {
    fetchCharger();
  }, [chargerId]);

  const getColor = (status: string) => {
    switch (status) {
      case "AVAILABLE": return "text-blue-400";
      case "IN_USE": return "text-green-400";
      case "OUT_OF_ORDER": return "text-orange-400";
      case "TEMPORARILY_DISABLED": return "text-red-400";
      default: return "text-black";
    }
  };

  if (loading) {
    return (
      <NavLayout title="Charger">
        <div className="p-8 text-center">Loading...</div>
      </NavLayout>
    );
  }

  if (!charger) {
    return (
      <NavLayout title="Charger">
        <div className="p-8 text-center text-red-600">Charger not found</div>
      </NavLayout>
    );
  }

  return (
    <div className="bg-base-200 min-h-screen">
      <NavLayout title={`Charger ${charger.id}`}>
        <div className="max-w-xl mx-auto mt-8 p-6 bg-base-100 rounded-xl shadow">
          <h2 className="text-2xl font-bold mb-4">Charger Details</h2>
          <p className="mb-2"><strong>Connector Type:</strong> {charger.connectorType}</p>
          <p className="mb-2"><strong>Charging Speed:</strong> {charger.chargingSpeed}</p>
          <p className="mb-4">
            <strong>Status:</strong>{" "}
            <span className={`${getColor(charger.status)} font-semibold`}>{charger.status}</span>
          </p>
          <div className="flex gap-4 mt-4">
            {charger.status === "AVAILABLE" && !charging && (
              <button
                className="btn btn-primary"
                onClick={() => handleLock()}
              >
                Lock Charger
              </button>
            )}
            {charging && (
                <button
                className="btn btn-primary"
                onClick={() => handleUnlock()}
              >
                Unlock Charger
              </button>
            )}
          </div>
        </div>
      </NavLayout>
    </div>
  );
}

export default ChargerDetails;

import { useEffect, useState } from "react";
import { ChargerService } from "../requests";

import type { Reservation } from "../types/Reservation";
import type { Charger } from "../types/Charger"

type Props = {
  charger: Charger;
  updateCharger: (chargerId: string, status: string) => void;
}

export function ChargerCard({ charger, updateCharger }: Props) {
  const [reservations, setReservations] = useState<Reservation[]>([]);

  useEffect(() => {
    async function loadResevationsOfCharger() {
      setReservations(await ChargerService.getChargerReservationsForNextDays(charger.id));
    }

    loadResevationsOfCharger();
  }, [charger]);


  return (
    <div className={`w-full flex  justify-between gap-2 rounded-md border p-6 ${charger.status === "OUT_OF_ORDER" || charger.status === "TEMPORARILY_DISABLED" ? " border-red-400" : " border-zinc-400"}`}>
      <div className="flex flex-col">
        <span>Connector Type: {charger.connectorType}</span>
        <span>Status: {charger.status}</span>
        <span>Price (per kWh): {charger.price}</span>
        <span>Charging Speed: {charger.chargingSpeed}</span>
        <span>Reservations for the next 5 days: {reservations.length}</span>
      </div>
      <div className="flex flex-col">
        {charger.status !== "AVAILABLE" && charger.station.status !== "DISABLED" ?
          <button className="btn btn-success p-2 mb-2" onClick={() => updateCharger(charger.id, "AVAILABLE")}>AVAILABLE</button>
          :
          <></>
        }
        {charger.status !== "OUT_OF_ORDER" ?
          <button className="btn btn-warning p-2 mb-2" onClick={() => updateCharger(charger.id, "OUT_OF_ORDER")}>OUT_OF_ORDER</button>
          :
          <></>
        }
        {charger.status !== "TEMPORARILY_DISABLED" ?
          <button className="btn btn-error p-2 mb-2" onClick={() => updateCharger(charger.id, "TEMPORARILY_DISABLED")}>TEMPORARILY_DISABLED</button>
          :
          <></>
        }
      </div>
    </div>
  )
}

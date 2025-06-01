import { useEffect, useState } from "react";
import { ChargerService } from "../requests";

import type { Reservation } from "../types/Reservation";
import type { Charger } from "../types/Charger"

type Props = {
  charger: Charger;
}

export function ChargerCard({ charger }: Props) {
  const [reservations, setReservations] = useState<Reservation[]>([]);

  useEffect(() => {
    async function loadResevationsOfCharger() {
      setReservations(await ChargerService.getChargerReservationsForNextDays(charger.id));
    }

    loadResevationsOfCharger();
  }, [charger]);

  return (
    <div className={`w-full flex flex-col gap-2 rounded-md border p-6 ${charger.status === "OUT_OF_ORDER" ? "bg-red-200 border-red-400" : "bg-base-200 border-zinc-400"}`}>
      <span>Connector Type: {charger.connectorType}</span>
      <span>Status: {charger.status}</span>
      <span>Price (per kWh): {charger.price}</span>
      <span>Charging Speed: {charger.chargingSpeed}</span>
      <span>Reservations for the next 5 days: {reservations.length}</span>
    </div>
  )
}

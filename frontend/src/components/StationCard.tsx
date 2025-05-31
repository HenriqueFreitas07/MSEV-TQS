import { useEffect, useState } from "react";
import { ChargerService } from "../requests";

import type { Station } from "../types/Station"
import type { Charger } from "../types/Charger";

type Props = {
  station: Station;
  onClick: () => void;
}

export function StationCard({ station, onClick }: Props) {
  const [chargers, setChargers] = useState<Charger[]>([]);
  const [chargersOutOfOrder, setChargersOutOfOrder] = useState(0);

  useEffect(() => {
    async function loadChargers() {
      const chargersRes = await ChargerService.getChargerByStation(station.id)
      setChargers(chargersRes);
      console.log(chargersRes)

      setChargersOutOfOrder(chargersRes.filter(c => c.status === "OUT_OF_ORDER").length);
    }

    loadChargers();
  }, [station]);

  return (
    <div className={`border rounded-md p-6 flex flex-col cursor-pointer bg-base-200 hover:scale-105 duration-75 shadow-xs ${chargersOutOfOrder > 0 ? "border-red-400 shadow-red-600" : "border-zinc-400"}`} onClick={onClick}>
      <span onClick={onClick}><strong>Name:</strong> {station.name}</span>
      <span onClick={onClick}><strong>Address:</strong> {station.address}</span>
      <span onClick={onClick}><strong>Status:</strong> {station.status}</span>
      <span onClick={onClick}><strong>Chargers:</strong> {chargers.length}</span>
      {
        chargersOutOfOrder > 0 && (
          <span onClick={onClick} className="text-red-600 font-bold">{chargersOutOfOrder} Chargers Out Of Order</span>
        )
      }
    </div>
  )
}

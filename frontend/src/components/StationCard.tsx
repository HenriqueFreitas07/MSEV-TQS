import { useEffect, useState } from "react";
import { ChargerService } from "../requests";

import type { Station } from "../types/Station"
import type { Charger } from "../types/Charger";

type Props = {
  station: Station;
  onClick: () => void;
  onUpdate: () => void;
}

export function StationCard({ station, onClick, onUpdate }: Props) {
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
    <div className={`border rounded-md flex flex-col justify-center cursor-pointer bg-base-200 hover:scale-105 duration-75 shadow-xs ${chargersOutOfOrder > 0 ? "border-red-400 shadow-red-600" : "border-zinc-400"}`} >
      <div className="h-3/4 p-6 " onClick={onClick}>
        <span onClick={onClick}><strong>Name:</strong> {station.name}</span><br />
        <span onClick={onClick}><strong>Address:</strong> {station.address}</span><br />
        <span onClick={onClick}><strong>Status:</strong> {station.status}</span><br />
        <span onClick={onClick}><strong>Chargers:</strong> {chargers.length}</span><br />
        {
          chargersOutOfOrder > 0 && (
            <><span onClick={onClick} className="text-red-600 font-bold">{chargersOutOfOrder} Charger{chargersOutOfOrder !== 1 ? "s" : ""} Out Of Order</span> <br /> </>
          )
        }
        <br />
      </div>
      <div className="h-1/4">
        <button className="btn btn-info w-full h-full" onClick={onUpdate}>{station.status === "ENABLED" ? "DISABLE" : "ENABLE"}</button>

      </div>
    </div >
  )
}

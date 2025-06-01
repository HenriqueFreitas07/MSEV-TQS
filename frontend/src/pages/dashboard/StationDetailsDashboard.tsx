import { useEffect, useState } from "react";
import { NavLink, useParams } from "react-router";
import type { Charger } from "../../types/Charger";
import { ChargerService, StationService } from "../../requests";
import type { Station } from "../../types/Station";
import NavLayout from "../../layouts/NavLayout";
import { ChargerCard } from "../../components/ChargerCard";

type Params = {
  stationId: string;
}

export default function StationDetailsDashboard() {
  const { stationId } = useParams<Params>();
  const [station, setStation] = useState<Station | null>(null);
  const [chargers, setChargers] = useState<Charger[]>([]);

  useEffect(() => {
    async function loadChargers() {
      setStation(await StationService.getStationById(stationId!));
      setChargers(await ChargerService.getChargerByStation(stationId!));
    }

    loadChargers();
  }, [stationId]);

  if (!station) return <div />;

  return (
    <NavLayout title="MSEV" footer={false}>
      <div className="p-8">
        <div className="flex justify-between items-center mb-4">
          <h2 className="font-bold text-2xl">{station.name} station</h2>
          <NavLink to="/" className="btn btn-primary">
            Add Charger
          </NavLink>
        </div>
        <hr />

        <div className="flex flex-col gap-4 mt-6">
          {
            chargers.map(charger => <ChargerCard key={charger.id} charger={charger} />)
          }
        </div>
      </div>
    </NavLayout>
  )
}

import { useEffect, useState } from "react";
import NavLayout from "../../layouts/NavLayout";
import type { Station } from "../../types/Station";
import { StationService } from "../../requests";
import { StationCard } from "../../components/StationCard";
import { NavLink, useNavigate } from "react-router";

export default function Dashboard() {
  const [stations, setStations] = useState<Station[]>([]);
  const [searchQuery, setSearchQuery] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    async function loadStations() {
      setStations(await StationService.getAllStations());
    }

    loadStations();
  }, []);

  function handleStationClick(stationId: string) {
    navigate(`/dashboard/stations/${stationId}`)
  }

  return (
    <NavLayout title="MSEV" footer={false}>
      <div className="p-8">
        <div className="flex justify-between mb-4 items-center">
          <h2 className="font-bold text-2xl">Stations</h2>

          <div className="flex items-center justify-end gap-4 w-1/2">
            <label className="input">
              <svg className="h-[1em] opacity-50" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <g
                  strokeLinejoin="round"
                  strokeLinecap="round"
                  strokeWidth="2.5"
                  fill="none"
                  stroke="currentColor"
                >
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.3-4.3"></path>
                </g>
              </svg>
              <input type="search" value={searchQuery} onChange={e => setSearchQuery(e.target.value.toLowerCase())} placeholder="Search" />
            </label>
            <NavLink to="/" className="btn btn-primary">
              Add Station
            </NavLink>
          </div>
        </div>
        <hr />
        <div className="grid lg:grid-cols-5 md:grid-cols-3 grid-cols-2 gap-4 mt-6">
          {
            stations.filter(s => s.name.toLowerCase().startsWith(searchQuery)).map(station => (
              <StationCard key={station.id} station={station} onClick={() => handleStationClick(station.id)} />
            ))
          }
        </div>
      </div>
    </NavLayout>
  )
}

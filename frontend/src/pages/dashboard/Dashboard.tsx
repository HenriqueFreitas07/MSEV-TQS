import { useEffect, useRef, useState, type FormEvent } from "react";
import NavLayout from "../../layouts/NavLayout";
import type { Station } from "../../types/Station";
import { StationService } from "../../requests";
import { StationCard } from "../../components/StationCard";
import { useNavigate } from "react-router";
import MapComponent from "../../components/MapComponent";
import type { MapMouseEvent } from "@vis.gl/react-google-maps";
import { showAlert } from "../../alerts";
import { FaMapMarkerAlt } from "react-icons/fa";

export default function Dashboard() {
  const [stations, setStations] = useState<Station[]>([]);
  const [searchQuery, setSearchQuery] = useState("");

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const [latitude, setLatitude] = useState<number | null>(null);
  const [longitude, setLongitude] = useState<number | null>(null);

  const modalRef = useRef<HTMLDialogElement | null>(null);

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

  async function handleCreateStation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (latitude === null || longitude === null) {
      showAlert("Error", "Please select a point in the map", "error");
      modalRef.current?.close();
      return;
    }

    const newStation = await StationService.createStation(name, address, latitude, longitude);

    setStations(prev => [...prev, newStation]);

    setName("");
    setAddress("");
    setLatitude(null);
    setLongitude(null);

    modalRef.current?.close();

    showAlert("Success", "Station created!", "success");
  }

  function handleMapClick(event: MapMouseEvent) {
    const { lat, lng } = event.detail.latLng!;

    setLatitude(lat);
    setLongitude(lng);
  }

  async function toggleEnable(station: Station) {
    if (station.status === "ENABLED") {
      await StationService.disableStation(station.id);
      setStations(prev => {
        const stationx = prev.find(s => s.id === station.id)!;
        stationx.status = "DISABLED"

        return [
          ...prev.filter(s => s.id !== stationx.id),
          stationx
        ]
      })
    } else {
      await StationService.enableStation(station.id);
      setStations(prev => {
        const stationx = prev.find(s => s.id === station.id)!;
        stationx.status = "ENABLED"

        return [
          ...prev.filter(s => s.id !== stationx.id),
          stationx
        ]
      })
    }
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
            <button className="btn btn-primary" onClick={() => modalRef.current?.showModal()}>
              Add Station
            </button>
          </div>
        </div>
        <hr />
        <div className="grid lg:grid-cols-5 md:grid-cols-3 grid-cols-2 gap-4 mt-6">
          {
            stations.filter(s => s.name.toLowerCase().startsWith(searchQuery)).map(station => (
              <StationCard key={station.id} station={station} onClick={() => handleStationClick(station.id)} onUpdate={() => toggleEnable(station)} />
            ))
          }
        </div>
      </div>
      <dialog className="modal" ref={modalRef}>
        <div className="modal-box max-w-5xl">
          <form method="dialog">
            <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
          </form>
          <h3 className="font-bold text-xl">New Station</h3>
          <form onSubmit={handleCreateStation} className="flex flex-col mt-4">
            <fieldset className="fieldset">
              <legend className="fieldset-legend">Name</legend>
              <input type="text" className="input w-full" placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
            </fieldset>

            <fieldset className="fieldset">
              <legend className="fieldset-legend">Address</legend>
              <input type="text" className="input w-full" placeholder="Address" value={address} onChange={e => setAddress(e.target.value)} required />
            </fieldset>

            <div className="h-96 w-full mt-4">
              <MapComponent
                onClick={handleMapClick}
                center={{ lat: 40.6408505, lng: -8.6332439 }}
                zoom={10}
                markers={(latitude !== null && longitude !== null) ? [{
                  markerOptions: {
                    position: { lat: latitude, lng: longitude }
                  },
                  icon: <FaMapMarkerAlt className="text-green-500" size={40} />
                }] : []}
                fitBounds={false}
              />
            </div>

            <button type="submit" className="btn btn-primary mt-4">
              Create
            </button>
          </form>
        </div>
      </dialog>
    </NavLayout>
  )
}

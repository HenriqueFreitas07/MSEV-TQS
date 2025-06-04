import { useEffect, useRef, useState, type FormEvent } from "react";
import { useParams } from "react-router";
import type { Charger } from "../../types/Charger";
import { ChargerService, StationService } from "../../requests";
import type { Station } from "../../types/Station";
import NavLayout from "../../layouts/NavLayout";
import { ChargerCard } from "../../components/ChargerCard";
import { showAlert } from "../../alerts";

type Params = {
  stationId: string;
}

export default function StationDetailsDashboard() {
  const { stationId } = useParams<Params>();

  const [station, setStation] = useState<Station | null>(null);
  const [chargers, setChargers] = useState<Charger[]>([]);

  const [connectorType, setConnectorType] = useState("");
  const [price, setPrice] = useState(0);
  const [chargingSpeed, setChargingSpeed] = useState(0);

  const modalRef = useRef<HTMLDialogElement | null>(null);

  useEffect(() => {
    async function loadData() {
      setStation(await StationService.getStationById(stationId!));
      setChargers(await ChargerService.getChargerByStation(stationId!));
    }

    loadData();
  }, [stationId]);

  async function handleCreateCharger(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();

    if (connectorType === "") {
      modalRef.current?.close();
      showAlert("Error", "Invalid connector type", "error");
      return;
    }

    const newCharger = await ChargerService.createCharger(stationId!, connectorType, price, chargingSpeed);

    setChargers(prev => [...prev, newCharger]);

    showAlert("Success", "Charger created!", "success");
    modalRef.current?.close();

    setConnectorType("");
    setPrice(0);
    setChargingSpeed(0);
  }

  if (!station) return <div />;

  return (
    <NavLayout title="MSEV" footer={false}>
      <div className="p-8">
        <div className="flex justify-between items-center mb-4">
          <h2 className="font-bold text-2xl">{station.name} station</h2>
          <button className="btn btn-primary" onClick={() => modalRef.current?.showModal()}>
            Add Charger
          </button>
        </div>
        <hr />

        <div className="flex flex-col gap-4 mt-6">
          {
            chargers.map(charger => <ChargerCard key={charger.id} charger={charger} />)
          }
        </div>
      </div>

      <dialog className="modal" ref={modalRef}>
        <div className="modal-box max-w-96">
          <form method="dialog">
            <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
          </form>
          <h3 className="font-bold text-xl">New Charger</h3>
          <form onSubmit={handleCreateCharger} className="flex flex-col mt-4">
            <fieldset className="fieldset">
              <legend className="fieldset-legend">Connector Type</legend>
              <select className="select w-full" value={connectorType} onChange={e => setConnectorType(e.target.value)}>
                <option value="" disabled>Pick a connector type</option>
                <option value="Type 1 (J1772)">Type 1 (J1772)</option>
                <option value="Type 2 (Mennekes)">Type 2 (Mennekes)</option>
                <option value="CCS1">CCS1</option>
                <option value="CCS2">CCS2</option>
                <option value="CHAdeMO">CHAdeMO</option>
                <option value="Tesla NACS">Tesla NACS</option>
              </select>
            </fieldset>

            <fieldset className="fieldset">
              <legend className="fieldset-legend">Price</legend>
              <input type="number" className="input w-full" placeholder="Price (€)" value={price} onChange={e => setPrice(Number(e.target.value))} required />
            </fieldset>

            <fieldset className="fieldset">
              <legend className="fieldset-legend">Charging Speed</legend>
              <input type="number" className="input w-full" placeholder="Charging Speed (kW)" value={chargingSpeed} onChange={e => setChargingSpeed(Number(e.target.value))} required />
            </fieldset>

            <button type="submit" className="btn btn-primary mt-4">
              Create
            </button>
          </form>
        </div>
      </dialog>
    </NavLayout>
  )
}

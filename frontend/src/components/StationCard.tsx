import { useEffect, useState } from "react";
import { ChargerService } from "../requests";
import type { ChartData } from "chart.js";
import type { Station } from "../types/Station"
import type { Charger } from "../types/Charger";
import type { ChargeSession } from "../types/charge-session";
import LineChart from "../components/LineChart";

type Props = {

  station: Station;
  onClick: () => void;
  onUpdate: () => void;
}

const options = {
  responsive: true,
  plugins: {
    legend: {
      position: 'top' as const,
    },
  },
}

export function StationCard({ station, onClick, onUpdate }: Props) {
  const [chargers, setChargers] = useState<Charger[]>([]);
  const [chargersOutOfOrder, setChargersOutOfOrder] = useState(0);
  const [labels, setLabels] = useState<string[]>([])
  const [points, setPoints] = useState<Record<number, number[]>>({})
  const [data, setData] = useState<ChartData<"line"> | null>(null)

  useEffect(() => {
    async function loadChargers() {
      const chargersRes = await ChargerService.getChargerByStation(station.id)
      setChargers(chargersRes);
      console.log(chargersRes)

      setChargersOutOfOrder(chargersRes.filter(c => c.status === "OUT_OF_ORDER").length);
    }

    loadChargers();
  }, [station]);


  const chargerGraphic = async () => {
    const chargerDatasets = []
    for (let cIndex = 0; cIndex < chargers.length; cIndex++) {
      const charger: Charger = chargers[cIndex];
      if (charger.status !== "IN_USE") {
        continue
      }
      const response = await ChargerService.getStatistics(charger.id)
      const stats: ChargeSession = response
      setPoints(prev => {
        const updated = [...(prev[cIndex] || []), stats.consumption];
        const trimmed = updated.length > 60 ? updated.slice(-60) : updated;
        return {
          ...prev,
          [cIndex]: trimmed
        };
      });
      const colorDeviation = (cIndex + 1) * 40
      const now = new Date();
      const label = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;

      setLabels((prev) => {
        const updated = [...prev, label];
        const trimmed = updated.length > 60 ? updated.slice(-60) : updated;
        return trimmed
      });

      chargerDatasets.push(
        {
          label: `Charger nº ${chargerDatasets.length + 1}`,
          data: points[cIndex],
          fill: true,
          borderColor: `rgb(${(150 + colorDeviation) % 255}, ${(200 + colorDeviation) % 255}, ${(100 + colorDeviation) % 255})`,
          tension: 0.1
        }
      )
    }
    const datasets: ChartData<"line"> = {
      labels: labels,
      datasets: chargerDatasets
    }
    setData(datasets)
  }

  useEffect(() => {
    setTimeout(chargerGraphic, 1000)
  }, [data]);

  return (
    <div className={`border w-full rounded-md flex flex-col justify-center cursor-pointer bg-base-200 hover:scale-105 duration-75 shadow-xs ${chargersOutOfOrder > 0 ? "border-red-400 shadow-red-600" : "border-zinc-400"}`} data-testid="station-card">
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
      {data !== null && data.datasets.length>0 &&
        <div className="w-full p-1 min-h-[300px]">
          <LineChart options={options} data={data} />
        </div>
      }
      <div className="h-1/4">
        <button className={`btn w-full h-full ${station.status === "ENABLED" ? "btn-error" : "btn-success"}` } onClick={onUpdate}>{station.status === "ENABLED" ? "DISABLE" : "ENABLE"}</button>
      </div>
    </div >
  )
}

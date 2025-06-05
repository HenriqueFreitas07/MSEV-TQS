import { format } from "date-fns";
import type { ChargeSession } from "../types/charge-session";
import LineChart from "../components/LineChart";
import { ChargerService } from "../requests";
import { useEffect ,useState} from "react";
import type { ChartData } from "chart.js";

type Props = {
  session: ChargeSession;
  endSession: () => void;
}
const options = {
  responsive: true,
  plugins: {
    legend: {
      position: 'top' as const,
    },
  },
}

export function ChargeSessionCard({ session, endSession }: Props) {
  const [points, setPoints] = useState<number[]>([])
  const [data, setData] = useState<ChartData<"line"> | null>(null)
  async function handleLockCharger(chargerId: string) {
    await ChargerService.lockCharger(chargerId);
    endSession();
  }

  const addPoints = () => {
    setTimeout(async () => {
      const response = await ChargerService.getStatistics(session.charger.id)
      const stats:ChargeSession=response
      const labels:string[] = []
      setPoints((prev) => [...prev, stats.consumption])
      if (points.length > 60) {
        setPoints((prev) => prev.slice(1, -1))
      }
      points.map(()=>{
        const d = new Date()
        labels.push(d.getHours() + ":" + d.getMinutes())
      })

      const datasets: ChartData<"line"> = {
        labels: labels,
        datasets: [
          {
            label: "Consumption",
            data: points,
            fill: true,
            borderColor: 'rgb(75, 192, 192)',
            tension: 0.1
          }
        ]
      }
      setData(datasets)
    }, 1000)
  }

  useEffect(() => {
    if(session.endTimestamp==null) 
    {
      addPoints()
    }
  }, [data])


  return (
    <div className=" cursor-pointer transition-all hover:scale-95 w-full flex flex-col p-8 gap-2 rounded-md border-neutral-200 shadow">
      <h3 className="text-xl font-semibold">Charge Session</h3>
      <p>Start Date: {format(session.startTimestamp, "dd-MM-yyyy HH:mm")}</p>
      {
        session.endTimestamp && <p>End Date: {format(session.endTimestamp, "dd-MM-yyyy HH:mm")}</p>
      }

      <p>Consumption: {session.consumption}</p>
      {
        session.endTimestamp !== null && <p>Charging Speed: {session.chargingSpeed}</p>
      }
      <p>{session.endTimestamp === null ? "Estimated " : ""}Price: {session.consumption * session.charger.price} €</p>

      <div className="w-full p-2 min-h-[300px]">
        {data !== null && <LineChart options={options} data={data} />}
      </div>

      {
        session.endTimestamp === null  && (
          <button className="btn btn-primary" onClick={() => handleLockCharger(session.charger.id)}>
            Lock Charger
          </button>
        )
      }
    </div>
  )
}

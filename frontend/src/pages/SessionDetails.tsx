import { useEffect, useState } from "react"
import type { ChargeSession } from "../types/charge-session";
import { useNavigate, useParams } from "react-router";
import { ChargerService } from "../requests";
import type { ChartData } from "chart.js";
import LineChart from "../components/LineChart";
import NavLayout from '../layouts/NavLayout';

export const options = {
    responsive: true,
    plugins: {
        legend: {
            position: 'top' as const,
        },
    },
};
export default function SessionDetails() {
    const { id } = useParams()
    const navigate = useNavigate()
    const [points, setPoints] = useState<number[]>([])
    const [data, setData] = useState<ChartData<"line"> | null>(null)
    const [currentSession, setCurrentSession] = useState<ChargeSession | null>();
    const [loading, setLoading] = useState<boolean>(true)
    useEffect(() => {
        const getSession = async () => {
            const response = await ChargerService.getChargeSessions(true)
            const session: ChargeSession[] = response.filter((s) => s.id == id)
            if (session.length == 1) {
                setCurrentSession(session[0])
                setLoading(false)
                return;
            }
            navigate("/not-found")
        }
        getSession()
    },[])
    const addPoints = () => {
        if (currentSession != null) {
            setTimeout(() => {
                const labels = []
                setPoints((prev) => [...prev, (currentSession.consumption + 1) * (3 * Math.random())])
                if (points.length > 60) {
                    setPoints((prev) => prev.slice(1, -1))
                }
                for (const p in points) {
                    const d = new Date()
                    labels.push(d.getHours() + ":" + d.getMinutes())
                }
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
    }


    return (
        <>
            <NavLayout title="MSEV" footer={false}>
                {
                    loading && 
                    <div className="flex justify-center absolute w-full h-screen bg-gray-300">

                    </div>
                }
                <div className="p-8">
                    <div className="flex items-center justify-between">
                        <h2 className="text-2xl font-bold mb-2">Charge Sessions</h2>
                    </div>
                    <hr />
                    <div className="grid lg:grid-cols-3 md:grid-cols-2 grid-cols-1 gap-4 mt-6">
                        <div className="w-full p-2 min-h-[100px]">
                            {data !== null && <LineChart options={options} data={data} />}
                        </div>
                    </div>

                </div>
            </NavLayout>
        </>
    )
}
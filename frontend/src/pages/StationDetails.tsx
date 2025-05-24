import NavLayout from "../layouts/NavLayout.js";
import { ChargerService, StationService } from "../requests.ts";
import { useEffect, useState } from "react";
import type { Charger } from "../types/Charger.tsx";
import type { Station } from "../types/Station.tsx";
import { useParams } from "react-router";

function StationDetails() {
    let color = "text-black";
    let color_station = "text-black";

    const [chargers, setChargers] = useState<Charger[]>([]);
    const [station, setStation] = useState<Station>();
    const params = useParams();


    useEffect(() => {
        const fetchData = async () => {
            if (params.postId) {
                setStation(await StationService.getStationById(params.postId));
                setChargers(await ChargerService.getChargerByStation(params.postId));
            }
        }
        fetchData();
    }, []);

    const getColorChargers = (status: string) => {
        if (status === "AVAILABLE") {
            color = "text-blue-400";
        }
        else if (status === "IN_USE") {
            color = "text-green-400";
        }
        else if (status === "OUT_OF_ORDER") {
            color = "text-orange-400";
        }
        else if (status === "TEMPORARILY_DISABLED") {
            color = "text-red-400";
        }
        return color;
    }

    const getColorStation = (status: string) => {
        if (status === "ENABLED") {
            color_station = "text-green-400"
        }
        else if (status === "DISABLED") {
            color_station = "text-red-400"
        }
        return color_station;
    }
    if (params.postId && station)
        return (
            <div className="items-center justify-center">
                <NavLayout title="Station" >
                    <p className={`text-4xl p-4 text-center ${getColorStation(station.status)}`}>Station {station.name}</p>

                    <p className="text-4xl p-4 text-center">Chargers</p>
                    <div className="flex grid grid-cols-2 ">
                        {chargers.map(
                            (charger) =>
                                <div className="card p-3 m-4 card-side card-sm bg-base-100 shadow-sm" key={charger.id}>
                                    <figure className="w-2/3">
                                        <img
                                            src="https://www.ayvens.com/-/media/leaseplan-digital/pt/blog/closeupofachargingelectriccarjpgs1024x1024wisk20ckeuxnuhyqyacvxmhzorzeru0rnlkt8gmrr91setym.jpg?rev=5b11d8a3cb184493bb742f8ae7e1a41f"
                                            alt="Charger"
                                        />
                                    </figure>
                                    <div className="card-body">
                                        <h2 className="card-title">{charger.connectorType}</h2>
                                        <p className="font-bold">Price: </p><p> {charger.price}</p>
                                        <p className="font-bold">Connector Type: </p><p> {charger.connectorType}</p>
                                        <p className="font-bold">Charging Speed: </p><p> {charger.chargingSpeed}</p>
                                        <p className="font-bold">Status: </p><p className={`${getColorChargers(charger.status)}`}> {charger.status}</p>
                                        <div className="card-actions justify-end">
                                            <button className="btn btn-primary">Reserve now</button>
                                        </div>
                                    </div>
                                </div>
                        )}
                    </div >
                </NavLayout>
            </div >
        )
}

export default StationDetails;

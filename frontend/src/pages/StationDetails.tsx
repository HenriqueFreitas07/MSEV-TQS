import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import { format } from "date-fns";
import dayjs from "dayjs";

import NavLayout from "../layouts/NavLayout.js";

import { ChargerService, StationService } from "../requests.ts";

import type { Charger } from "../types/Charger.tsx";
import type { Station } from "../types/Station.tsx";
import type { Reservation } from "../types/reservation";

function StationDetails() {
  let color = "text-black";
  let color_station = "text-black";

  const [chargers, setChargers] = useState<Charger[]>([]);
  const [selectedCharger, setSelectedCharger] = useState<string | null>(null);
  const [station, setStation] = useState<Station>();
  const [reservations, setReservations] = useState<Record<string, Reservation[]>>({});

  const params = useParams();

  const modalRef = useRef<HTMLDialogElement | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setStation(await StationService.getStationById(params.postId!));

      const chargersResponse = await ChargerService.getChargerByStation(params.postId!);

      setChargers(chargersResponse);

      for (const charger of chargersResponse) {
        const chargerReservations = await ChargerService.getChargerReservationsForNextDays(charger.id);

        setReservations(prev => {
          return {
            ...prev,
            [charger.id]: chargerReservations
          }
        })
      }
    }
    fetchData();
  }, [params.postId]);

  async function handleUnlockCharger(chargerId: string) {
    await ChargerService.unlockCharger(chargerId);

    setChargers(prev => {
      const charger = prev.find(c => c.id === chargerId)!;
      charger.status = "IN_USE";

      return [
        ...prev.filter(c => c.id !== chargerId),
        charger
      ]
    })
  }

  function canUnlockCharger(chargerId: string, status: string): boolean {
    if (status === "OUT_OF_ORDER" || status === "TEMPORARILY_DISABLED") return false;

    if (status === "IN_USE") {
      const reservationsOfCharger = reservations[chargerId];

      if (!reservationsOfCharger) return false;

      return reservationsOfCharger.some(r => dayjs().isAfter(r.startTimestamp) && dayjs().isBefore(r.endTimestamp));
    }

    return true;
  }

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
      <div className="items-center justify-center bg-base-200">
        <NavLayout title="Station" footer={false}>
          <p data-test-id="station" className={`text-4xl p-4 text-center ${getColorStation(station.status)}`}>Station {station.name}</p>

          <p className="text-4xl p-4 text-center">Chargers</p>
          <div className="grid grid-cols-2" data-test-id="chargers">
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
                    <h2 className="card-title" data-test-id="type">{charger.connectorType}</h2>
                    <p className="font-bold" data-test-id="price">Price: </p><p> {charger.price}</p>
                    <p className="font-bold">Connector Type: </p><p> {charger.connectorType}</p>
                    <p className="font-bold">Charging Speed: </p><p> {charger.chargingSpeed}</p>
                    <p className="font-bold">Status: </p><p className={`${getColorChargers(charger.status)}`}> {charger.status}</p>
                    <p className="font-bold">Reservations for the next 5 days: </p><p>{reservations[charger.id]?.length ?? 0}</p>
                    <div className="flex flex-col gap-2">
                      <div className="flex gap-2 w-full">
                        <button className="btn btn-info flex-1" onClick={() => { modalRef.current?.showModal(); setSelectedCharger(charger.id) }}>Availability</button>
                        <button className="btn btn-primary flex-1" onClick={() => handleUnlockCharger(charger.id)} disabled={!canUnlockCharger(charger.id, charger.status)}>Unlock</button>
                      </div>
                      <button className="btn btn-success w-full">Reserve now</button>
                    </div>
                  </div>
                </div>
            )}
          </div >

          <dialog className="modal" ref={modalRef}>
            <div className="modal-box">
              <form method="dialog">
                <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
              </form>
              <h3 className="font-bold text-lg">Charger Reservations</h3>
              {
                selectedCharger && reservations[selectedCharger].length > 0 ? (
                  <div className="overflow-x-auto">
                    <table className="table table-zebra">
                      <thead>
                        <tr>
                          <th></th>
                          <th>Start Date</th>
                          <th>End Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        {
                          selectedCharger && reservations[selectedCharger].map((reservation, i) => (
                            <tr key={reservation.id}>
                              <th>{i}</th>
                              <td>{format(reservation.startTimestamp, "dd-MM-yyyy HH:mm")}</td>
                              <td>{format(reservation.endTimestamp, "dd-MM-yyyy HH:mm")}</td>
                            </tr>
                          ))
                        }
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="py-4">
                    There are no reservations for this charger in the next 5 days
                  </div>
                )
              }
            </div>
          </dialog>
        </NavLayout>
      </div >
    )
}

export default StationDetails;

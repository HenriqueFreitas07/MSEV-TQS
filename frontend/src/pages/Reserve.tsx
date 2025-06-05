import NavLayout from "../layouts/NavLayout.js";
import { useState, useEffect, useRef } from "react";
import { useParams } from "react-router";
import { ChargerService, ReservationService } from "../requests.js";
import dayjs from "dayjs";
import utc from 'dayjs/plugin/utc';
dayjs.extend(utc);
import type { Reservation } from "../types/Reservation.js";
import type { Charger } from "../types/Charger.js";
import { TbGasStation, TbGasStationOff } from "react-icons/tb";
import { useAuth } from "../contexts/auth.js";

interface TimeSlot {
  start: string;
  end: string;
}

const generateTimeSlots = (startHour: number, endHour: number): TimeSlot[] => {
  const slots: TimeSlot[] = [];
  let current = dayjs().hour(startHour).minute(0).second(0);

  const end = dayjs().hour(endHour).minute(0).second(0);

  while (current.isBefore(end)) {
    const next = current.add(30, "minute");
    slots.push({
      start: current.format("HH:mm"),
      end: next.format("HH:mm"),
    });
    current = next;
  }

  return slots;
};

const generateDaysWithSlots = (days: number): { date: string; slots: TimeSlot[] }[] => {
  const results = [];
  for (let i = 0; i < days; i++) {
    const date = dayjs().add(i, "day");
    results.push({
      date: date.format("YYYY-MM-DD"),
      slots: generateTimeSlots(9, 17), // 9 AM to 5 PM
    });
  }
  return results;
};



function Reserve() {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [charger, setCharger] = useState<Charger>();
  const [car, setCar] = useState<{ date: string, slots: TimeSlot }[]>([]);
  const [slots, setSlots] = useState<{ date: string, slots: TimeSlot }[]>([]);
  const modalRef = useRef<HTMLDialogElement | null>(null);

  const daysWithSlots = generateDaysWithSlots(5);

  const params = useParams();
  const auth = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      setCharger(await ChargerService.getChargerById(params.postId!));
      const reservationsResponse = await ReservationService.getReservationsForCharger(params.postId!);

      setReservations(reservationsResponse);
    }
    fetchData();
  }, [setReservations]);

  const handleReserve = async (e: React.MouseEvent) => {
    e.preventDefault();
    modalRef.current?.close();
    if (auth.user && charger) {

      for (const reserve of slots) {
        const startTimestamp = dayjs.utc(reserve.date + " " + reserve.slots.start, "YYYY-MM-DD HH:mm").toISOString();
        const endTimestamp = dayjs.utc(reserve.date + " " + reserve.slots.end, "YYYY-MM-DD HH:mm").toISOString();
        const reservation = {
          user: auth.user,
          startTimestamp: startTimestamp,
          endTimestamp: endTimestamp,
          charger: charger
        };
        const response = await ReservationService.createReservation(reservation);

        setReservations((prev) => [...prev, response])
      }

      setCar([]);
    }

  }

  const concat = () => {
    let conc = true;
    let change = [...car];

    while (conc) {
      conc = false
      for (const reserve of change) {
        for (const reserve2 of change) {
          if (reserve === reserve2 || reserve.date !== reserve2.date) {
            break;
          }
          else if (reserve.slots.start === reserve2.slots.end) {
            change = change.filter(
              (r) =>
                !(
                  (r.date === reserve.date && r.slots.start === reserve.slots.start && r.slots.end === reserve.slots.end) ||
                  (r.date === reserve2.date && r.slots.start === reserve2.slots.start && r.slots.end === reserve2.slots.end)
                )
            );

            change.push({ date: reserve.date, slots: { start: reserve2.slots.start, end: reserve.slots.end } })
            conc = true
          }
          else if (reserve2.slots.start === reserve.slots.end) {
            change = change.filter(
              (r) =>
                !(
                  (r.date === reserve.date && r.slots.start === reserve.slots.start && r.slots.end === reserve.slots.end) ||
                  (r.date === reserve2.date && r.slots.start === reserve2.slots.start && r.slots.end === reserve2.slots.end)
                )
            );
            change.push({ date: reserve.date, slots: { start: reserve.slots.start, end: reserve2.slots.end } });
            conc = true
          }
        }
      }
    }
    setSlots(change);
  }


  const addToCar = (slot: TimeSlot, day: string) => {
    const exists = car.some(
      (item) => item.date === day && item.slots.start === slot.start && item.slots.end === slot.end
    );
    if (exists) {
      setCar((prev) =>
        prev.filter(
          (item) =>
            !(item.date === day && item.slots.start === slot.start && item.slots.end === slot.end)
        )
      );
    } else {
      setCar((prev) => [...prev, { date: day, slots: slot }]);
    }
  };

  return (
    <div className="items-center justify-center">
      <NavLayout title="Reserve">
        <div className="flex justify-between p-6 ml-12 mr-12">
          <div className="flex">
            <div className="pr-6">Station:
              {charger?.station.status === "ENABLED" ? (
                <TbGasStation className="text-green-500" size={40} />
              ) : (
                <TbGasStationOff className="text-red-500" size={40} />
              )}

            </div>
            <div>
              <div className="flex">
                <p className="font-bold mr-3" data-test-id="name">Name: </p>
                <p> {charger?.station.name}</p>
              </div>
              <div className="flex">
                <p className="font-bold mr-3">Address: </p>
                <p> {charger?.station?.address}</p>
              </div>
            </div>
          </div>
          <div className="flex">
            <div className="pr-6">Charger:</div>
            <div>
              <div className="flex">
                <p className="font-bold mr-3" data-test-id="price">Price: </p>
                <p> {charger?.price}</p>
              </div>
              <div className="flex">
                <p className="font-bold mr-3">Connector Type: </p>
                <p> {charger?.connectorType}</p>
              </div>
              <div className="flex">
                <p className="font-bold mr-3">Charging Speed: </p>
                <p> {charger?.chargingSpeed}</p>
              </div>
            </div>
          </div>
          <div>
            <button className="btn btn-lg w-full bg-green-600 text-white" onClick={() => { modalRef.current?.showModal(); concat(); }}>Reserve now</button>
          </div>
        </div>
        <dialog className="modal" ref={modalRef}>
          <div className="modal-box">
            <form method="dialog">
              <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
            </form>
            <h3 className="font-bold text-lg">Slots</h3>
            {
              car.length > 0 ? (
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
                        slots.map((reservation, i) => (
                          <tr key={i}>
                            <th>{reservation.date}</th>
                            <td>{reservation.slots.start}</td>
                            <td>{reservation.slots.end}</td>
                          </tr>
                        ))
                      }
                    </tbody>
                  </table>
                  <div className="justify-center items-center mt-3">
                    <form>
                      <button className="btn btn-md  bg-green-600 text-white" onClick={(e) => handleReserve(e)}>Confirm Reserve</button>
                    </form>
                  </div>
                </div>
              ) : (
                <div className="py-4">
                  There are no slots selected! Please select a slot before trying to create a reserve!
                </div>
              )
            }
          </div>
        </dialog>
        <div className="flex">
          <div style={{ width: "5%" }}></div>
          <div className=" rounded-xl p-4 shadow-md bg-base-200" style={{ width: "90%" }}>
            <h1 className="text-2xl font-bold text-center mb-6 text-blue-400">Available Time Slots</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 ">
              {daysWithSlots.map((day, index) => (
                <div key={index} className=" rounded-xl p-4 shadow-md">
                  <h2 className="text-lg font-semibold mb-3 text-center text-blue-400">{day.date}</h2>
                  <div className="flex flex-col gap-2">
                    {day.slots.map((slot, idx) => (
                      <div key={idx}>
                        {dayjs().date() == Number(day.date.substring(day.date.length - 2)) && dayjs().minute() > Number(slot.start.substring(3, 5)) &&
                          dayjs().hour() >= Number(slot.start.substring(0, 2))
                          || dayjs().date() == Number(day.date.substring(day.date.length - 2)) && dayjs().hour() > Number(slot.start.substring(0, 2))
                          ?
                          <button key={idx} className="btn btn-sm w-full">
                          </button>
                          :
                          <>
                            {!reservations.some(
                              (r) =>
                              (
                                (
                                  dayjs(r.startTimestamp).format("HH:mm") < slot.end &&
                                  dayjs(r.endTimestamp).format("HH:mm") > slot.start) &&
                                dayjs(r.startTimestamp).format("YYYY-MM-DD") === day.date
                              )

                            ) ?
                              <button key={idx} className={`btn btn-sm w-full ${car.some(item => item.date === day.date && item.slots.start === slot.start && item.slots.end === slot.end) ? "bg-red-400" : "bg-blue-400"}`}
                                onClick={() => addToCar(slot, day.date)}>
                                {slot.start} - {slot.end}
                              </button>
                              :
                              <button className="btn btn-sm w-full bg-gray-400">
                                {slot.start} - {slot.end}
                              </button>
                            }
                          </>
                        }
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

      </NavLayout>
    </div>
  )

}

export default Reserve;
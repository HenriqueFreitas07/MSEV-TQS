import NavLayout from "../layouts/NavLayout.js";
import { useState, useEffect } from "react";
import { useParams } from "react-router";
import { ReservationService } from "../requests.js";

import dayjs from "dayjs";
import type { Reservation } from "../types/reservation.js";

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
      date: date.format("dddd, MMM D"),
      slots: generateTimeSlots(9, 17), // 9 AM to 5 PM
    });
  }
  return results;
};



function Reserve() {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const daysWithSlots = generateDaysWithSlots(5);

  const params = useParams();

  useEffect(() => {
    const fetchData = async () => {
      const reservationsResponse = await ReservationService.getReservations(params.postId!, params.postId!);

      setReservations(reservations);

      for (const reserve of reservationsResponse) {
        continue;
      }
    }
    fetchData();
  }, []);

  return (
    <div className="items-center justify-center">
      <NavLayout title="Reserve">
        <div className="flex justify-center p-6 ">
          <div>Station: </div>
          <div>Charger: </div>

        </div>
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
                          <button key={idx} className="btn btn-sm w-full bg-blue-400">
                            {slot.start} - {slot.end}
                          </button>
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
import NavLayout from "../layouts/NavLayout.js";
import { useState, useEffect, useRef } from "react";
import { ReservationService } from "../requests.js";
import dayjs from "dayjs";
import type { Reservation } from "../types/Reservation.js";
import { useAuth } from "../contexts/auth.js";
import Swal, { type SweetAlertIcon, type SweetAlertResult } from 'sweetalert2';




function UserReserves() {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const modalRef = useRef<HTMLDialogElement | null>(null);

  const auth = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      if (!auth.user?.id) {
        setReservations([]);
        console.log(auth.user)
        return;
      }
      const reservationsResponse = await ReservationService.getReservationsForUSer(auth.user.id);
      setReservations(reservationsResponse);
    }
    fetchData();
  }, [setReservations]);

  const deleteReserve = async (e: React.MouseEvent, reservationID: string) => {
    e.preventDefault();
    try {
      await ReservationService.cancelReservation(reservationID);
      setReservations((prev) => prev.filter(r => r.id !== reservationID));
      Swal.fire({
        icon: "success",
        title: "Reservation deleted with success!",
        timer: 1000
      });
    } catch {
      Swal.fire({
        icon: "error",
        title: "Error deleting reservation",
        text: "Please try again.",
      });
    }
  }


  return (
    <div className="items-center justify-center">
      <NavLayout title="Reserve">
        {reservations.length === 0 ?
          <div className="text-center">

            <h1 className="font-bold text-5xl mt-64">You don't have any reservations!</h1>
            <br />
            <h3>To make a new reservation go to the stations page;</h3>
            <h3>Select a station and then select a charger by clicking on the "Reserve now" button;</h3>
            <h3>Then select the slots for your reserve;</h3>
            <h3 className="mb-44">Complete this process by clicking on the "Reserve now" and confirming!</h3>

          </div>
          :
          reservations.map((reserve, idx) => (
            <div className="p-4 ml-12 mr-12 rounded-xl mt-3 mb-3 shadow-md bg-base-200" key={idx}>
              <div className="text-end text-xl text-red-500"><button onClick={(e) => deleteReserve(e, reserve.id)}>X</button></div>
              <div className="justify-between flex">
                <div className="flex" style={{ "width": "50%" }} >
                  <div className="pr-6">Reservation:</div>
                  <div>
                    <div className="flex">
                      <p className="font-bold mr-3">Id: </p>
                      <p> {reserve.id}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">Start: </p>
                      <p> {dayjs(reserve.startTimestamp).format("YYYY-MM-DD HH:mm")}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">End: </p>
                      <p> {dayjs(reserve.endTimestamp).format("YYYY-MM-DD HH:mm")}</p>
                    </div>
                  </div>
                </div>
                {reserve.used ?
                  <p className="font-bold mr-3 text-red-500" > Used </p>
                  :
                  <p className="font-bold mr-3 text-green-500" > Not Used </p>
                }

                <div className="flex" >
                  <div className="pr-6">Charger:</div>
                  <div>
                    <div className="flex">
                      <p className="font-bold mr-3" data-test-id="price">Price: </p>
                      <p> {reserve.charger.price}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">Connector Type: </p>
                      <p> {reserve.charger.connectorType}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">Charging Speed: </p>
                      <p> {reserve.charger.chargingSpeed}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">Station Name: </p>
                      <p> {reserve.charger.station.name}</p>
                    </div>
                    <div className="flex">
                      <p className="font-bold mr-3">Address: </p>
                      <p> {reserve.charger.station.address}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div >
          ))
        }
      </NavLayout >
    </div >
  )

}

export default UserReserves;
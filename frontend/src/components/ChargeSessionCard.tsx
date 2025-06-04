import { format } from "date-fns";
import type { ChargeSession } from "../types/charge-session";
import { ChargerService } from "../requests";

type Props = {
  session: ChargeSession;
  endSession: () => void;
}

export function ChargeSessionCard({ session, endSession }: Props) {
  async function handleLockCharger(chargerId: string) {
    await ChargerService.lockCharger(chargerId);
    endSession();
  }

  return (
    <div className="flex flex-col p-8 gap-2 rounded-md border-neutral-200 shadow">
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

      {
        session.endTimestamp === null && (
          <button className="btn btn-primary" onClick={() => handleLockCharger(session.charger.id)}>
            Lock Charger
          </button>
        )
      }
    </div>
  )
}

import { useEffect, useState } from "react";
import type { ChargeSession } from "../types/charge-session";
import { ChargerService } from "../requests";
import NavLayout from "../layouts/NavLayout";
import { ChargeSessionCard } from "../components/ChargeSessionCard";

export default function ChargeSessions() {
  const [sessions, setSessions] = useState<ChargeSession[]>([]);
  const [activeOnly, setActiveOnly] = useState(true);

  useEffect(() => {
    ChargerService.getChargeSessions().then(r => setSessions(r));
  }, []);

  function endSession(sessionId: string) {

    setSessions(prev => {
      const session = prev.find(s => s.id === sessionId)!;
      session.endTimestamp = new Date().toISOString();

      return [
        ...prev.filter(s => s.id !== sessionId),
        session
      ]
    })
  }

  return (
    <NavLayout title="MSEV" footer={false}>
      <div className="p-8">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold mb-2">Charge Sessions</h2>
          <div className="flex items-center justify-center gap-2">
            <p>Active Sessions Only:</p>
            <input type="checkbox" checked={activeOnly} onChange={() => setActiveOnly(prev => !prev)} className="toggle toggle-primary" />
          </div>
        </div>
        <hr />

        <div className="grid lg:grid-cols-5 md:grid-cols-3 grid-cols-2 gap-4 mt-6">
          {
            sessions.filter(s => !activeOnly || s.endTimestamp === null).map(s => <ChargeSessionCard key={s.id} session={s} endSession={() => endSession(s.id)} />)
          }
        </div>
      </div>
    </NavLayout>
  )
}

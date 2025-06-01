import type { Charger } from "./Charger";
import type { User } from "./user";

export type ChargeSession = {
  id: string;
  startTimestamp: string;
  endTimestamp: string | null;
  consumption: number;
  chargingSpeed: number;
  user: User;
  charger: Charger;
}

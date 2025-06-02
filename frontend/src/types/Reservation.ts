import type { Charger } from "./Charger";
import type { User } from "./user";

export type Reservation = {
  id: string;
  user: User;
  charger: Charger;
  startTimestamp: string;
  endTimestamp: string;
  used: boolean;
}

export type createReservation = {
  user: User;
  charger: Charger;
  startTimestamp: string;
  endTimestamp: string;
}
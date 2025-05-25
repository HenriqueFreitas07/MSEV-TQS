import type {Station} from "../types/Station";

export type Charger = {
  id: string,
  station: Station,
  connectorType: string,
  price: number,
  chargingSpeed: number,
  status: string
}
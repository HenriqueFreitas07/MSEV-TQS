import { type AdvancedMarkerProps } from '@vis.gl/react-google-maps';
import type { JSX } from 'react/jsx-runtime';

export type markerProps={
  markerOptions:AdvancedMarkerProps,
  //make a callback function type with optional params and return type
  callback?:(params?:unknown)=>void,
  icon?:JSX.Element, 
}
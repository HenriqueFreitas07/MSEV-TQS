import {
  Pin,
  APIProvider,
  Map,
  AdvancedMarker,
  useMap
} from '@vis.gl/react-google-maps';
import { useEffect } from 'react';
import { type markerProps } from '../types/MapTypes';

type Props = {
  markers?: markerProps[];
  center?: google.maps.LatLngLiteral;
  zoom?: number;
};

function MapContent({ markers }: { markers?: markerProps[] }) {
  const map = useMap(); // access the map instance

  useEffect(() => {
    if (!map || !markers || markers.length === 0) return;

    const bounds = new google.maps.LatLngBounds();
    markers.forEach((m) => {
      if(m.markerOptions.position !=null )
      {
        bounds.extend(m.markerOptions.position);
      }
    });

    map.fitBounds(bounds);
  }, [map, markers]);

  return (
    <>
      {markers?.map((markerProps, index) => (
        <AdvancedMarker 
        onClick={markerProps.callback} // for redirecting to the station page
        key={index} 
        {...markerProps.markerOptions}>
          {markerProps.icon !== null ? (
            markerProps.icon
          ) : (
            <Pin
              background="#0f9d58"
              borderColor="#006425"
              glyphColor="#60d98f"
            />
          )}
        </AdvancedMarker>
      ))}
    </>
  );
}

export default function MapComponent({
  markers,
  zoom = 6,
  center = { lat: 50.8503, lng: 4.3517 }
}: Props) {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  return (
    <APIProvider apiKey={apiKey} libraries={['places']}>
      <Map
        defaultCenter={center}
        defaultZoom={zoom}
        mapId="456871e505d11ddac458556d"
        colorScheme="DARK"
        gestureHandling="greedy"
        style={{ width: '100%', height: '100%' }}
      >
        <MapContent markers={markers} />
      </Map>
    </APIProvider>
  );
}

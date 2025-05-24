import { Pin, APIProvider, Map, AdvancedMarker, type AdvancedMarkerProps } from '@vis.gl/react-google-maps';

type Props = {
  markers?: AdvancedMarkerProps[];
  center?: google.maps.LatLngLiteral;
  zoom?: number;
};

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
        colorScheme='DARK'
        gestureHandling={'cooperative'}
        style={{ width: '100%', height: '100%' }}
      >
        {markers?.map((markerProps, index) => (
          <AdvancedMarker key={index} {...markerProps} >
            <Pin
              background={'#0f9d58'}
              borderColor={'#006425'}
              glyphColor={'#60d98f'}
            />
          </AdvancedMarker>
        ))}
      </Map>
    </APIProvider>
  );
}

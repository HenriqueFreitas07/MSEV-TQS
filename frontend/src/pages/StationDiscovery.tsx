import NavLayout from '../layouts/NavLayout'
import MapComponent from '../components/MapComponent';

export default function StationsDiscovery() {
  const markers = [
    {
      position: { lat: 50.8503, lng: 4.3517 },
      title: 'Brussels',
      label: 'A',
      gmpClickable: true,
      icon: {
        url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
      },
    },
    {
      position: { lat: 51.0447, lng: 3.7344 },
      title: 'Antwerp',
      label: 'B',
      icon: {
        url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
      },
    },
  ];
  return (
    <NavLayout title='Stations'>
        <div className='flex flex-col h-[500px] items-center justify-center '>
            <MapComponent markers={markers} />
        </div>
    </NavLayout>
  )
}
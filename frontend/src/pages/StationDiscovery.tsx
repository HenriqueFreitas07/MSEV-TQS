import NavLayout from "../layouts/NavLayout";
import MapComponent from "../components/MapComponent";
import { useEffect, useRef, useState } from "react";
import { TbGasStationOff } from "react-icons/tb";
import { TbGasStation } from "react-icons/tb";
import { MdPersonSearch } from "react-icons/md";
import type { markerProps } from "../types/MapTypes";
import { GoogleService, StationService } from "../requests";
import type { autoComplete } from "../types/PlacesTypes";

export default function StationsDiscovery() {
  const [nameSearch, setNameSearch] = useState(true);
  const [currentText, setCurrentText] = useState("");

  const [selectedMarker, setSelectedMarker] = useState<markerProps>();
  const [markers, setMarkers] = useState<markerProps[]>([]);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [autocompleteList, setAutocompleteList] = useState<autoComplete[]>([]);
  const [stations, setStations] = useState([
    {
      id: 1,
      name: "Station 1",
      address: "123 Main St, Cityville",
      latitude: 40.7128,
      longitude: -8.506,
      status: "ENABLED",
    },
    {
      id: 2,
      name: "Station 2",
      address: "Aveiro",
      latitude: 40.7128,
      longitude: -8.64554,
      status: "DISABLED",
    },
  ]);
  const handlerRadio = (
    e: React.ChangeEvent<HTMLInputElement>,
    value: boolean
  ) => {
    setNameSearch(value);
  };
  const handlerAutocomplete = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setCurrentText(value);
    if (value.length < 3) {
      setStations([]);
      return;
    }
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(() => {
      if (nameSearch) {
        StationService.searchByName(value)
          .then((data) => {
            console.log(data);
          })
          .catch((err) => {
            console.log(err);
          });
      // if search is by name then i show up the google places api autocomplete 
      GoogleService.autocompletePlaces(value)
        .then((data) => {
          type SuggestionItem = {
            text: string;
            placePrediction: { text: { text: string }; placeId: string };
          };
          const suggestions = data.suggestions.map((item: SuggestionItem) => ({
            text: item.placePrediction.text.text,
            place_id: item.placePrediction.placeId,
          }));
          setAutocompleteList(suggestions);
        })
        .catch((err) => {
          console.log(err);
        });

      } else {
        StationService.searchByAddress(value)
          .then((data) => {
            console.log(data);
          })
          .catch((err) => {
            console.log(err);
          });
      }
    }, 1000);
  };
  const handleSelectSugg = (index: number) => {
    const selectedSuggestion = autocompleteList[index];
    setCurrentText(selectedSuggestion.text);
    setAutocompleteList([]);
    setStations([]);
    GoogleService.getPlace(selectedSuggestion.place_id)
      .then((data) => {
        setSelectedMarker({
          markerOptions: {
            position: {
              lat: data.location.latitude,
              lng: data.location.longitude,
            },
            title: data.displayName.text,
          },
          icon: <MdPersonSearch className="text-green-500" size={40} />,
        });
      })
      .catch((err) => {
        console.log(err);
      });
      // get all the stations and set for all of them that are in a radius of 5km
      StationService.getAllStations().then((data)=>{
        setStations((prevStations) =>
          prevStations.filter(
            (station) =>
              station.latitude <= data.location.latitude + 0.05 &&
              station.latitude >= data.location.latitude - 0.05 &&
              station.longitude <= data.location.longitude + 0.05 &&
              station.longitude >= data.location.longitude - 0.05
          )
        );
      })


  };

  const UpdateStationMarkers = () => {
    setMarkers(
      stations.map((station) => ({
        markerOptions: {
          id: station.id,
          position: {
            lat: station.latitude,
            lng: station.longitude,
          },
          title: station.name,
          description: station.address,
        },
        icon:
          station.status === "ENABLED" ? (
            <TbGasStation className="text-green-500" size={40} />
          ) : (
            <TbGasStationOff className="text-red-500" size={40} />
          ),
      }))
    );
  };
  useEffect(() => {
    UpdateStationMarkers();
  }, [stations]);

  return (
    <NavLayout title="Stations">
      <div className="w-full p-3 my-2 grid text-center grid-cols-1">
        <h1 className="text-3xl font-bold">Station Discovery</h1>
        <p className="text-lg">Discover charging stations near you</p>
        <div className="flex flex-col w-full p-4 relative">
          <div className="w-[50%] p-3 flex mx-auto justify-around">
            <div className="flex p-3 gap-2">
              <input
                type="radio"
                name="radio-1"
                onChange={(e) => handlerRadio(e, true)}
                className="radio"
                defaultChecked
              />
              By Name
            </div>
            <div className="flex p-3 gap-2">
              <input
                type="radio"
                name="radio-1"
                onChange={(e) => handlerRadio(e, false)}
                className="radio"
              />
              By Address
            </div>
          </div>
          <label className="input w-4/5 mx-auto">
            <svg
              className="h-[1em] opacity-50"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
            >
              <g
                strokeLinejoin="round"
                strokeLinecap="round"
                strokeWidth="2.5"
                fill="none"
                stroke="currentColor"
              >
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.3-4.3"></path>
              </g>
            </svg>
            <input
              type="search"
              className="w-full"
              value={currentText}
              onChange={handlerAutocomplete}
              placeholder="Search"
            />
          </label>
          <div className="w-4/5 mx-auto mt-3 absolute left-[50%] translate-x-[-50%] bottom-0 translate-y-[100%] z-10">
            <ul className="list bg-base-100 rounded-box shadow-sm shadow-neutral-500">
              {autocompleteList.map((item, index) => (
                <li
                  onClick={() => handleSelectSugg(index)}
                  key={index}
                  className="list-row hover:bg-white hover:text-black cursor-pointer flex justify-between items-center p-3"
                >
                  <div className="text-left">
                    <div>{item.text}</div>
                    <div className="text-xs uppercase font-semibold opacity-60">
                      {" "}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
      <div className="flex flex-col h-[600px] items-center justify-center ">
        <MapComponent
          markers={[...markers, selectedMarker].filter(
            (m): m is markerProps => m !== undefined
          )}
        />
      </div>
    </NavLayout>
  );
}

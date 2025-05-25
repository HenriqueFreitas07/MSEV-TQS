import NavLayout from "../layouts/NavLayout";
import MapComponent from "../components/MapComponent";
import { useEffect, useRef, useState } from "react";
import { TbGasStationOff } from "react-icons/tb";
import { TbGasStation } from "react-icons/tb";
import { MdPersonSearch } from "react-icons/md";
import type { markerProps } from "../types/MapTypes";
import { GoogleService, StationService } from "../requests";
import type { autoComplete } from "../types/PlacesTypes";
import type { Station } from "../types/Station";
import { showAlert } from "../alerts";
import { useNavigate } from "react-router";

export default function StationsDiscovery() {
  const [nameSearch, setNameSearch] = useState<number>(0);
  const [currentText, setCurrentText] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [selectedMarker, setSelectedMarker] = useState<markerProps>();
  const [markers, setMarkers] = useState<markerProps[]>([]);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [autocompleteList, setAutocompleteList] = useState<autoComplete[]>([]);
  const [stations, setStations] = useState<Station[]>([]);
  const handlerRadio = (
    value: 0 | 1 | 2
  ) => {
    setNameSearch(value);
  };

  const UpdateStationMarkers = () => {
    //get all the stations and set markers for them
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
        callback: () => {
          console.log("Marker clicked:", station.id);
          navigate(`/stations/${station.id}`);
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
  const handlerAutocomplete = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setLoading(true);
    setCurrentText(value);
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(() => {
      if (nameSearch == 0) {
        StationService.searchStationByName(value)
          .then((data) => {
            setAutocompleteList(
              data.map((item) => ({
                text: item.name,
                place_id: item.id,
              }))
            );
            setStations(data);
            UpdateStationMarkers();
          })
          .catch((err) => {
            console.log(err);
          });

      } else if (nameSearch === 1) {
        StationService.searchStationByAddress(value)
          .then((data) => {
            setAutocompleteList(
              data.map((item) => ({
                text: item.name,
                place_id: item.id,
              }))
            );
            setStations(data);
            UpdateStationMarkers();
          })
          .catch((err) => {
            console.log(err);
          });
      }
      else if (nameSearch === 2) {
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
      }
      setLoading(false);
    }, 1000);
  };
  const handleSelectSugg = (index: number) => {
    if (index < 0 || index >= autocompleteList.length) {
      showAlert("Error", "Invalid selection", "error");
      return;
    }

    const selectedSuggestion = autocompleteList[index];
    setMarkers([]);
    setAutocompleteList([]);
    if (nameSearch === 2) {
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
          // set the station markers within 5km of the selected place
          const nearbyStations = stations.filter((station) => {
            const distance = Math.sqrt(
              Math.pow(station.latitude - data.location.latitude, 2) +
              Math.pow(station.longitude - data.location.longitude, 2)
            );
            return distance <= 0.05; // Assuming 0.05 degrees is roughly 5km
          });
          console.log("Nearby Stations:", nearbyStations);
          setStations(nearbyStations);
          UpdateStationMarkers();
        })
        .catch((err) => {
          console.log(err);
        });
      setCurrentText(selectedSuggestion.text);
      setAutocompleteList([]);
      return;
    }
    setCurrentText(selectedSuggestion.text);
    const selectedStation: Station | undefined = stations.find(
      (station) => station.id === selectedSuggestion.place_id
    );
    if (!selectedStation) {
      showAlert("Error", "Station not found", "error");
      return;
    }
    setStations([selectedStation]);
    UpdateStationMarkers();
  };




  useEffect(() => {
    const response = StationService.getAllStations()
    response.then((data) => {
      setStations(data);
    }).catch((err) => {
      console.error("Error fetching stations:", err);
      showAlert("Error", "Failed to fetch stations", "error");
    });
  }, []);

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
                onChange={() => handlerRadio(0)}
                className="radio"
                defaultChecked
              />
              By Name
            </div>
            <div className="flex p-3 gap-2">
              <input
                type="radio"
                name="radio-1"
                onChange={() => handlerRadio(1)}
                className="radio"
              />
              By Address
            </div>
            <div className="flex p-3 gap-2">
              <input
                type="radio"
                name="radio-1"
                onChange={() => handlerRadio(2)}
                className="radio"
              />
              By Google Places
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
              {loading &&
                <li>
                  <span className="loading loading-dots loading-xl"></span>
                </li>
              }
              {currentText !== "" && autocompleteList.length === 0 && !loading && (
                <li className="list-row p-3 text-center">
                  No suggestions found
                </li>
              )}
              {!loading && autocompleteList.map((item, index) => (
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

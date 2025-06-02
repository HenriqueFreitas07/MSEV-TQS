import http from "k6/http";
import { check } from "k6";

export const options = {
    stages: [
        { duration: '30s', target: 120 },
        { duration: '30s', target: 220 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1100'],
        http_req_failed: ['rate<0.01'],
        "checks": ["rate>0.98"],
    },
};

const BASE_URL = "http://localhost/api/v1";
const AUTH_TOKEN = __ENV.AUTH_TOKEN;

function getStations() {
    const res = http.get(`${BASE_URL}/stations`, {
        headers: {
           Cookie: `accessToken=${AUTH_TOKEN}`,
        },
    });
    if (res.status !== 200) {
        console.error(`Failed to fetch stations: ${res.status} ${res.status_text}`);
        return [];
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    console.log(`Stations: ${res.json().length}`);
    return res.json().map(station => station.id);
}

function getChargers(id) {

    const res = http.get(`${BASE_URL}/chargers/station/${id}`, {
        headers: {
            Cookie: `accessToken=${AUTH_TOKEN}`,
        },
    });
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    console.log(`Chargers for station ${id}: ${res.json().length}`);
}


function getStationsByAddress(location) {
    if (!location) {
        console.error("Location is required to search for stations by address");
        return [];
    }
    let res = http.get(`${BASE_URL}/stations/search-by-address` + `?address=${encodeURIComponent(location)}`, {
        headers: {
           Cookie: `accessToken=${AUTH_TOKEN}`,
        },
    });
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

}



export default function () {
    const stations = getStations();
    if (stations.length === 0) {
        console.error("No stations found");
        return;
    }

    const randomStationId = stations[Math.floor(Math.random() * stations.length)];
    getChargers(randomStationId);

    
}
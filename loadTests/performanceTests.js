import http from "k6/http";
import { check } from "k6";

const TEST_TYPE = __ENV.TEST_TYPE || 'spike';

const thresholds = {
    http_req_duration: ['p(95)<1100'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.98'],
};

export const options = TEST_TYPE === 'spike' ? {
    stages: [
        { duration: '5s', target: 10 },
        { duration: '5s', target: 300 },
        { duration: '10s', target: 300 },
        { duration: '5s', target: 0 },
    ],
    thresholds,
} : TEST_TYPE === 'ramp' ? {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '30s', target: 0 },
    ],
    thresholds,
} : {
    vus: 50,
    duration: '5m',
    thresholds,
};


const BASE_URL = "http://localhost/api/v1";

function getStations(authHeader) {
    const res = http.get(`${BASE_URL}/stations`, authHeader);
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

function loginAndGetToken() {

    const payload = JSON.stringify({
        email: "test@example.com",
        password: "testpass"
    });

    const res = http.post(`${BASE_URL}/login`, payload, {
        headers: { "Content-Type": "application/json" },
    });

    check(res, {
        'login status is 204': (r) => r.status === 204,
        'has Set-Cookie header': (r) => !!r.headers['Set-Cookie'],
    });

    const cookies = res.headers['Set-Cookie'];
    const match = cookies.match(/accessToken=([^;]+)/);
    if (match && match[1]) {
        const token = match[1];
        console.log(`Got auth token: ${token}`);
        return token;
    } else {
        console.error("Auth token not found in cookies");
        return null;
    }
}

function signupUser() {
    const payload = JSON.stringify({
        name: "testuser",
        email: "test@example.com",
        password: "testpass"
    });

    const res = http.post(`${BASE_URL}/signup`, payload, {
        headers: { "Content-Type": "application/json" },
    });

    check(res, {
        'signup status is 204 or 400 (duplicate)': (r) => r.status === 204 || r.status === 400,
    });

    console.log(`Signup result: ${res.status}`);
}

function logout() {
    const res = http.post(`${BASE_URL}/logout`);
    check(res, {
        'logout status is 204': (r) => r.status === 204,
    });
}

function getChargers(id, authHeader) {

    const res = http.get(`${BASE_URL}/chargers/station/${id}`,authHeader);
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
    console.log(`Stations found for address "${location}": ${res.json().length}`);
    console.log(`Response status: ${res.status} ${res.status_text}`);
    check(res, {
        'status is 200': (r) =>  r.status === 200,
    });

}

export default function () {

    //Its okay to repeat signup for performance tests, it will just return 400 if user already exists
    signupUser();

    // Authenticate user
    const token = loginAndGetToken();
    if (!token) {
        console.error("Auth failed. Exiting.");
        return;
    }

    const authHeader = {
        headers: {
            Cookie: `accessToken=${token}`
        }
    };

    const stations = getStations(authHeader);
    if (stations.length === 0) {
        console.error("No stations found");
        return;
    }

    const randomStationId = stations[Math.floor(Math.random() * stations.length)];
    getChargers(randomStationId, authHeader);

    logout();
    
    // Search for stations by a random real address
    //const addresses = [
    //    "1600 Amphitheatre Parkway, Mountain View, CA",
    //    "Rua da Universidade de Aveiro",
    //    "221B Baker Street, London, UK",
    //]
    //const randomAddress = addresses[Math.floor(Math.random() * addresses.length)];
    //getStationsByAddress(randomAddress);
 
}
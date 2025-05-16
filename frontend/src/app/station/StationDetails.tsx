
const staticData = [{
    connectorType: "Type 2",
    price: 0.25,
    chargingSpeed: 22,
    status: "AVAILABLE",
},
{
    connectorType: "CCS",
    price: 0.45,
    chargingSpeed: 150,
    status: "IN_USE",
},
{
    connectorType: "CHAdeMO",
    price: 0.30,
    chargingSpeed: 50,
    status: "OUT_OF_ORDER",
}]

function StationDetails() {

    return (
        <div className="items-center justify-center">
            <p className="text-4xl p-4 text-center">Chargers</p>
            <div className="flex grid grid-cols-2 ">
                {staticData.map(
                    (charger) =>
                        <div className="card p-3 m-4 card-side card-sm bg-base-100 shadow-sm" >
                            <figure className="w-2/3">
                                <img
                                    src="https://www.ayvens.com/-/media/leaseplan-digital/pt/blog/closeupofachargingelectriccarjpgs1024x1024wisk20ckeuxnuhyqyacvxmhzorzeru0rnlkt8gmrr91setym.jpg?rev=5b11d8a3cb184493bb742f8ae7e1a41f"
                                    alt="Charger"
                                />
                            </figure>
                            <div className="card-body">
                                <h2 className="card-title">{charger.connectorType}</h2>
                                <p className="font-bold">Price: </p><p> {charger.price}</p>
                                <p className="font-bold">Connector Type: </p><p> {charger.connectorType}</p>
                                <p className="font-bold">Charging Speed: </p><p> {charger.chargingSpeed}</p>
                                <p className="font-bold">Status: </p><p> {charger.status}</p>
                                <div className="card-actions justify-end">
                                    <button className="btn btn-primary">Reserve now</button>
                                </div>
                            </div>
                        </div>
                )}

            </div >
        </div >
    )
}

export default StationDetails;

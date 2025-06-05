import NavLayout from "../layouts/NavLayout"
import hero_image from  "../assets/hero_charging_tesla.jpg"
export default function Home() {
    return (
        <>
            <NavLayout title="MSEV">
                <div
                    style={
                        {
                            backgroundImage: `url(${hero_image})`
                        }
                    }
                    className="hero min-h-screen"
                >
                    <div className="hero-overlay"></div>
                    <div className="hero-content text-neutral-content text-center">
                        <div className="max-w-md">
                            <h1 className="mb-5 text-5xl font-bold">Welcome to MSEV!</h1>
                            <p className="mb-5">
                                Our platform provides a comprehensive solution for station owners and users alike, making it easy to find, reserve, and manage charging stations.
                            </p>
                            <button className="btn btn-primary">Get Started</button>
                        </div>
                    </div>
                </div>
            </NavLayout>
        </>
    )
}
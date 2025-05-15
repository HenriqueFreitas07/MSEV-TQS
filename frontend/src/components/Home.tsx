import NavLayout from "../layouts/NavLayout"
import hero_image from  "../assets/hero_charging_tesla.jpg"
export default function Home() {
    return (
        <>
            <NavLayout title="Home Page">
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
                            <h1 className="mb-5 text-5xl font-bold">Hello there</h1>
                            <p className="mb-5">
                                Provident cupiditate voluptatem et in. Quaerat fugiat ut assumenda excepturi exercitationem
                                quasi. In deleniti eaque aut repudiandae et a id nisi.
                            </p>
                            <button className="btn btn-primary">Get Started</button>
                        </div>
                    </div>
                </div>
            </NavLayout>
        </>
    )
}
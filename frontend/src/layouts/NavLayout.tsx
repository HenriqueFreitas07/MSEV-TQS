import React from 'react'
import Footer from '../components/Footer'

type Props = {
    children: React.ReactNode,
    title: string 
}

export default function Main({ children, title }: Props) {
    return (
        <>
            <div className="navbar bg-base-100 shadow-sm">
                <div className="flex-1">
                    <a href="/" className="btn btn-ghost text-xl">{title}</a>
                </div>
                <div className="flex-none">
                    <ul className="menu menu-horizontal px-1">
                        <li>
                            <a href="/register">Register</a>
                        </li>
                        <li>
                            <a href="/stations">Stations</a>
                        </li>
                    </ul>
                </div>
            </div>
            {
                children
            }
            <Footer />
        </>
    )
}
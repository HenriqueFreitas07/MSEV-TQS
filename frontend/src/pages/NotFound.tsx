import React from 'react'


export default function NotFound() {
  return (
    <>
      <div className="hero bg-base-200 min-h-screen">
        <div className="hero-content text-center">
          <div className="max-w-md">
            <h1 className="text-5xl font-bold">404 Not Found</h1>
            <p className="py-6">
              Perhaps you were looking for something else? <br />
              Or maybe you just typed the wrong URL? <br />

            </p>
            <a href="/" className="btn btn-primary">GO HOME</a>
          </div>
        </div>
      </div>
    </>
  )
}
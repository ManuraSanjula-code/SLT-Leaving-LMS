"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import RequestMovement from '../components/employee/RequestMovement';
import NavBar from "@/app/components/navbar/NavBar";

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <RequestMovement />
          </div>
        </>
      </Router>
  );
}

export default App;
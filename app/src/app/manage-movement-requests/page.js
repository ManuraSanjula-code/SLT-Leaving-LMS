"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import ManageMovementRequests from '../components/admin/ManageMovementRequests';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <ManageMovementRequests />
          </div>
        </>
      </Router>
  );
}

export default App;
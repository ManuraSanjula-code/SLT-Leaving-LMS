"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import SingleEmployeeActivities from '../components/employee/SingleEmployeeActivities';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <SingleEmployeeActivities />
          </div>
        </>
      </Router>
  );
}

export default App;
"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import EmployeeActivities from '../components/admin/EmployeeActivities';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <EmployeeActivities />
          </div>
        </>
      </Router>
  );
}

export default App;
"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "../components/navbar/NavBar";
import SingleEmployeeActivities from '../components/employee/SingleEmployeeActivities';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <SingleEmployeeActivities isAdmin={false} />
          </div>
        </>
      </Router>
  );
}

export default App;
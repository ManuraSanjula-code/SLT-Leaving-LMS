"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "../components/navbar/NavBar";
import NoPayLeaves from '../components/employee/NoPayLeaves';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <NoPayLeaves isAdmin={true} />
          </div>
        </>
      </Router>
  );
}

export default App;
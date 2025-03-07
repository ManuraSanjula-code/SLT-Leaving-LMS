"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import ManageEmployees from '../components/admin/ManageEmployees';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <ManageEmployees />
          </div>
        </>
      </Router>
  );
}

export default App;
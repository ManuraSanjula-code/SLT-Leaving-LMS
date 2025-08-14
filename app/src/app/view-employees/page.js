"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "../components/navbar/NavBar";
import ManageEmployees from '../components/admin/ManageEmployee/main/ManageEmployees';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <ManageEmployees  hr={true}/>
          </div>
        </>
      </Router>
  );
}

export default App;
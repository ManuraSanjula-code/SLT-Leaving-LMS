"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import UserProfile from '../components/dashboard/UserProfile';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <UserProfile />
          </div>
        </>
      </Router>
  );
}

export default App;
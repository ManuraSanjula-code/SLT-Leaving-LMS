"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import NoPayLeaves from '../components/dashboard/NoPayLeaves';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <NoPayLeaves />
          </div>
        </>
      </Router>
  );
}

export default App;
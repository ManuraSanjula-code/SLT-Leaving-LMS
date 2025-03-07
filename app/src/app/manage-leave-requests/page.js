"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import ManageLeaveRequests from '../components/admin/ManageLeaveRequests';

function App() {
  return (
      <Router>
        <>
          <NavBar />
          <div className="App">
            <ManageLeaveRequests />
          </div>
        </>
      </Router>
  );
}

export default App;
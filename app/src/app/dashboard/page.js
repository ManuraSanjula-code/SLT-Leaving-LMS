"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import Dashboard from '../components/dashboard/Dashboard';
import NavBar from "@/app/components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <Dashboard />
                </div>
            </>
        </Router>
    );
}

export default App;
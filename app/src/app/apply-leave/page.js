"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import ApplyLeave from '../components/dashboard/ApplyLeave';
import NavBar from "@/app/components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <ApplyLeave />
                </div>
            </>
        </Router>
    );
}

export default App;
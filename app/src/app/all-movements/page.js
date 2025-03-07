"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import AllMovements from '../components/employee/AllMovements';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <AllMovements />
                </div>
            </>
        </Router>
    );
}

export default App;
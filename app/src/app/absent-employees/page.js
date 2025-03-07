"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import AbsentEmployees from '../components/employee/AbsentEmployees';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <AbsentEmployees />
                </div>
            </>
        </Router>
    );
}

export default App;
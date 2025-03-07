"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import AllLeaves from '../components/employee/AllLeaves';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <AllLeaves />
                </div>
            </>
        </Router>
    );
}

export default App;
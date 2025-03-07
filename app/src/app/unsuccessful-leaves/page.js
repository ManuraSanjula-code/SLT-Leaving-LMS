"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "@/app/components/navbar/NavBar";
import UnsuccessfulLeaves from '../components/employee/UnsuccessfulLeaves';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <UnsuccessfulLeaves />
                </div>
            </>
        </Router>
    );
}

export default App;
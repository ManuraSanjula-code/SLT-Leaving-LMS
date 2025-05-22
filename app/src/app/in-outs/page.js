"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import AttendanceTracker from '../components/employee/InOut';
import NavBar from "../components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <AttendanceTracker userId={null}/>
                </div>
            </>
        </Router>
    );
}

export default App;
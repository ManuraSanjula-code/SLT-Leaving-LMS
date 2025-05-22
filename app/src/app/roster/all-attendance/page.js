"use client";

import React from 'react';
import {BrowserRouter as Router} from 'react-router-dom';
import NavBar from "../../components/navbar/NavBar";
import AttendanceTable from '../../components/roster/main/AllAttendance';

function App() {
    return (
        <Router>
            <>
                <NavBar/>
                <div className="App">
                    <AttendanceTable/>
                </div>
            </>
        </Router>
    );
}

export default App;
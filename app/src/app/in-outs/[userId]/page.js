"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import AttendanceTracker from '../../components/employee/InOut';
import NavBar from "../../components/navbar/NavBar";
import {useParams} from "next/navigation";

function App() {
    const params = useParams();
    const userId = params.userId;
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <AttendanceTracker userId={userId} />
                </div>
            </>
        </Router>
    );
}

export default App;
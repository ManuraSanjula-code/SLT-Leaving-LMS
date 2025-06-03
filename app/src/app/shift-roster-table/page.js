"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import ShiftRosterTable from '../components/roster/shif-roster/shif-roster-table';
import NavBar from "../components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <ShiftRosterTable />
                </div>
            </>
        </Router>
    );
}

export default App;
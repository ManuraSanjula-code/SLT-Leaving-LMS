"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import CharanaTVRosterTable from '../components/charana-tv/charana-tv';
import NavBar from "../components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <CharanaTVRosterTable />
                </div>
            </>
        </Router>
    );
}

export default App;
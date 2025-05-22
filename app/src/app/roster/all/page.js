"use client";

import React from 'react';
import {BrowserRouter as Router} from 'react-router-dom';
import NavBar from "../../components/navbar/NavBar";
import RosterDisplay from '../../components/roster/main/AllRoster';

function App() {
    return (
        <Router>
            <>
                <NavBar/>
                <div className="App">
                    <RosterDisplay/>
                </div>
            </>
        </Router>
    );
}

export default App;
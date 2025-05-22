"use client";

import React from 'react';
import {BrowserRouter as Router} from 'react-router-dom';
import NavBar from "../../components/navbar/NavBar";
import RosterDetails from '../../components/roster/main/Detail';

function App() {
    return (
        <Router>
            <>
                <NavBar/>
                <div className="App">
                    <RosterDetails/>
                </div>
            </>
        </Router>
    );
}

export default App;
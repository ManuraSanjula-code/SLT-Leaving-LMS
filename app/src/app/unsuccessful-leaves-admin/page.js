"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "../components/navbar/NavBar";
import UnsuccessfulLeaves from '../components/employee/UnsuccessfulLeaves';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <UnsuccessfulLeaves isAdmin={true}/>
                </div>
            </>
        </Router>
    );
}

export default App;
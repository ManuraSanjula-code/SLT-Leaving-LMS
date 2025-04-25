"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import NavBar from "../components/navbar/NavBar";
import UnauthorizedLeaves from '../components/employee/UnauthorizedLeaves';

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <UnauthorizedLeaves  isAdmin={false}/>
                </div>
            </>
        </Router>
    );
}

export default App;
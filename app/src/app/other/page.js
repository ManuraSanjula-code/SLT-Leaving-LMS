"use client";

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import OtherComponet from '../components/other/OtherComponet';
import NavBar from "../components/navbar/NavBar";

function App() {
    return (
        <Router>
            <>
                <NavBar />
                <div className="App">
                    <OtherComponet />
                </div>
            </>
        </Router>
    );
}

export default App;
"use client";

import { useParams } from 'next/navigation';
import AllLeaves from '../../components/employee/AllLeaves';
import NavBar from "../../components/navbar/NavBar";
import {BrowserRouter as Router} from "react-router-dom";
import React from "react";

export default function AllMovementsAdminPage() {
    const params = useParams();
    const userId = params.userId;

    return (
        <Router>
            <>
                <NavBar/>
                <div className="App">
                    <AllLeaves isAdmin={false} userAdmin={true} userId={userId} />
                </div>
            </>
        </Router>
    );
}
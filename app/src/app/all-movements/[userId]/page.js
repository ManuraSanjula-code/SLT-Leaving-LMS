"use client";

import { useParams } from 'next/navigation';
import ManageMovementRequests from '../../components/employee/AllMovements';
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
                    <ManageMovementRequests isAdmin={true} userId={userId} />
                </div>
            </>
        </Router>
    );
}
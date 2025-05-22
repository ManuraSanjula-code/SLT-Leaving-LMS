"use client";

import { useParams } from 'next/navigation';
import SingleEmployeeActivities from '../../components/employee/SingleEmployeeActivities';
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
                    <SingleEmployeeActivities isAdmin={true} userId={userId} />
                </div>
            </>
        </Router>
    );
}
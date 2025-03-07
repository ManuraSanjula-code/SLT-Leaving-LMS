/// If depending on employee joing date calculate the leaves
/// each employee have a supervicer and hod
/// in employee address add more thing such as division, electro, disrtic 
/// employee also have department
/// system can upload many data imports and it search all the files that similar to table in the database 
//  ----------- it import and save all the things what are similar
/// and also please mention system also accepting roaster file those who are part of the raoster 
/// if admin upload one csv the system read all the things after and notify what missing 
/// if admin forogot the upload a roaster file admin have manually type roster ids or automate by given all the people that part of roaster

/// CEO -- movemnet approves by chairman and add role call chairman
/// with in the system admin can add new role and new sections and profiles
/// in User tabel there is 4 ways indetify the employee -- AA____ which give by SLT company (parent company of the peo tv)
///                                                     -- VC____ peo tv verison of employee id 
///                                                     -- System assign public-id
///                                                     -- Data base also assign a id 


/// --------------------------- IMPORTAT -----------------------
/// TO TEST for we make role and profiles and sections and authorities in code side --- later it will directly inserted in database by sql
/// and create a tigger funtion in sql if data came to the sql server as soon after data arrived please encrypt the password 
/// --- while employee login make tigger fucntion to genarate exact same hash to user given password that matches to db encrypt password

/// Havent add departement 
/// after login redux not updating

"use client"

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NavBar from './components/navbar/NavBar';
import Dashboard from './components/dashboard/Dashboard';
import ApplyLeave from './components/dashboard/ApplyLeave';
import RequestMovement from './components/employee/RequestMovement';
import UserProfile from './components/dashboard/UserProfile';
import AllLeaves from './components/employee/AllLeaves';
import AllMovements from './components/employee/AllMovements';
import UnsuccessfulLeaves from './components/employee/UnsuccessfulLeaves';
import UnauthorizedLeaves from './components/employee/UnauthorizedLeaves';
import AbsentEmployees from './components/employee/AbsentEmployees';
import NoPayLeaves from './components/dashboard/NoPayLeaves';
import ManageEmployees from './components/admin/ManageEmployees';
import ManageLeaveRequests from './components/admin/ManageLeaveRequests';
import ManageMovementRequests from './components/admin/ManageMovementRequests';
import EmployeeActivities from './components/admin/EmployeeActivities';
import SingleEmployeeActivities from './components/employee/SingleEmployeeActivities';
import Unauthorized from "./unauthorized/page"
import ServerError from "./server-error/page"
import NetworkError from "./network-error/page"
import NotFoundPage from "./not-found-page/page"
import Error from "./error/page"

function App() {
  return (
    <Router>
      <NavBar />
      <Routes>

        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/apply-leave" element={<ApplyLeave />} />
        <Route path="/request-movement" element={<RequestMovement />} />
        <Route path="/profile" element={<UserProfile />} />
        <Route path="/all-leaves" element={<AllLeaves />} />
        <Route path="/all-movements" element={<AllMovements />} />
        <Route path="/single-employee-activities" element={<SingleEmployeeActivities />} />
        <Route path="/unsuccessful-leaves" element={<UnsuccessfulLeaves />} />
        <Route path="/unauthorized-leaves" element={<UnauthorizedLeaves />} />
        <Route path="/absent-employees" element={<AbsentEmployees />} />
        <Route path="/no-pay-leaves" element={<NoPayLeaves />} />
        <Route path="/manage-employees" element={<ManageEmployees />} />
        <Route path="/manage-leave-requests" element={<ManageLeaveRequests />} />
        <Route path="/manage-movement-requests" element={<ManageMovementRequests />} />
        <Route path="/employee-activities" element={<EmployeeActivities />} />
        <Route path="/" element={<Dashboard />} />
        
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/server-error" element={<ServerError />} />
        <Route path="/network-error" element={<NetworkError />} />
        <Route path="/error" element={<Error />} />
        <Route path="*" element={<NotFoundPage />} />

      </Routes>
    </Router>
  );
}

export default App;
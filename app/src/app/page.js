"use client"

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NavBar from './components/navbar/NavBar';
import Dashboard from './components/employee/Dashboard';
import ApplyLeave from './components/employee/ApplyLeave';
import RequestMovement from './components/employee/RequestMovement';
import UserProfile from './components/employee/UserProfile';
import AllLeaves from './components/employee/AllLeaves';
import AllMovements from './components/employee/AllMovements';
import UnsuccessfulLeaves from './components/employee/UnsuccessfulLeaves';
import UnauthorizedLeaves from './components/employee/UnauthorizedLeaves';
import AbsentEmployees from './components/employee/AbsentEmployees';
import NoPayLeaves from './components/employee/NoPayLeaves';
import ManageEmployees from './components/admin/ManageEmployee/main/ManageEmployees';
import ManageLeaveRequests from './components/admin/ManageLeaveRequests';
import ManageMovementRequests from './components/admin/ManageMovementRequests';
import EmployeeActivities from './components/admin/EmployeeActivities';
import SingleEmployeeActivities from './components/employee/SingleEmployeeActivities';
import Unauthorized from "./unauthorized/page"
import ServerError from "./server-error/page"
import NetworkError from "./network-error/page"
import NotFoundPage from "./not-found-page/page"
import Error from "./error/page"
import AttendanceTable from "./roster/all-attendance/page";
import AttendanceDashboard from "./roster/all/page";
import RosterDetail from "./roster/detail/page";

function App() {
  return (
    <Router>
      <NavBar />
      <Routes>

        <Route path="/dashboard" element={<Dashboard/>} />
        <Route path="/apply-leave" element={<ApplyLeave />} />
        <Route path="/request-movement" element={<RequestMovement />} />
        <Route path="/profile" element={<UserProfile />} />
        <Route path="/all-leaves" element={<AllLeaves />} />
        <Route path="/all-movements" element={<AllMovements />} />
        <Route path="/single-employee-activities" element={<SingleEmployeeActivities />} />
        <Route path="/unsuccessful-leaves" element={<UnsuccessfulLeaves/>} />
        <Route path="/unauthorized-leaves" element={<UnauthorizedLeaves/>} />
        <Route path="/unsuccessful-leaves-admin" element={<UnsuccessfulLeaves />} />
        <Route path="/unauthorized-leaves-admin" element={<UnauthorizedLeaves/>} />
        <Route path="/absent-employees" element={<AbsentEmployees />} />
        <Route path="/no-pay-leaves" element={<NoPayLeaves/>} />
        <Route path="/no-pay-leaves-admin" element={<NoPayLeaves/>} />
        <Route path="/manage-employees" element={<ManageEmployees />} />
        <Route path="/manage-leave-requests" element={<ManageLeaveRequests />} />
        <Route path="/manage-movement-requests" element={<ManageMovementRequests />} />
        <Route path="/employee-activities" element={<EmployeeActivities />} />
        <Route path="/" element={<Dashboard />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/server-error" element={<ServerError />} />
        <Route path="/network-error" element={<NetworkError />} />
        <Route path="/error" element={<Error />} />
        <Route path="/roster/all-attendance" element={<AttendanceTable />} />
        <Route path="/roster/all" element={<AttendanceDashboard />} />
        <Route path="/roster/detail" element={<RosterDetail />} />
        <Route path="*" element={<NotFoundPage />} />

      </Routes>
    </Router>
  );
}

export default App;
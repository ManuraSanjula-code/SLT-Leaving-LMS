"use client";

import React from 'react';
import {
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Box
} from '@mui/material';

const EmployeeTable = ({ employeeAttendanceDetails }) => {
    return (
        <>
            <Typography variant="h5" gutterBottom>
                Employee Attendance Details
            </Typography>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow style={{ backgroundColor: '#f5f5f5' }}>
                            <TableCell><strong>Employee ID</strong></TableCell>
                            <TableCell><strong>Name</strong></TableCell>
                            <TableCell><strong>Shift Time</strong></TableCell>
                            <TableCell><strong>Status</strong></TableCell>
                            <TableCell><strong>Arrival Time</strong></TableCell>
                            <TableCell><strong>Left Time</strong></TableCell>
                            <TableCell><strong>Late (mins)</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {employeeAttendanceDetails.map((employee, index) => (
                            <TableRow key={index} style={{
                                backgroundColor:
                                    employee.attendanceStatus === 'ABSENT' ? '#ffebee' :
                                        employee.attendanceStatus === 'LATE' ? '#fff8e1' :
                                            'inherit'
                            }}>
                                <TableCell>{employee.employeeId}</TableCell>
                                <TableCell>{employee.employeeName}</TableCell>
                                <TableCell>{employee.shiftTime}</TableCell>
                                <TableCell>
                                    <Box
                                        component="span"
                                        px={1}
                                        py={0.5}
                                        borderRadius={1}
                                        style={{
                                            backgroundColor:
                                                employee.attendanceStatus === 'PRESENT' ? '#e8f5e9' :
                                                    employee.attendanceStatus === 'ABSENT' ? '#ffebee' :
                                                        employee.attendanceStatus === 'LATE' ? '#fff8e1' :
                                                            employee.attendanceStatus === 'HALF_DAY' ? '#e3f2fd' :
                                                                'inherit',
                                            fontWeight: 'bold'
                                        }}
                                    >
                                        {employee.attendanceStatus}
                                    </Box>
                                </TableCell>
                                <TableCell>{employee.arrivalTime || '-'}</TableCell>
                                <TableCell>{employee.leftTime || '-'}</TableCell>
                                <TableCell>{employee.lateMinutes || '-'}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </>
    );
};

export default EmployeeTable;
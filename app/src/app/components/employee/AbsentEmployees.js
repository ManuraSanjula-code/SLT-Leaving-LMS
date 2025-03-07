"use client";

import React, { useState } from "react";
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Badge,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";

// Mock data for absent all-employees
const absentEmployees = [
  { id: 1, employeeName: 'John Doe', date: '2023-10-01', reason: 'No leave applied', isResolved: false },
  { id: 2, employeeName: 'Jane Smith', date: '2023-10-05', reason: 'No leave applied', isResolved: true },
];

const AbsentEmployees = () => {
  const [startDateFilter, setStartDateFilter] = useState(""); // State for start date filter
  const [endDateFilter, setEndDateFilter] = useState(""); // State for end date filter
  const [resolutionFilter, setResolutionFilter] = useState("All"); // State for resolution filter
  const [employees, setEmployees] = useState(absentEmployees); // State for all-employees

  // Filter absent all-employees based on date range and resolution status
  const filteredEmployees = employees.filter((employee) => {
    const matchesStartDateFilter =
        !startDateFilter || employee.date >= startDateFilter;

    const matchesEndDateFilter =
        !endDateFilter || employee.date <= endDateFilter;

    const matchesResolutionFilter =
        resolutionFilter === "All" ||
        (resolutionFilter === "Resolved" && employee.isResolved) ||
        (resolutionFilter === "Unresolved" && !employee.isResolved);

    return matchesStartDateFilter && matchesEndDateFilter && matchesResolutionFilter;
  });

  // Handle resolving an absence
  const handleResolveAbsence = (id) => {
    const updatedEmployees = employees.map((employee) =>
        employee.id === id ? { ...employee, isResolved: true } : employee
    );
    setEmployees(updatedEmployees); // Update state
    console.log(`Resolved absence for employee ID: ${id}`);
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Absent History
          </Typography>

          {/* Filters */}
          <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
            <TextField
                label="Start Date"
                type="date"
                variant="outlined"
                value={startDateFilter}
                onChange={(e) => setStartDateFilter(e.target.value)}
                InputLabelProps={{ shrink: true }}
            />
            <TextField
                label="End Date"
                type="date"
                variant="outlined"
                value={endDateFilter}
                onChange={(e) => setEndDateFilter(e.target.value)}
                InputLabelProps={{ shrink: true }}
            />
            <FormControl variant="outlined" sx={{ minWidth: 200 }}>
              <InputLabel>Filter by Resolution</InputLabel>
              <Select
                  value={resolutionFilter}
                  onChange={(e) => setResolutionFilter(e.target.value)}
                  label="Filter by Resolution"
              >
                <MenuItem value="All">All</MenuItem>
                <MenuItem value="Resolved">Resolved</MenuItem>
                <MenuItem value="Unresolved">Unresolved</MenuItem>
              </Select>
            </FormControl>
          </Box>

          {/* Table */}
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Employee Name</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Reason</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredEmployees.map((employee) => (
                    <TableRow key={employee.id}>
                      <TableCell>
                        {!employee.isResolved && (
                            <Badge color="error" variant="dot" sx={{ mr: 1 }} />
                        )}
                        {employee.employeeName}
                      </TableCell>
                      <TableCell>{employee.date}</TableCell>
                      <TableCell>{employee.reason}</TableCell>
                      <TableCell>
                        {!employee.isResolved && (
                            <Button
                                variant="contained"
                                color="primary"
                                size="small"
                                onClick={() => handleResolveAbsence(employee.id)}
                            >
                              Resolve
                            </Button>
                        )}
                      </TableCell>
                    </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </Container>
  );
};

export default AbsentEmployees;
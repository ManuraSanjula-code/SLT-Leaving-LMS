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
} from "@mui/material";

// Mock data for no-pay leaves
const noPayLeaves = [
  { id: 1, employeeName: "John Doe", startDate: "2023-10-01", endDate: "2023-10-03", reason: "No pay approved" },
  { id: 2, employeeName: "Jane Smith", startDate: "2023-10-05", endDate: "2023-10-06", reason: "No pay approved" },
];

const NoPayLeaves = () => {
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");

  // Filter leaves based on date range
  const filteredLeaves = noPayLeaves.filter((leave) => {
    const matchesStartDateFilter = !startDateFilter || leave.startDate >= startDateFilter;
    const matchesEndDateFilter = !endDateFilter || leave.endDate <= endDateFilter;
    return matchesStartDateFilter && matchesEndDateFilter;
  });

  return (
      <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            minHeight: "100vh", // Ensures full viewport height
          }}
      >
        <CssBaseline />
        <Container maxWidth="lg" sx={{ flex: 1 }}>
          <Box sx={{ mt: 4, mb: 4, flexGrow: 1 }}>
            <Typography variant="h4" gutterBottom>
              No-Pay Leaves
            </Typography>

            {/* Date Filters */}
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
            </Box>

            {/* Table */}
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Employee Name</TableCell>
                    <TableCell>Start Date</TableCell>
                    <TableCell>End Date</TableCell>
                    <TableCell>Reason</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredLeaves.map((leave) => (
                      <TableRow key={leave.id}>
                        <TableCell>{leave.employeeName}</TableCell>
                        <TableCell>{leave.startDate}</TableCell>
                        <TableCell>{leave.endDate}</TableCell>
                        <TableCell>{leave.reason}</TableCell>
                      </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </Container>

        {/* Footer */}
        {/*<Footer />*/}
      </Box>
  );
};

export default NoPayLeaves;
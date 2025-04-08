"use client";

import React, { useState, useEffect } from "react";
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
  CircularProgress,
  Pagination
} from "@mui/material";

const NoPayLeaves = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [leaveData, setLeaveData] = useState({ content: [], totalPages: 0 });
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");
  const [page, setPage] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    fetchLeaveData();
  }, [page, startDateFilter, endDateFilter]);

  const fetchLeaveData = async () => {
    setLoading(true);
    try {
      // Build URL with query parameters
      let url = `http://localhost:8080/lms/no-pay?page=${page}&size=${pageSize}`;

      if (startDateFilter) {
        url += `&startDate=${startDateFilter}`;
      }

      if (endDateFilter) {
        url += `&endDate=${endDateFilter}`;
      }

      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include', // This ensures cookies are sent with the request
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      setLeaveData(data);
    } catch (err) {
      console.error("Error fetching no-pay leaves:", err);
      setError("Failed to fetch no-pay leaves. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (event, value) => {
    setPage(value - 1); // API uses 0-based indexing
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  return (
      <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            minHeight: "100vh",
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

            {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                  <CircularProgress />
                </Box>
            ) : error ? (
                <Typography color="error" sx={{ my: 2 }}>
                  {error}
                </Typography>
            ) : (
                <>
                  {/* Table */}
                  <TableContainer component={Paper}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>Employee ID</TableCell>
                          <TableCell>Date</TableCell>
                          <TableCell>Submission Date</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Comment</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {leaveData.content && leaveData.content.length > 0 ? (
                            leaveData.content.map((leave) => (
                                <TableRow key={leave.id}>
                                  <TableCell>{leave.employeeID}</TableCell>
                                  <TableCell>{formatDate(leave.happenDate)}</TableCell>
                                  <TableCell>{formatDate(leave.submissionDate)}</TableCell>
                                  <TableCell>
                                    {leave.unSuccessful ? "Unsuccessful Attendance" : ""}
                                    {leave.absent ? "Absent" : ""}
                                    {leave.late ? "Late" : ""}
                                    {leave.halfDay ? "Half Day" : ""}
                                  </TableCell>
                                  <TableCell>{leave.comment}</TableCell>
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                              <TableCell colSpan={5} align="center">
                                No records found
                              </TableCell>
                            </TableRow>
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  {/* Pagination */}
                  {leaveData.totalPages > 1 && (
                      <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
                        <Pagination
                            count={leaveData.totalPages}
                            page={page + 1}
                            onChange={handlePageChange}
                            color="primary"
                        />
                      </Box>
                  )}
                </>
            )}
          </Box>
        </Container>
      </Box>
  );
};

export default NoPayLeaves;
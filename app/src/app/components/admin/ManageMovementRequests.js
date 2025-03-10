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
  IconButton,
  Button,
  Checkbox,
} from "@mui/material";
import { Delete as DeleteIcon } from "@mui/icons-material";

// Mock data for movement requests
const initialMovementRequests = [
  {
    id: 1,
    employeeId: 1,
    type: "Remote Work",
    startDate: "2023-10-01",
    endDate: "2023-10-03",
    status: "Pending",
  },
  {
    id: 2,
    employeeId: 2,
    type: "Office Relocation",
    startDate: "2023-10-05",
    endDate: "2023-10-06",
    status: "Approved",
  },
  {
    id: 3,
    employeeId: 3,
    type: "Remote Work",
    startDate: "2023-10-10",
    endDate: "2023-10-15",
    status: "Pending",
  },
];

const ManageMovementRequests = () => {
  const [movementRequests, setMovementRequests] = useState(initialMovementRequests);
  const [selected, setSelected] = useState([]); // State for selected movement requests

  // Handle individual row selection
  const handleSelect = (id) => {
    if (selected.includes(id)) {
      setSelected((prev) => prev.filter((item) => item !== id)); // Un-select
    } else {
      setSelected((prev) => [...prev, id]); // Select
    }
  };

  // Handle "Select All" functionality
  const handleSelectAll = () => {
    if (selected.length === movementRequests.length) {
      setSelected([]); // Un-select all
    } else {
      setSelected(movementRequests.map((request) => request.id)); // Select all
    }
  };

  // Handle bulk approve
  const handleBulkApprove = () => {
    setMovementRequests((prev) =>
        prev.map((request) =>
            selected.includes(request.id) ? { ...request, status: "Approved" } : request
        )
    );
    setSelected([]); // Clear selection after action
  };

  // Handle bulk reject
  const handleBulkReject = () => {
    setMovementRequests((prev) =>
        prev.map((request) =>
            selected.includes(request.id) ? { ...request, status: "Rejected" } : request
        )
    );
    setSelected([]); // Clear selection after action
  };

  // Handle bulk delete
  const handleBulkDelete = () => {
    setMovementRequests((prev) => prev.filter((request) => !selected.includes(request.id)));
    setSelected([]); // Clear selection after action
  };

  // Handle individual delete
  const handleDeleteMovementRequest = (id) => {
    setMovementRequests((prev) => prev.filter((request) => request.id !== id));
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {/* Bulk Actions */}
          <Box sx={{ mb: 2 }}>
            <Button
                variant="contained"
                color="primary"
                onClick={handleBulkApprove}
                disabled={selected.length === 0}
                sx={{ mr: 1 }}
            >
              Approve Selected
            </Button>
            <Button
                variant="contained"
                color="secondary"
                onClick={handleBulkReject}
                disabled={selected.length === 0}
                sx={{ mr: 1 }}
            >
              Reject Selected
            </Button>
            <Button
                variant="contained"
                color="error"
                onClick={handleBulkDelete}
                disabled={selected.length === 0}
            >
              Delete Selected
            </Button>
          </Box>

          {/* Table */}
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                        indeterminate={
                            selected.length > 0 && selected.length < movementRequests.length
                        }
                        checked={selected.length === movementRequests.length}
                        onChange={handleSelectAll}
                    />
                  </TableCell>
                  <TableCell>Employee</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Start Date</TableCell>
                  <TableCell>End Date</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {movementRequests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell padding="checkbox">
                        <Checkbox
                            checked={selected.includes(request.id)}
                            onChange={() => handleSelect(request.id)}
                        />
                      </TableCell>
                      <TableCell>Employee {request.employeeId}</TableCell>
                      <TableCell>{request.type}</TableCell>
                      <TableCell>{request.startDate}</TableCell>
                      <TableCell>{request.endDate}</TableCell>
                      <TableCell>{request.status}</TableCell>
                      <TableCell>
                        <Button
                            variant="contained"
                            color="primary"
                            sx={{ mr: 1 }}
                            onClick={() => {
                              setMovementRequests((prev) =>
                                  prev.map((req) =>
                                      req.id === request.id
                                          ? { ...req, status: "Approved" }
                                          : req
                                  )
                              );
                            }}
                        >
                          Approve
                        </Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={() => {
                              setMovementRequests((prev) =>
                                  prev.map((req) =>
                                      req.id === request.id
                                          ? { ...req, status: "Rejected" }
                                          : req
                                  )
                              );
                            }}
                        >
                          Reject
                        </Button>
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

export default ManageMovementRequests;
"use client";

import React, { useState, useEffect } from "react";
import {
  Radio,
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
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  MenuItem,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  Checkbox,
  FormControlLabel,
  Grid,
  CircularProgress,
  Alert,
} from "@mui/material";
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, VisibilityOff, Visibility } from '@mui/icons-material';
import dynamic from 'next/dynamic';

import EmployeeDialog from '../EmployeeDialog'


// Function to handle file upload
const handleFileImport_ = async (event, setIsLoading, setError, setSuccessMessage) => {
  const file = event.target.files[0];

  if (!file) {
    setError("No file selected.");
    return;
  }

  setIsLoading(true); // Start loading
  setError(null); // Reset error state
  setSuccessMessage(""); // Reset success message

  try {
    // Create a FormData object to send the file
    const formData = new FormData();
    formData.append("file", file);

    // Send the file to the server
    const response = await fetch("http://localhost:8080/users/api/upload/csv", {
      method: "POST",
      body: formData,
    });

    // Check if the response is successful
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "Failed to upload file.");
    }

    // Handle successful response
    const result = await response.json();
    setSuccessMessage(result.message || "File uploaded successfully.");
  } catch (err) {
    // Handle errors
    setError(err.message || "An unexpected error occurred.");
  } finally {
    setIsLoading(false); // Stop loading
  }
};

// Function to fetch users with pagination
const fetchUsers = async (setUsers, setIsLoading, setError, setCurrentPage, setTotalPages, page, limit) => {
  setIsLoading(true); // Start loading
  setError(null); // Reset error state

  try {
    const response = await fetch(`http://localhost:8080/users/all?page=${page}&limit=${limit}`);

    // Check if the response is successful
    if (!response.ok) {
      const errorData = await response.text(); // Try to get error details
      throw new Error(errorData || "Failed to fetch users.");
    }

    // Parse the JSON response
    const data = await response.json();

    // Update state with fetched data and metadata
    setUsers(data.content || []); // Extract the user data
    setTotalPages(data.totalPages || 0); // Extract total pages
    setCurrentPage(data.number || 0); // Update current page
  } catch (err) {
    // Handle errors
    setError(err.message || "An unexpected error occurred.");
  } finally {
    setIsLoading(false); // Stop loading
  }
};

// Filter options
const roles = [
  "ROLE_ADMIN",
  "ROLE_CEO",
  "ROLE_EMPLOYEE",
  "ROLE_HOD",
  "ROLE_SUPERVISOR",
];
const sections = [
  "HE/MCR",
  "IS/VAS",
  "FINANCE",
  "SALES",
  "ADMIN",
  "LEGAL",
  "MARKETING",
  "MEDIA",
];
const profiles = ["VC-VAS", "HEADQUARTERS-EMPLOYEE", "OUT-STATION-STAFF"];


// ManageEmployees Component
const ManageEmployees = () => {
  const [employees, setEmployees] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentEmployee, setCurrentEmployee] = useState(null);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-based indexing
  const [pageSize, setPageSize] = useState(10); // Default limit from backend
  const [totalPages, setTotalPages] = useState(0); // Total pages from backend

  // Filter states
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [selectedSections, setSelectedSections] = useState([]);
  const [selectedProfiles, setSelectedProfiles] = useState([]);

  // Search state
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false); // Loading state
  const [error, setError] = useState(null); // Error state
  const [successMessage, setSuccessMessage] = useState(""); // Success message state

  // Fetch users on component mount or when pagination changes
  useEffect(() => {
    fetchUsers(setEmployees, setIsLoading, setError, setCurrentPage, setTotalPages, currentPage, pageSize);
  }, [currentPage, pageSize]);

  // Handle opening and closing dialogs
  const handleOpenDialog = (employee = null) => {
    setCurrentEmployee(employee);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setCurrentEmployee(null);
  };

  const handleSaveEmployee = (employee) => {
    
    const userId = localStorage.getItem('userId');
    if (!userId) {
      console.error("User ID not found in localStorage");
      return Promise.reject("User ID not found");
    }

    console.log(employee)
    console.log(JSON.stringify(employee))

    return fetch(`http://localhost:8080/users/add/employees/${userId}`, {
      method: 'POST',
      credentials: 'include', // Ensures cookies are included
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(employee).trim(), // Convert JS object to JSON
    })
      .then(response => {
        if (!response.ok) {
          return response.json().then(errData => {
            throw new Error(`HTTP error! Status: ${response.status} - ${JSON.stringify(errData)}`);
          });
        }
        return response.json(); // Parse the JSON response
      })
      .then(data => {
        console.log("Employee added successfully:", data);
        return data; // Return data if needed
      })
      .catch(error => {
        console.error("Error saving employee:", error);
      });
  };
  // Handle deleting an employee
  const handleDeleteEmployee = (id) => {
    setEmployees((prev) => prev.filter((emp) => emp.userId !== id));
  };

  // Filter employees based on selected roles, sections, profiles, and search query
  const filteredEmployees = employees.filter((employee) => {
    const matchesRole = selectedRoles.length === 0 || selectedRoles.some((role) => employee.roles.some((r) => r.name === role));
    const matchesSection = selectedSections.length === 0 || selectedSections.some((section) => employee.sections.some((s) => s.section === section));
    const matchesProfile = selectedProfiles.length === 0 || selectedProfiles.some((profile) => employee.profiles.includes(profile));

    // Check if the search query matches the name, role, section, or profile
    const matchesSearch =
      searchQuery.trim() === "" ||
      employee.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      employee.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      employee.roles.some((role) => role.name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      employee.sections.some((section) => section.section.toLowerCase().includes(searchQuery.toLowerCase())) ||
      employee.profiles.some((profile) => profile.toLowerCase().includes(searchQuery.toLowerCase()));

    return matchesRole && matchesSection && matchesProfile && matchesSearch;
  });

  return (
    <Container>
      <CssBaseline />
      <Box sx={{ mt: 4 }}>
        <Typography variant="h4" gutterBottom>
          Manage Employees
        </Typography>

        {/* Loading Spinner */}
        {isLoading && (
          <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
            <CircularProgress />
            <Typography variant="body1" sx={{ ml: 2 }}>
              Please wait...
            </Typography>
          </Box>
        )}

        {/* Error Message */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {/* Success Message */}
        {successMessage && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {successMessage}
          </Alert>
        )}

        {/* Import and Export Buttons */}
        <Grid container spacing={2} justifyContent="flex-end" sx={{ mb: 2 }}>
          <Grid item>

            {/* <Button variant="contained"
            onClick={() => handleFileImport({ target: { files: [new File([], "dummy.csv")] } }, setIsLoading, setError, setSuccessMessage)}
            >
              Import Data
              <input
              type="file"
              hidden
              onChange={handleFileImport}
            />
            </Button> */}

            {/* <Button
              variant="contained"
              component="label"
              disabled={isLoading} // Disable button while loading
            >
              Choose File
              <input
                type="file"
                hidden
                onChange={(e) => handleFileImport(e, setIsLoading, setError, setSuccessMessage)}
              />
            </Button> */}

          </Grid>
          <Grid item>
            <Button variant="contained" onClick={() => handleOpenDialog()} sx={{ mr: 2 }}>
              Add Employee
            </Button>
          </Grid>
        </Grid>

        {/* Search Bar */}
        <TextField
          label="Search"
          variant="outlined"
          fullWidth
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          sx={{ mb: 2 }}
        />

        {/* Filter Section */}
        <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
          <FormControl fullWidth>
            <InputLabel>Roles</InputLabel>
            <Select
              multiple
              value={selectedRoles}
              onChange={(e) => setSelectedRoles(e.target.value)}
              renderValue={(selected) => selected.join(", ")}
              MenuProps={{
                anchorOrigin: {
                  vertical: "bottom",
                  horizontal: "left",
                },
                transformOrigin: {
                  vertical: "top",
                  horizontal: "left",
                },
                getContentAnchorEl: null,
              }}
            >
              {roles.map((role) => (
                <MenuItem key={role} value={role}>
                  {role}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel>Sections</InputLabel>
            <Select
              multiple
              value={selectedSections}
              onChange={(e) => setSelectedSections(e.target.value)}
              renderValue={(selected) => selected.join(", ")}
              MenuProps={{
                anchorOrigin: {
                  vertical: "bottom",
                  horizontal: "left",
                },
                transformOrigin: {
                  vertical: "top",
                  horizontal: "left",
                },
                getContentAnchorEl: null,
              }}
            >
              {sections.map((section) => (
                <MenuItem key={section} value={section}>
                  {section}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel>Profiles</InputLabel>
            <Select
              multiple
              value={selectedProfiles}
              onChange={(e) => setSelectedProfiles(e.target.value)}
              renderValue={(selected) => selected.join(", ")}
              MenuProps={{
                anchorOrigin: {
                  vertical: "bottom",
                  horizontal: "left",
                },
                transformOrigin: {
                  vertical: "top",
                  horizontal: "left",
                },
                getContentAnchorEl: null,
              }}
            >
              {profiles.map((profile) => (
                <MenuItem key={profile} value={profile}>
                  {profile}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Employee Table */}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>First Name</TableCell>
                <TableCell>Last Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Section</TableCell>
                <TableCell>Profile</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredEmployees.map((employee) => (
                <TableRow key={employee.userId}>
                  <TableCell>{employee.firstName}</TableCell>
                  <TableCell>{employee.lastName}</TableCell>
                  <TableCell>{employee.email}</TableCell>
                  <TableCell>{employee.roles.map((role) => role.name).join(", ")}</TableCell>
                  <TableCell>{employee.sections.map((section) => section.section).join(", ")}</TableCell>
                  <TableCell>{employee.profiles.join(", ")}</TableCell>
                  <TableCell>
                    <IconButton onClick={() => handleOpenDialog(employee)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => handleDeleteEmployee(employee.userId)}>
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination Controls */}
        <Box sx={{ display: "flex", justifyContent: "space-between", mt: 2 }}>
          <Button
            variant="contained"
            onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
            disabled={currentPage === 0}
          >
            Previous Page
          </Button>
          <Typography variant="body1">
            Page {currentPage + 1} / {totalPages}
          </Typography>
          <Button
            variant="contained"
            onClick={() => setCurrentPage((prev) => prev + 1)}
            disabled={currentPage >= totalPages - 1}
          >
            Next Page
          </Button>
        </Box>
        <Dialog open={openDialog} onClose={handleCloseDialog}>
          <EmployeeDialog
            open={openDialog}
            onClose={handleCloseDialog}
            onSave={handleSaveEmployee}
            employee={currentEmployee}
            roles={roles}
            sections={sections}
            profiles={profiles}
          />
        </Dialog>
      </Box>
    </Container>
  );
};


export default ManageEmployees;
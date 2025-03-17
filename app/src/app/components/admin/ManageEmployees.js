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
  Button,
  TextField,
  Dialog,
  MenuItem,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  Grid,
  CircularProgress,
  Alert,
} from "@mui/material";
import { Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import dynamic from 'next/dynamic';
import SuccessDialog from '../../components/SuccessDialog';
import ErrorDialog from '../../components/ErrorDialog';
import DynamicDialog from '../DynamicDialog';

const EmployeeDialog = dynamic(() => import('../EmployeeDialog'), {
  ssr: false,
});

const fetchUsers = async (
  setUsers,
  setIsLoading,
  setError,
  setCurrentPage,
  setTotalPages,
  page,
  limit,
  isMounted
) => {
  if (isMounted) {
    setIsLoading(true);
    setError(null);
  }

  try {
    const response = await fetch(`http://localhost:8080/users/all?page=${page}&limit=${limit}`);

    // Check if the response is successful
    if (!response.ok) {
      const errorData = await response.text(); // Try to get error details
      throw new Error(errorData || "Failed to fetch users.");
    }

    // Parse the JSON response
    const data = await response.json();

    // Update state only if the component is still mounted
    if (isMounted) {
      setUsers(data.content || []); // Extract the user data
      setTotalPages(data.totalPages || 0); // Extract total pages
      setCurrentPage(data.number || 0); // Update current page
    }
  } catch (err) {
    // Handle errors only if the component is still mounted
    if (isMounted) {
      setError(err.message || "An unexpected error occurred.");
    }
  } finally {
    // Stop loading only if the component is still mounted
    if (isMounted) {
      setIsLoading(false);
    }
  }
};

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

const ManageEmployees = () => {
  const [employees, setEmployees] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentEmployee, setCurrentEmployee] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [selectedSections, setSelectedSections] = useState([]);
  const [selectedProfiles, setSelectedProfiles] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");

  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);

  const handleSuccessOpen = () => {
    setSuccessOpen(true);
  };

  const handleSuccessClose = () => {
    setSuccessOpen(false);
  };

  const handleErrorOpen = () => {
    setErrorOpen(true);
  };

  const handleErrorClose = () => {
    setErrorOpen(false);
  };

  useEffect(() => {
    let isMounted = true;
    fetchUsers(setEmployees, setIsLoading, setError, setCurrentPage, setTotalPages, currentPage, pageSize, isMounted);
    return () => {
      isMounted = false;
    };
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

  const saveEmp = (userId) => {
    return fetch(`http://localhost:8080/users/add/employees/${userId}`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(employee).trim(),
    })
      .then(response => {
        if (!response.ok) {
          return response.json().then(errData => {
            throw new Error(`HTTP error! Status: ${response.status} - ${JSON.stringify(errData)}`);
          });
        }
        return response.json();
      })
      .then(data => {
        console.log("Employee added successfully:", data);
        handleSuccessOpen()
        return data;
      })
      .catch(error => {
        handleErrorOpen()
        console.error("Error saving employee:", error);
      });
  }

  const updateEmp = (userId, employee) => {
    return fetch(`http://localhost:8080/users/${employee.userId}/${userId}`, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(employee),
    }).then(response => {
      if (!response.ok) {
        return response.json().then(errData => {
          throw new Error(`HTTP error! Status: ${response.status} - ${JSON.stringify(errData)}`);
        });
      }
      return response.json();
    })
      .then(data => {
        console.log("Employee updated successfully:", data);
        handleSuccessOpen()
        return data;
      })
      .catch(error => {
        handleErrorOpen()
        console.error("Error updating employee:", error);
      });
  };

  const handleSaveEmployee = (employee) => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      console.error("User ID not found in localStorage");
      return Promise.reject("User ID not found");
    }
    if (employee.userId) {
      return updateEmp(userId, employee);
    } else {
      return saveEmp(userId)
    }
  };
  const handleDeleteEmployee = (id) => {
    setEmployees((prev) => prev.filter((emp) => emp.userId !== id));
  };

  const filteredEmployees = employees.filter((employee) => {
    const matchesRole = selectedRoles.length === 0 || selectedRoles.some((role) => employee.roles.some((r) => r.name === role));
    const matchesSection = selectedSections.length === 0 || selectedSections.some((section) => employee.sections.some((s) => s.section === section));
    const matchesProfile = selectedProfiles.length === 0 || selectedProfiles.some((profile) => employee.profiles.includes(profile));

    const matchesSearch =
      searchQuery.trim() === "" ||
      employee.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      employee.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      employee.roles.some((role) => role.name.toLowerCase().includes(searchQuery.toLowerCase())) ||
      employee.sections.some((section) => section.section.toLowerCase().includes(searchQuery.toLowerCase())) ||
      employee.profiles.some((profile) => profile.toLowerCase().includes(searchQuery.toLowerCase()));

    return matchesRole && matchesSection && matchesProfile && matchesSearch;
  });

  const handleRefresh = () => {
    let isMounted = true;

    fetchUsers(
      setEmployees,
      setIsLoading,
      setError,
      setCurrentPage,
      setTotalPages,
      currentPage,
      pageSize,
      isMounted
    );

    // Optional: Cleanup function for manual refresh
    return () => {
      isMounted = false;
    };
  };

  const [dialogOpen, setDialogOpen] = useState(false);

  const handleDialogOpen = () => {
    setDialogOpen(true);
  };

  const handleDialogClose = () => {
    setDialogOpen(false);
  };

  const items = [
    {
      label: 'Add Employee',
      onClick: handleOpenDialog,
    },
    {
      label: 'Add Profiles',
      onClick: handleOpenDialog
    },
    {
      label: 'Add Section',
      onClick: handleOpenDialog,
    },
    {
      label: 'Add Profiles',
      onClick: handleOpenDialog,
    },
    {
      label: 'Add Roles',
      onClick: handleOpenDialog,
    },
  ];


  return (
    <>
      <SuccessDialog
        open={successOpen}
        onClose={handleSuccessClose}
        title="Success!"
        message="Your action was completed successfully."
      />

      {/* Error Dialog */}
      <ErrorDialog
        open={errorOpen}
        onClose={handleErrorClose}
        title="Oops! Something Went Wrong"
        message="There was an error processing your request. Please try again."
      />

      <DynamicDialog
        open={dialogOpen}
        onClose={handleDialogClose}
        title="Select an Item"
        items={items}
      />
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

          <Grid container spacing={2} justifyContent="flex-end" sx={{ mb: 2 }}>

            <Grid item>
              <Button variant="contained" onClick={() => handleRefresh()} sx={{ mr: 2 }}>
                <Typography component="span" sx={{ fontSize: "30px", lineHeight: 1 }}>
                  ⟲
                </Typography>
              </Button>
            </Grid>

            <Grid item>
              <Button variant="contained" onClick={() => handleDialogOpen()} sx={{ mr: 2 }}>
                <Typography component="span" sx={{ fontSize: "30px", lineHeight: 1 }}>
                  +
                </Typography>
              </Button>
            </Grid>

            {/* <Grid item>
              <Button variant="contained" onClick={() => handleOpenDialog()} sx={{ mr: 1 }}>
                Add Employee
              </Button>
            </Grid>

            <Grid item>
              <Button variant="contained" onClick={() => handleOpenDialog()} sx={{ mr: 1 }}>
                Add Section
              </Button>
            </Grid>

            <Grid item>
              <Button variant="contained" onClick={() => handleOpenDialog()} sx={{ mr: 1 }}>
                Add Profile
              </Button>
            </Grid>

            <Grid item>
              <Button variant="contained" onClick={() => handleOpenDialog()} sx={{ mr: 1 }}>
                Add Role
              </Button>
            </Grid> */}

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
    </>
  );
};

export default ManageEmployees;
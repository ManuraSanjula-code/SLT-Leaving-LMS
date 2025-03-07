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
// import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from "@mui/icons-material";
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, VisibilityOff, Visibility } from '@mui/icons-material';

// Utility function to generate a temporary password
const generateTemporaryPassword = () => {
  const length = 10; // Length of the temporary password
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()"; // Characters to include
  let password = "";
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * charset.length);
    password += charset[randomIndex];
  }
  return password;
};

// Function to handle file upload
const handleFileImport = async (event, setIsLoading, setError, setSuccessMessage) => {
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

  // Handle saving an employee
  // const handleSaveEmployee = (employee) => {
  //   if (employee.userId) {
  //     // Edit existing employee
  //     setEmployees((prev) =>
  //       prev.map((emp) => (emp.userId === employee.userId ? employee : emp))
  //     );
  //   } else {
  //     // Add new employee
  //     setEmployees((prev) => [...prev, { ...employee, userId: prev.length + 1 }]);
  //   }
  //   handleCloseDialog();
  // };


  const handleSaveEmployee = async (employee) => {
    try {
      let response;

      if (employee.userId) {
        // Update existing employee (PUT request)
        response = await fetch(`/api/employees/${employee.userId}`, {
          method: 'PUT', // Use PUT or PATCH depending on your API design
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(employee),
        });

        if (!response.ok) {
          throw new Error('Failed to update employee');
        }

        // Update the local state after a successful API call
        setEmployees((prev) =>
          prev.map((emp) => (emp.userId === employee.userId ? employee : emp))
        );
      } else {
        // Create new employee (POST request)
        response = await fetch('/api/employees', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(employee),
        });

        if (!response.ok) {
          throw new Error('Failed to create employee');
        }

        // Get the newly created employee data from the server (if needed)
        const newEmployee = await response.json();

        // Add the new employee to the local state
        setEmployees((prev) => [...prev, { ...newEmployee }]);
      }

      // Close the dialog after a successful operation
      handleCloseDialog();
    } catch (error) {
      console.error('Error saving employee:', error);
      alert('An error occurred while saving the employee. Please try again.');
    }
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

            <Button
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
            </Button>

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

        {/* Employee Dialog (Add/Edit) */}
        {/* <EmployeeDialog
          open={openDialog}
          onClose={handleCloseDialog}
          onSave={handleSaveEmployee}
          employee={currentEmployee}
          roles={roles}
          sections={sections}
          profiles={profiles}
        /> */}

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

const EmployeeDialog_ = ({ open, onClose, onSave, employee, roles, sections, profiles }) => {
  const [firstName, setFirstName] = useState(employee?.firstName || '');
  const [lastName, setLastName] = useState(employee?.lastName || '');
  const [email, setEmail] = useState(employee?.email || '');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState(employee?.phone || '');
  const [gender, setGender] = useState(employee?.gender || '');
  const [isSltEmp, setIsSltEmp] = useState(employee?.isSltEmp || 0);
  const [isSltIntern, setIsSltIntern] = useState(employee?.isSltIntern || 0);
  const [active, setActive] = useState(employee?.active || 1);

  // const [selectedRoles, setSelectedRoles] = useState(employee?.roles || []);
  // const [selectedSections, setSelectedSections] = useState(employee?.sections || []);
  // const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles || []);

  const [addresses, setAddresses] = useState(employee?.addresses || []);
  const [defaultAddress, setDefaultAddress] = useState(employee?.defaultAddress || 0);

  const [selectedRoles, setSelectedRoles] = useState(employee?.roles.map(role => role.name) || []);
  const [selectedSections, setSelectedSections] = useState(employee?.sections.map(section => section.section) || []);
  const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles.map(profile => profile.name) || []);

  React.useEffect(() => {
    if (employee) {
      setSelectedRoles(employee.roles || []);
      setSelectedSections(employee.sections || []);
      setSelectedProfiles(employee.profiles || []);
    } else {
      setSelectedRoles([]);
      setSelectedSections([]);
      setSelectedProfiles([]);
    }
  }, [employee]);

  const handleAddAddress = () => {
    const newAddress = {
      id: addresses.length + 1,
      street: '',
      city: '',
      state: '',
      zip: '',
      isDefault: false,
    };
    setAddresses([...addresses, newAddress]);
  };

  const handleRemoveAddress = (id) => {
    setAddresses(addresses.filter((addr) => addr.id !== id));
  };

  const handleAddressChange = (id, field, value) => {
    setAddresses(
      addresses.map((addr) =>
        addr.id === id ? { ...addr, [field]: value } : addr
      )
    );
  };

  const handleSetDefaultAddress = (id) => {
    setAddresses(
      addresses.map((addr) => ({
        ...addr,
        isDefault: addr.id === id,
      }))
    );
    setDefaultAddress(id);
  };

  const handleGenerateTemporaryPassword = () => {
    const tempPassword = generateTemporaryPassword();
    setPassword(tempPassword);
  };

  const handleSave = () => {
    const newEmployee = {
      id: employee?.id,
      firstName,
      lastName,
      email,
      password,
      phone,
      gender,
      isSltEmp,
      isSltIntern,
      active,
      roles: selectedRoles,
      sections: selectedSections,
      profiles: selectedProfiles,
      addresses,
      defaultAddress,
    };
    onSave(newEmployee);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{employee ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
      <DialogContent>
        <TextField
          margin="normal"
          required
          fullWidth
          label="First Name"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          label="Last Name"
          value={lastName}
          onChange={(e) => setLastName(e.target.value.trim())}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          label="Email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={9}>
            <TextField
              margin="normal"
              required
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Grid>
          <Grid item xs={3}>
            <Button
              variant="outlined"
              onClick={handleGenerateTemporaryPassword}
              sx={{ mt: 2 }}
            >
              Generate Temp Password
            </Button>
          </Grid>
        </Grid>
        <TextField
          margin="normal"
          required
          fullWidth
          label="Phone Number"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
        />
        <FormControl fullWidth margin="normal">
          <InputLabel>Gender</InputLabel>
          <Select
            value={gender}
            onChange={(e) => setGender(e.target.value)}
          >
            <MenuItem value="M">Male</MenuItem>
            <MenuItem value="F">Female</MenuItem>
          </Select>
        </FormControl>

        <FormControlLabel
          control={<Checkbox checked={isSltEmp === 1}
            onChange={(e) => setIsSltEmp(e.target.checked ? 1 : 0)} />}
          label="Is SLT Employee"
        />

        <FormControlLabel
          control={<Checkbox checked={isSltIntern === 1}
            onChange={(e) => setIsSltIntern(e.target.checked ? 1 : 0)} />}
          label="Is SLT Intern"
        />

        <FormControlLabel
          control={<Checkbox checked={active === 1} onChange={(e) => setActive(e.target.checked ? 1 : 0)} />}
          label="Active"
        />

        {/* <FormControl fullWidth margin="normal">
          <InputLabel>Roles</InputLabel>
          <Select
            multiple
            value={selectedRoles}
            onChange={(e) => setSelectedRoles(e.target.value)}
          >
            {roles.map((role) => (
              <MenuItem key={role} value={role}>
                {role}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Sections</InputLabel>
          <Select
            multiple
            value={selectedSections}
            onChange={(e) => setSelectedSections(e.target.value)}
          >
            {sections.map((section) => (
              <MenuItem key={section} value={section}>
                {section}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Profiles</InputLabel>
          <Select
            multiple
            value={selectedProfiles}
            onChange={(e) => setSelectedProfiles(e.target.value)}
          >
            {profiles.map((profile) => (
              <MenuItem key={profile} value={profile}>
                {profile}
              </MenuItem>
            ))}
          </Select>
        </FormControl> */}

        <FormControl fullWidth margin="normal">
          <InputLabel>Roles</InputLabel>
          <Select
            multiple // Enable multi-selection
            value={selectedRoles} // Array of strings (e.g., ['ROLE_ADMIN', 'ROLE_EMPLOYEE'])
            onChange={(e) => setSelectedRoles(e.target.value)} // Update state with selected values
            renderValue={(selected) => selected.join(', ')} // Display selected roles as a comma-separated string
          >
            {roles.map((role) => (
              <MenuItem key={role} value={role}> {/* Ensure `value` is a string */}
                {role}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Sections</InputLabel>
          <Select
            multiple // Enable multi-selection
            value={selectedSections} // Array of strings (e.g., ['HE/MCR', 'FINANCE'])
            onChange={(e) => setSelectedSections(e.target.value)}
            renderValue={(selected) => selected.join(', ')} // Display selected sections as a comma-separated string
          >
            {sections.map((section) => (
              <MenuItem key={section} value={section}> {/* Ensure `value` is a string */}
                {section}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Profiles</InputLabel>
          <Select
            multiple // Enable multi-selection
            value={selectedProfiles} // Array of strings (e.g., ['VC-VAS', 'HEADQUARTERS-EMPLOYEE'])
            onChange={(e) => setSelectedProfiles(e.target.value)}
            renderValue={(selected) => selected.join(', ')} // Display selected profiles as a comma-separated string
          >
            {profiles.map((profile) => (
              <MenuItem key={profile} value={profile}> {/* Ensure `value` is a string */}
                {profile}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Address Section */}
        <Typography variant="h6" sx={{ mt: 3 }}>
          Addresses
        </Typography>
        {addresses.map((address) => (
          <Grid container spacing={2} key={address.id} sx={{ mt: 2 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Street"
                value={address.street}
                onChange={(e) =>
                  handleAddressChange(address.id, 'street', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="City"
                value={address.city}
                onChange={(e) =>
                  handleAddressChange(address.id, 'city', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="State"
                value={address.state}
                onChange={(e) =>
                  handleAddressChange(address.id, 'state', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Zip Code"
                value={address.zip}
                onChange={(e) =>
                  handleAddressChange(address.id, 'zip', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Radio
                    checked={address.isDefault}
                    onChange={() => handleSetDefaultAddress(address.id)}
                  />
                }
                label="Set as Default Address"
              />
              <Button
                variant="outlined"
                color="error"
                onClick={() => handleRemoveAddress(address.id)}
                sx={{ ml: 2 }}
              >
                Remove Address
              </Button>
            </Grid>
          </Grid>
        ))}
        <Button
          variant="outlined"
          onClick={handleAddAddress}
          sx={{ mt: 2 }}
        >
          Add Address
        </Button>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSave} variant="contained">
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const EmployeeDialog = ({ open, onClose, onSave, employee, roles, sections, profiles }) => {
  const [firstName, setFirstName] = useState(employee?.firstName || '');
  const [lastName, setLastName] = useState(employee?.lastName || '');
  const [email, setEmail] = useState(employee?.email || '');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState(employee?.phone || '');
  const [gender, setGender] = useState(employee?.gender || '');
  const [isSltEmp, setIsSltEmp] = useState(employee?.isSltEmp || 0);
  const [isSltIntern, setIsSltIntern] = useState(employee?.isSltIntern || 0);
  const [active, setActive] = useState(employee?.active || 1);

  const [selectedRoles, setSelectedRoles] = useState(employee?.roles.map(role => role.name) || []);
  const [selectedSections, setSelectedSections] = useState(employee?.sections.map(section => section.section) || []);
  const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles.map(profile => profile.name) || []);

  const [addresses, setAddresses] = useState(employee?.addresses || []);
  const [defaultAddress, setDefaultAddress] = useState(employee?.defaultAddress || 0);

  React.useEffect(() => {
    if (employee) {
      setSelectedRoles(employee.roles.map(role => role.name) || []);
      setSelectedSections(employee.sections.map(section => section.section) || []);
      setSelectedProfiles(employee.profiles.map(profile => profile.name) || []);
    } else {
      setSelectedRoles([]);
      setSelectedSections([]);
      setSelectedProfiles([]);
    }
  }, [employee]);

  const handleSave = () => {
    const newEmployee = {
      id: employee?.id,
      firstName,
      lastName,
      email,
      password,
      phone,
      gender,
      isSltEmp,
      isSltIntern,
      active,
      roles: selectedRoles.map(roleName => ({ id: roles.indexOf(roleName) + 1, name: roleName })),
      sections: selectedSections.map(sectionName => ({ id: sections.indexOf(sectionName) + 1, section: sectionName })),
      profiles: selectedProfiles.map(profileName => ({ id: profiles.indexOf(profileName) + 1, name: profileName })),
      addresses,
      defaultAddress,
    };
    onSave(newEmployee);
  };

  const handleAddAddress = () => {
    const newAddress = {
      id: addresses.length + 1,
      street: '',
      city: '',
      state: '',
      zip: '',
      isDefault: false,
    };
    setAddresses([...addresses, newAddress]);
  };

  const handleRemoveAddress = (id) => {
    setAddresses(addresses.filter((addr) => addr.id !== id));
  };

  const handleAddressChange = (id, field, value) => {
    setAddresses(
      addresses.map((addr) =>
        addr.id === id ? { ...addr, [field]: value } : addr
      )
    );
  };

  const handleSetDefaultAddress = (id) => {
    setAddresses(
      addresses.map((addr) => ({
        ...addr,
        isDefault: addr.id === id,
      }))
    );
    setDefaultAddress(id);
  };

  const handleGenerateTemporaryPassword = () => {
    const tempPassword = generateTemporaryPassword();
    setPassword(tempPassword);
  };


  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{employee ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
      <DialogContent>
        {/* ... other fields ... */}

        <TextField
          margin="normal"
          required
          fullWidth
          label="First Name"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          label="Last Name"
          value={lastName}
          onChange={(e) => setLastName(e.target.value.trim())}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          label="Email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={9}>
            <TextField
              margin="normal"
              required
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Grid>
          <Grid item xs={3}>
            <Button
              variant="outlined"
              onClick={handleGenerateTemporaryPassword}
              sx={{ mt: 2 }}
            >
              Generate Temp Password
            </Button>
          </Grid>
        </Grid>
        <TextField
          margin="normal"
          required
          fullWidth
          label="Phone Number"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
        />

        <FormControl fullWidth margin="normal">
          <InputLabel>Roles</InputLabel>
          <Select
            multiple
            value={selectedRoles}
            onChange={(e) => setSelectedRoles(e.target.value)}
            renderValue={(selected) => selected.join(', ')}
          >
            {roles.map((role) => (
              <MenuItem key={role} value={role}>
                {role}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Sections</InputLabel>
          <Select
            multiple
            value={selectedSections}
            onChange={(e) => setSelectedSections(e.target.value)}
            renderValue={(selected) => selected.join(', ')}
          >
            {sections.map((section) => (
              <MenuItem key={section} value={section}>
                {section}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Profiles</InputLabel>
          <Select
            multiple
            value={selectedProfiles}
            onChange={(e) => setSelectedProfiles(e.target.value)}
            renderValue={(selected) => selected.join(', ')}
          >
            {profiles.map((profile) => (
              <MenuItem key={profile} value={profile}>
                {profile}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Gender</InputLabel>
          <Select
            value={gender}
            onChange={(e) => setGender(e.target.value)}
          >
            <MenuItem value="M">Male</MenuItem>
            <MenuItem value="F">Female</MenuItem>
          </Select>
        </FormControl>

        <FormControlLabel
          control={<Checkbox checked={isSltEmp === 1}
            onChange={(e) => setIsSltEmp(e.target.checked ? 1 : 0)} />}
          label="Is SLT Employee"
        />

        <FormControlLabel
          control={<Checkbox checked={isSltIntern === 1}
            onChange={(e) => setIsSltIntern(e.target.checked ? 1 : 0)} />}
          label="Is SLT Intern"
        />

        <FormControlLabel
          control={<Checkbox checked={active === 1} onChange={(e) => setActive(e.target.checked ? 1 : 0)} />}
          label="Active"
        />

        <Typography variant="h6" sx={{ mt: 3 }}>
          Addresses
        </Typography>

        {addresses.map((address) => (
          <Grid container spacing={2} key={address.id} sx={{ mt: 2 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Street"
                value={address.street}
                onChange={(e) =>
                  handleAddressChange(address.id, 'street', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="City"
                value={address.city}
                onChange={(e) =>
                  handleAddressChange(address.id, 'city', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="State"
                value={address.state}
                onChange={(e) =>
                  handleAddressChange(address.id, 'state', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Zip Code"
                value={address.zip}
                onChange={(e) =>
                  handleAddressChange(address.id, 'zip', e.target.value)
                }
              />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Radio
                    checked={address.isDefault}
                    onChange={() => handleSetDefaultAddress(address.id)}
                  />
                }
                label="Set as Default Address"
              />
              <Button
                variant="outlined"
                color="error"
                onClick={() => handleRemoveAddress(address.id)}
                sx={{ ml: 2 }}
              >
                Remove Address
              </Button>
            </Grid>
          </Grid>
        ))}
        <Button
          variant="outlined"
          onClick={handleAddAddress}
          sx={{ mt: 2 }}
        >
          Add Address
        </Button>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSave} variant="contained">
          Save
        </Button>
      </DialogActions>

    </Dialog>
  );
};

export default ManageEmployees;
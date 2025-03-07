"use client";

import React, {useState} from 'react';
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
    Radio,
    RadioGroup,
    InputAdornment,
} from '@mui/material';
import {Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, VisibilityOff, Visibility} from '@mui/icons-material';
import {CircularProgress, Alert} from '@mui/material';

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
        setError('No file selected.');
        return;
    }

    setIsLoading(true); // Start loading
    setError(null); // Reset error state
    setSuccessMessage(''); // Reset success message

    try {
        // Create a FormData object to send the file
        const formData = new FormData();
        formData.append('file', file);

        // Send the file to the server
        const response = await fetch('http://localhost:8080/users/api/upload/csv', {
            method: 'POST',
            body: formData,
        });

        // Check if the response is successful
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to upload file.');
        }

        // Handle successful response
        const result = await response.json();
        setSuccessMessage(result.message || 'File uploaded successfully.');
    } catch (err) {
        // Handle errors
        setError(err.message || 'An unexpected error occurred.');
    } finally {
        setIsLoading(false); // Stop loading
    }
};


// Mock data for all-employees
const initialEmployees = [
    {
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        role: 'ROLE_EMPLOYEE',
        gender: 'M',
        phone: '1234567890',
        isSltEmp: 0,
        isSltIntern: 1,
        active: 1,
        addresses: [
            {id: 1, street: '123 Main St', city: 'New York', state: 'NY', zip: '10001', isDefault: true},
        ],
        roles: ['ROLE_EMPLOYEE'],
        sections: ['HE/MCR'],
        profiles: ['VC-VAS'],
    },
    {
        id: 2,
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane.smith@example.com',
        role: 'ROLE_ADMIN',
        gender: 'F',
        phone: '0987654321',
        isSltEmp: 1,
        isSltIntern: 0,
        active: 1,
        addresses: [
            {id: 2, street: '456 Elm St', city: 'Los Angeles', state: 'CA', zip: '90001', isDefault: false},
        ],
        roles: ['ROLE_ADMIN'],
        sections: ['ADMIN'],
        profiles: ['HEADQUARTERS-EMPLOYEE'],
    },
];

const fetchUsers = async (setUsers, setIsLoading, setError) => {
  setIsLoading(true); // Start loading
  setError(null); // Reset error state

  try {
    const response = await fetch('http://localhost:8080/users');

    // Check if the response is successful
    if (!response.ok) {
      const errorData = await response.text(); // Try to get error details
      throw new Error(errorData || 'Failed to fetch users.');
    }

    // Parse the JSON response
    const data = await response.json();
    setUsers(data); // Set the fetched users
  } catch (err) {
    // Handle errors
    setError(err.message || 'An unexpected error occurred.');
  } finally {
    setIsLoading(false); // Stop loading
  }
};


// Filter options
const roles = ['ROLE_ADMIN', 'ROLE_CEO', 'ROLE_EMPLOYEE', 'ROLE_HOD', 'ROLE_SUPERVISOR'];
const sections = ['HE/MCR', 'IS/VAS', 'FINANCE', 'SALES', 'ADMIN', 'LEGAL', 'MARKETING', 'MEDIA'];
const profiles = ['VC-VAS', 'HEADQUARTERS-EMPLOYEE', 'OUT-STATION-STAFF'];

const ManageEmployees = () => {
    const [employees, setEmployees] = useState(initialEmployees);
    const [openDialog, setOpenDialog] = useState(false);
    const [currentEmployee, setCurrentEmployee] = useState(null);
    const [dialogOpen, setDialogOpen] = useState(false);

    // Filter states
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [selectedSections, setSelectedSections] = useState([]);
    const [selectedProfiles, setSelectedProfiles] = useState([]);

    // Search state
    const [searchQuery, setSearchQuery] = useState('');
    const [isLoading, setIsLoading] = useState(false); // Loading state
    const [error, setError] = useState(null); // Error state
    const [successMessage, setSuccessMessage] = useState(''); // Success message state

    const handleOpenDialog = (employee = null) => {
        setCurrentEmployee(employee);
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setCurrentEmployee(null);
    };

    const handleSaveEmployee = (employee) => {
        if (employee.id) {
            // Edit existing employee
            setEmployees((prev) =>
                prev.map((emp) => (emp.id === employee.id ? employee : emp))
            );
        } else {
            // Add new employee
            setEmployees((prev) => [...prev, {...employee, id: prev.length + 1}]);
        }
        handleCloseDialog();
    };

    const handleDeleteEmployee = (id) => {
        setEmployees((prev) => prev.filter((emp) => emp.id !== id));
    };

    // Filter employees based on selected roles, sections, profiles, and search query
    const filteredEmployees = employees.filter((employee) => {
        const matchesRole = selectedRoles.length === 0 || selectedRoles.some((role) => employee.roles.includes(role));
        const matchesSection = selectedSections.length === 0 || selectedSections.some((section) => employee.sections.includes(section));
        const matchesProfile = selectedProfiles.length === 0 || selectedProfiles.some((profile) => employee.profiles.includes(profile));

        // Check if the search query matches the name, role, section, or profile
        const matchesSearch =
            searchQuery.trim() === '' ||
            employee.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            employee.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            employee.roles.some((role) => role.toLowerCase().includes(searchQuery.toLowerCase())) ||
            employee.sections.some((section) => section.toLowerCase().includes(searchQuery.toLowerCase())) ||
            employee.profiles.some((profile) => profile.toLowerCase().includes(searchQuery.toLowerCase()));

        return matchesRole && matchesSection && matchesProfile && matchesSearch;
    });

    return (
        <Container>
            <CssBaseline/>
            <Box sx={{mt: 4}}>
                <Typography variant="h4" gutterBottom>
                    Manage Employees
                </Typography>

                {/* Search Bar */}
                <TextField
                    label="Search by Name, Role, Section, or Profile"
                    variant="outlined"
                    fullWidth
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{mb: 2}}
                />

                {/* Filter Section */}
                <Box sx={{mb: 2}}>
                    <Typography variant="h6">Filters</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6} md={3}>
                            <FormControl fullWidth>
                                <InputLabel>Roles</InputLabel>
                                <Select
                                    multiple
                                    value={selectedRoles}
                                    onChange={(e) => setSelectedRoles(e.target.value)}
                                    renderValue={(selected) => selected.join(', ')}
                                    MenuProps={{
                                        anchorOrigin: {
                                            vertical: 'bottom',
                                            horizontal: 'left',
                                        },
                                        transformOrigin: {
                                            vertical: 'top',
                                            horizontal: 'left',
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
                        </Grid>
                        <Grid item xs={12} sm={6} md={3}>
                            <FormControl fullWidth>
                                <InputLabel>Sections</InputLabel>
                                <Select
                                    multiple
                                    value={selectedSections}
                                    onChange={(e) => setSelectedSections(e.target.value)}
                                    renderValue={(selected) => selected.join(', ')}
                                    MenuProps={{
                                        anchorOrigin: {
                                            vertical: 'bottom',
                                            horizontal: 'left',
                                        },
                                        transformOrigin: {
                                            vertical: 'top',
                                            horizontal: 'left',
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
                        </Grid>
                        <Grid item xs={12} sm={6} md={3}>
                            <FormControl fullWidth>
                                <InputLabel>Profiles</InputLabel>
                                <Select
                                    multiple
                                    value={selectedProfiles}
                                    onChange={(e) => setSelectedProfiles(e.target.value)}
                                    renderValue={(selected) => selected.join(', ')}
                                    MenuProps={{
                                        anchorOrigin: {
                                            vertical: 'bottom',
                                            horizontal: 'left',
                                        },
                                        transformOrigin: {
                                            vertical: 'top',
                                            horizontal: 'left',
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
                        </Grid>
                    </Grid>
                </Box>

                <Box sx={{mb: 2}}>

                    {/* <Button variant="contained" component="label" sx={{mr: 2}} disabled={isLoading}
                            onClick={() => setDialogOpen(true)}>
                        Import Data
                        <input type="file" hidden
                               onChange={(e) => handleFileImport(e, setIsLoading, setError, setSuccessMessage)}/>
                    </Button>
                    <Button variant="contained" onClick={() => handleFileExport("csv")} sx={{mr: 2}}>
                        Export as CSV
                    </Button>
                    <Button variant="contained" onClick={() => handleFileExport("xlsx")} sx={{mr: 2}}>
                        Export as Excel
                    </Button>
                    <Button variant="contained" onClick={() => handleFileExport("json")}>
                        Export as JSON
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

                    {isLoading && (
                        <Box sx={{mt: 2}}>
                            <CircularProgress size={24}/>
                            <Typography variant="body2" sx={{ml: 1, display: 'inline'}}>
                                Please wait...
                            </Typography>
                        </Box>
                    )}

                    {/* Error Message */}
                    {error && (
                        <Alert severity="error" sx={{mt: 2}}>
                            {error}
                        </Alert>
                    )}

                    {/* Success Message */}
                    {successMessage && (
                        <Alert severity="success" sx={{mt: 2}}>
                            {successMessage}
                        </Alert>
                    )}
                </Box>

                {/* Add Employee Button */}
                <Button
                    variant="contained"
                    startIcon={<AddIcon/>}
                    onClick={() => handleOpenDialog()}
                    sx={{mb: 2}}
                >
                    Add Employee
                </Button>

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
                                <TableRow key={employee.id}>
                                    <TableCell>{employee.firstName}</TableCell>
                                    <TableCell>{employee.lastName}</TableCell>
                                    <TableCell>{employee.email}</TableCell>
                                    <TableCell>{employee.role}</TableCell>
                                    <TableCell>{employee.sections.join(', ')}</TableCell>
                                    <TableCell>{employee.profiles.join(', ')}</TableCell>
                                    <TableCell>
                                        <IconButton onClick={() => handleOpenDialog(employee)}>
                                            <EditIcon/>
                                        </IconButton>
                                        <IconButton onClick={() => handleDeleteEmployee(employee.id)}>
                                            <DeleteIcon/>
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {/* Employee Dialog (Add/Edit) */}
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

const EmployeeDialog = ({open, onClose, onSave, employee, roles, sections, profiles}) => {
    const [firstName, setFirstName] = useState(employee?.firstName || '');
    const [lastName, setLastName] = useState(employee?.lastName || '');
    const [email, setEmail] = useState(employee?.email || '');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState(employee?.phone || '');
    const [gender, setGender] = useState(employee?.gender || '');
    const [isSltEmp, setIsSltEmp] = useState(employee?.isSltEmp || 0);
    const [isSltIntern, setIsSltIntern] = useState(employee?.isSltIntern || 0);
    const [active, setActive] = useState(employee?.active || 1);
    const [selectedRoles, setSelectedRoles] = useState(employee?.roles || []);
    const [selectedSections, setSelectedSections] = useState(employee?.sections || []);
    const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles || []);
    const [addresses, setAddresses] = useState(employee?.addresses || []);
    const [defaultAddress, setDefaultAddress] = useState(employee?.defaultAddress || 0);

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
                addr.id === id ? {...addr, [field]: value} : addr
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
                    onChange={(e) => setLastName(e.target.value)}
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
                            sx={{mt: 2}}
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
                                       onChange={(e) => setIsSltEmp(e.target.checked ? 1 : 0)}/>}
                    label="Is SLT Employee"
                />
                <FormControlLabel
                    control={<Checkbox checked={isSltIntern === 1}
                                       onChange={(e) => setIsSltIntern(e.target.checked ? 1 : 0)}/>}
                    label="Is SLT Intern"
                />
                <FormControlLabel
                    control={<Checkbox checked={active === 1} onChange={(e) => setActive(e.target.checked ? 1 : 0)}/>}
                    label="Active"
                />
                <FormControl fullWidth margin="normal">
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
                </FormControl>

                {/* Address Section */}
                <Typography variant="h6" sx={{mt: 3}}>
                    Addresses
                </Typography>

                {addresses.map((address) => (
                    <Grid container spacing={2} key={address.id} sx={{mt: 2}}>
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
                                sx={{ml: 2}}
                            >
                                Remove Address
                            </Button>
                        </Grid>
                    </Grid>
                ))}
                <Button
                    variant="outlined"
                    onClick={handleAddAddress}
                    sx={{mt: 2}}
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
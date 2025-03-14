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
import dynamic from 'next/dynamic'

const EmployeeSelectionDialog = dynamic(() => import('../components/EmployeeSelectionDialog'), {
    ssr: false,
});
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

    const [employeeId, setEmployeeId] = useState(employee?.employeeId || '');
    const [sltId, setSltId] = useState(employee?.sltId || '');

    const [selectedRoles, setSelectedRoles] = useState(employee?.roles.map(role => role.name) || []);
    const [selectedSections, setSelectedSections] = useState(employee?.sections.map(section => section.section) || []);
    const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles.map(profile => profile.name) || []);

    const [addresses, setAddresses] = useState(employee?.addresses || []);
    const [defaultAddress, setDefaultAddress] = useState(employee?.defaultAddress || 0);

    const [hodDialogOpen, setHodDialogOpen] = useState(false);
    const [supervisorDialogOpen, setSupervisorDialogOpen] = useState(false);
    const [otherEmployeeDialogOpen, setOtherEmployeeDialogOpen] = useState(false);

    const [selectedHod, setSelectedHod] = useState(employee?.hod || null);
    const [selectedSupervisor, setSelectedSupervisor] = useState(employee?.supervisor || null);
    const [selectedOtherEmployee, setSelectedOtherEmployee] = useState(employee?.otherEmployee || null);
    const [hodRoles, setHodRoles] = useState([]);
    const [sup, setSup] = useState([]);

    const handleSelectHod = (hod) => {
        setSelectedHod(hod);
        setHodDialogOpen(false);
    };

    const handleSelectSupervisor = (supervisor) => {
        setSelectedSupervisor(supervisor);
        setSupervisorDialogOpen(false);
    };

    const handleSelectOtherEmployee = (employee) => {
        setSelectedOtherEmployee(employee);
        setOtherEmployeeDialogOpen(false);
    };

    useEffect(() => {
        fetch("http://localhost:8080/users/get-role/ROLE_HOD")
            .then((response) => response.json())
            .then((result) => {
                setHodRoles(result); // Updates state asynchronously
            })
            .catch((error) => console.error(error));

        fetch("http://localhost:8080/users/get-role/ROLE_SUPERVISOR")
            .then((response) => response.json())
            .then((result) => {
                setSup(result); // Updates state asynchronously
            })
            .catch((error) => console.error(error));
    }, []);

    useEffect(() => {

    }, [hodRoles, sup]);

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
        const addressData = addresses.map(addr => ({
            city: addr.city,
            country: 'LK', // Hardcoded for simplicity
            streetName: addr.street,
            postalCode: addr.zip,
            isDefault: addr.isDefault,
        }));
        const newEmployee = {
            id: employee?.id, // Optional if adding a new employee
            firstName,
            lastName,
            email,
            password,
            employeeId: employeeId, // Hardcoded for simplicity
            sltId: sltId, // Hardcoded for simplicity
            addresses: addressData,
            roles: selectedRoles.map(roleName => roleName),
            sections: selectedSections.map(sectionName => sectionName),
            profiles: selectedProfiles.map(profileName => profileName),
            isSltEmp: isSltEmp,
            isSltIntern: isSltIntern,
            active: active,
            phone,
            gender,
            hod: null, // Assuming this is not required
            supervisor: null, // Assuming this is not required
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
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="SLT ID"
                    value={sltId}
                    onChange={(e) => setSltId(e.target.value.trim())}
                />
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="PEO TV ID"
                    value={employeeId}
                    onChange={(e) => setEmployeeId(e.target.value.trim())}
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
                    control={<Checkbox checked={isSltIntern === 1}
                        onChange={(e) => setIsSltIntern(e.target.checked ? 1 : 0)} />}
                    label="Is Roaster"
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
                                value={address.streetName}
                                onChange={(e) =>
                                    handleAddressChange(address.id, 'Street', e.target.value)
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
                                label="Postal Code"
                                value={address.postalCode}
                                onChange={(e) =>
                                    handleAddressChange(address.postalCode, 'city', e.target.value)
                                }
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Country"
                                value={address.country}
                                onChange={(e) =>
                                    handleAddressChange(address.id, 'Country', e.target.value)
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
                <Typography variant="h6" sx={{ mt: 3 }}>
                    Assign HOD, Supervisor, or Other Employee
                </Typography>

                <Grid container spacing={2} sx={{ mt: 2 }}>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="HOD"
                            value={selectedHod ? selectedHod.id : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setHodDialogOpen(true)}
                            sx={{ mt: 1 }}
                        >
                            Select HOD
                        </Button>
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Supervisor"
                            value={selectedSupervisor ? selectedSupervisor.id : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setSupervisorDialogOpen(true)}
                            sx={{ mt: 1 }}
                        >
                            Select Supervisor
                        </Button>
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Other Employee"
                            value={selectedOtherEmployee ? selectedOtherEmployee.id : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setOtherEmployeeDialogOpen(true)}
                            sx={{ mt: 1 }}
                        >
                            Select Other Employee
                        </Button>
                    </Grid>
                </Grid>
                <EmployeeSelectionDialog
                    open={hodDialogOpen}
                    onClose={() => setHodDialogOpen(false)}
                    onSelect={handleSelectHod}
                    employees={hodRoles.map(hod => ({
                        id: hod.employeeId, // Using userId as a unique identifier
                        name: `${hod.firstName} ${hod.lastName}` // Combining first and last name
                    }))}
                    title="Select HOD"
                />

                <EmployeeSelectionDialog
                    open={supervisorDialogOpen}
                    onClose={() => setSupervisorDialogOpen(false)}
                    onSelect={handleSelectSupervisor}
                    employees={sup.map(sup => ({
                        id: sup.employeeId, // Using userId as a unique identifier
                        name: `${sup.firstName} ${sup.lastName}` // Combining first and last name
                    }))}
                    title="Select Supervisor"
                />

                <EmployeeSelectionDialog
                    open={otherEmployeeDialogOpen}
                    onClose={() => setOtherEmployeeDialogOpen(false)}
                    onSelect={handleSelectOtherEmployee}
                    employees={[
                        { id: '5', name: 'Other Employee 1' },
                        { id: '6', name: 'Other Employee 2' },
                        // Add more Other Employees here
                    ]}
                    title="Select Other Employee"
                />
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


export default EmployeeDialog;
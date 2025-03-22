"use client";

import React, {useEffect, useMemo, useState} from "react";
import {
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Radio,
    Select,
    TextField,
    Typography,
} from "@mui/material";
import dynamic from 'next/dynamic'

const EmployeeSelectionDialog = dynamic(() => import('./EmployeeSelectionDialog'), {
    ssr: false,
});
const EmployeeDialog = React.memo(({open, onClose, onSave, employee, roles, sections, profiles}) => {
    const [firstName, setFirstName] = useState(employee?.firstName || '');
    const [lastName, setLastName] = useState(employee?.lastName || '');
    const [email, setEmail] = useState(employee?.email || '');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState(employee?.phone || '');
    const [gender, setGender] = useState(employee?.gender || '');
    const [isSltEmp, setIsSltEmp] = useState(employee?.isSltEmp || 0);
    const [isSltIntern, setIsSltIntern] = useState(employee?.isSltIntern || 0);
    const [isRoaster, setIsRoaster] = useState(employee?.isRoaster || 0);

    const [active, setActive] = useState(employee?.active || 1);
    const [employeeId, setEmployeeId] = useState(employee?.employeeId || '');
    const [sltId, setSltId] = useState(employee?.sltId || '');
    const [userId, setUserId] = useState(employee?.userId || '');

    const [selectedRoles, setSelectedRoles] = useState(employee?.roles.map(role => role.name) || []);
    const [selectedSections, setSelectedSections] = useState(employee?.sections.map(section => section.section) || []);
    const [selectedProfiles, setSelectedProfiles] = useState(employee?.profiles.map(profile => profile.name) || []);

    const [addresses, setAddresses] = useState(employee?.addresses || []);
    const [defaultAddress, setDefaultAddress] = useState(employee?.defaultAddress || 0);

    const [hodDialogOpen, setHodDialogOpen] = useState(false);
    const [supervisorDialogOpen, setSupervisorDialogOpen] = useState(false);
    const [otherEmployeeDialogOpen, setOtherEmployeeDialogOpen] = useState(false);


    const [hodRoles, setHodRoles] = useState([]);
    const [sup, setSup] = useState([]);
    const [otherRoles, setOtherRoles] = useState([]);

    const [selectedHod, setSelectedHod] = useState(employee?.hod || {});
    const [selectedSupervisor, setSelectedSupervisor] = useState(employee?.supervisor || {});
    const [selectedOtherEmployee, setSelectedOtherEmployee] = useState(employee?.other || {});

    const [errors, setErrors] = useState({});

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

    const memoizedHodRoles = useMemo(() => hodRoles.map(hod => ({
        employeeId: hod.employeeId,
        name: `${hod.firstName} ${hod.lastName}`
    })), [hodRoles]);

    const memoizedSupRoles = useMemo(() => sup.map(sup => ({
        employeeId: sup.employeeId,
        name: `${sup.firstName} ${sup.lastName}`
    })), [sup]);

    const memoizedOtherRoles = useMemo(() => otherRoles.map(other => ({
        employeeId: other.employeeId,
        name: `${other.firstName} ${other.lastName}`
    })), [otherRoles]);

    useEffect(() => {
        const fetchRoles = async () => {
            try {
                const hodResponse = await fetch("http://localhost:8080/users/get-role/ROLE_HOD");
                const hodData = await hodResponse.json();
                setHodRoles(hodData);

                const supResponse = await fetch("http://localhost:8080/users/get-role/ROLE_SUPERVISOR");
                const supData = await supResponse.json();
                setSup(supData);

                const otherResponse = await fetch("http://localhost:8080/users/get-role/ROLE_CEO");
                const otherData = await otherResponse.json();
                setOtherRoles(otherData);
            } catch (error) {
                console.error("Error fetching roles:", error);
            }
        };

        fetchRoles();
    }, []); // Empty dependency array to run only once

    useEffect(() => {

    }, [hodRoles, sup, otherRoles]);

    React.useEffect(() => {
        if (employee) {
            setSelectedRoles(employee.roles.map(role => role.name) || []);
            setSelectedSections(employee.sections.map(section => section.section) || []);
            setSelectedProfiles(employee.profiles.map(profile => profile.name) || []);
            setSelectedHod(employee['hod'] || {});
            setSelectedSupervisor(employee['supervisor'] || {});
            setSelectedOtherEmployee(employee['other'] || {});
        } else {
            setSelectedRoles([]);
            setSelectedSections([]);
            setSelectedProfiles([]);
            setSelectedHod({});
            setSelectedSupervisor({});
            setSelectedOtherEmployee({});
        }
    }, [employee]); // Only `employee` is included in the dependency array

    const handleSave = () => {
        const newErrors = {};

        if (!firstName) {
            newErrors.firstName = "First Name is required.";
        }
        if (!lastName) {
            newErrors.lastName = "Last Name is required.";
        }
        if (!email) {
            newErrors.email = "Email is required.";
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = "Email is invalid.";
        }
        if (!sltId) {
            newErrors.sltId = "SLT ID is required.";
        }
        if (!employeeId) {
            newErrors.employeeId = "PEO TV ID is required.";
        }
        if (!password) {
            newErrors.password = "Password is required.";
        }
        if (!phone) {
            newErrors.phone = "Phone Number is required.";
        }
        if (selectedRoles.length === 0) {
            newErrors.roles = "At least one role is required.";
        }
        if (selectedSections.length === 0) {
            newErrors.sections = "At least one section is required.";
        }
        if (selectedProfiles.length === 0) {
            newErrors.profiles = "At least one profile is required.";
        }
        if (!gender) {
            newErrors.gender = "Gender is required.";
        }
        if (addresses.length === 0) {
            newErrors.addresses = "At least one address is required.";
        } else {
            addresses.forEach((address, index) => {
                if (!address.streetName) {
                    newErrors[`streetName-${index}`] = "Street Name is required.";
                }
                if (!address.city) {
                    newErrors[`city-${index}`] = "City is required.";
                }
                if (!address.postalCode) {
                    newErrors[`postalCode-${index}`] = "Postal Code is required.";
                }
                if (!address.country) {
                    newErrors[`country-${index}`] = "Country is required.";
                }
            });
        }

        // If there are errors, stop submission and set errors
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        // Clear errors if validation passes
        setErrors({});
        const addressData = addresses.map(addr => ({
            city: addr.city,
            country: 'LK', // Hardcoded for simplicity
            streetName: addr.streetName,
            postalCode: addr.postalCode,
            isDefault: addr.isDefault,
        }));
        const newEmployee = {
            id: employee?.id, // Optional if adding a new employee
            userId: userId,
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
            roaster: isRoaster,
            phone,
            gender,
            hod: selectedHod.employeeId,
            supervisor: selectedSupervisor.employeeId,
            other: selectedOtherEmployee.employeeId
        };

        onSave(newEmployee);
    };

    const handleAddAddress = () => {
        const newAddress = {
            id: addresses.length + 1,
            streetName: '',
            city: '',
            state: '',
            postalCode: '',
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
                    error={!!errors.firstName}
                    helperText={errors.firstName}
                />
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Last Name"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value.trim())}
                    error={!!errors.lastName}
                    helperText={errors.lastName}
                />
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    error={!!errors.email}
                    helperText={errors.email}
                />
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="SLT ID"
                    value={sltId}
                    onChange={(e) => setSltId(e.target.value.trim())}
                    error={!!errors.sltId}
                    helperText={errors.sltId}
                />
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="PEO TV ID"
                    value={employeeId}
                    onChange={(e) => setEmployeeId(e.target.value.trim())}
                    error={!!errors.employeeId}
                    helperText={errors.employeeId}
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
                            error={!!errors.password}
                            helperText={errors.password}
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
                    error={!!errors.phone}
                    helperText={errors.phone}
                />
                <FormControl fullWidth margin="normal" error={!!errors.roles}>
                    <InputLabel>Roles</InputLabel>
                    <Select
                        multiple
                        value={selectedRoles}
                        onChange={(e) => setSelectedRoles(e.target.value)}
                        renderValue={(selected) => selected.join(", ")}
                    >
                        {roles.map((role) => (
                            <MenuItem key={role} value={role}>
                                {role}
                            </MenuItem>
                        ))}
                    </Select>
                    {errors.roles && <FormHelperText>{errors.roles}</FormHelperText>}
                </FormControl>
                <FormControl fullWidth margin="normal" error={!!errors.sections}>
                    <InputLabel>Sections</InputLabel>
                    <Select
                        multiple
                        value={selectedSections}
                        onChange={(e) => setSelectedSections(e.target.value)}
                        renderValue={(selected) => selected.join(", ")}
                    >
                        {sections.map((section) => (
                            <MenuItem key={section} value={section}>
                                {section}
                            </MenuItem>
                        ))}
                    </Select>
                    {errors.sections && <FormHelperText>{errors.sections}</FormHelperText>}
                </FormControl>

                <FormControl fullWidth margin="normal" error={!!errors.profiles}>
                    <InputLabel>Profiles</InputLabel>
                    <Select
                        multiple
                        value={selectedProfiles}
                        onChange={(e) => setSelectedProfiles(e.target.value)}
                        renderValue={(selected) => selected.join(", ")}
                    >
                        {profiles.map((profile) => (
                            <MenuItem key={profile} value={profile}>
                                {profile}
                            </MenuItem>
                        ))}
                    </Select>
                    {errors.profiles && <FormHelperText>{errors.profiles}</FormHelperText>}
                </FormControl>

                <FormControl fullWidth margin="normal" error={!!errors.gender}>
                    <InputLabel>Gender</InputLabel>
                    <Select
                        value={gender}
                        onChange={(e) => setGender(e.target.value)}
                    >
                        <MenuItem value="M">Male</MenuItem>
                        <MenuItem value="F">Female</MenuItem>
                    </Select>
                    {errors.gender && <FormHelperText>{errors.gender}</FormHelperText>}
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
                    control={<Checkbox checked={isRoaster === 1}
                                       onChange={(e) => setIsRoaster(e.target.checked ? 1 : 0)}/>}
                    label="Is Roaster"
                />
                <FormControlLabel
                    control={<Checkbox checked={active === 1} onChange={(e) => setActive(e.target.checked ? 1 : 0)}/>}
                    label="Active"
                />

                <Typography variant="h6" sx={{mt: 3}}>
                    Addresses
                </Typography>

                {addresses.map((address, index) => (
                    <Grid container spacing={2} key={address.id} sx={{mt: 2}}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Street"
                                value={address.streetName}
                                onChange={(e) =>
                                    handleAddressChange(address.id, "streetName", e.target.value)
                                }
                                error={!!errors[`streetName-${index}`]}
                                helperText={errors[`streetName-${index}`]}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="City"
                                value={address.city}
                                onChange={(e) =>
                                    handleAddressChange(address.id, "city", e.target.value)
                                }
                                error={!!errors[`city-${index}`]}
                                helperText={errors[`city-${index}`]}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Postal Code"
                                value={address.postalCode}
                                onChange={(e) =>
                                    handleAddressChange(address.id, "postalCode", e.target.value)
                                }
                                error={!!errors[`postalCode-${index}`]}
                                helperText={errors[`postalCode-${index}`]}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Country"
                                value={address.country}
                                onChange={(e) =>
                                    handleAddressChange(address.id, "country", e.target.value)
                                }
                                error={!!errors[`country-${index}`]}
                                helperText={errors[`country-${index}`]}
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
                {errors.addresses && (
                    <Typography color="error" sx={{mt: 2}}>
                        {errors.addresses}
                    </Typography>
                )}
                <Button
                    variant="outlined"
                    onClick={handleAddAddress}
                    sx={{mt: 2}}
                >
                    Add Address
                </Button>
                <Typography variant="h6" sx={{mt: 3}}>
                    Assign HOD, Supervisor, or Other Employee
                </Typography>

                <Grid container spacing={2} sx={{mt: 2}}>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="HOD"
                            value={selectedHod ? selectedHod.employeeId : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setHodDialogOpen(true)}
                            sx={{mt: 1}}
                        >
                            Select HOD
                        </Button>
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Supervisor"
                            value={selectedSupervisor ? selectedSupervisor.employeeId : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setSupervisorDialogOpen(true)}
                            sx={{mt: 1}}
                        >
                            Select Supervisor
                        </Button>
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Other Employee"
                            value={selectedOtherEmployee ? selectedOtherEmployee.employeeId : ''}
                            InputProps={{
                                readOnly: true,
                            }}
                        />
                        <Button
                            variant="outlined"
                            onClick={() => setOtherEmployeeDialogOpen(true)}
                            sx={{mt: 1}}
                        >
                            Select Other Employee
                        </Button>
                    </Grid>
                </Grid>

                <EmployeeSelectionDialog
                    open={hodDialogOpen}
                    onClose={() => setHodDialogOpen(false)}
                    onSelect={handleSelectHod}
                    employees={memoizedHodRoles}
                    title="Select HOD"
                />
                <EmployeeSelectionDialog
                    open={supervisorDialogOpen}
                    onClose={() => setSupervisorDialogOpen(false)}
                    onSelect={handleSelectSupervisor}
                    employees={memoizedSupRoles}
                    title="Select Supervisor"
                />
                <EmployeeSelectionDialog
                    open={otherEmployeeDialogOpen}
                    onClose={() => setOtherEmployeeDialogOpen(false)}
                    onSelect={handleSelectOtherEmployee}
                    employees={memoizedOtherRoles}
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
});


export default EmployeeDialog;
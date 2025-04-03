'use client';

import React, {useCallback, useEffect, useMemo, useState} from "react";
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
import dynamic from 'next/dynamic';
import { useDispatch } from 'react-redux';
import { setCurrentAdminPage } from '../../../store/managementSlice';

const EmployeeSelectionDialog = dynamic(() => import('./EmployeeSelectionDialog'), {
    ssr: false,
});

// Helper function for deep comparison
const deepEqual = (a, b) => {
    if (a === b) return true;
    if (typeof a !== 'object' || a === null || typeof b !== 'object' || b === null) return false;

    const keysA = Object.keys(a);
    const keysB = Object.keys(b);

    if (keysA.length !== keysB.length) return false;

    for (const key of keysA) {
        if (!keysB.includes(key)) return false;
        if (!deepEqual(a[key], b[key])) return false;
    }

    return true;
};

// Initial form state
const INITIAL_FORM_DATA = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    gender: '',
    isSltEmp: 0,
    isSltIntern: 0,
    isRoaster: 0,
    active: 1,
    employeeId: '',
    sltId: '',
    userId: '',
    selectedRoles: [],
    selectedSections: [],
    selectedProfiles: [],
    addresses: [],
    defaultAddress: 0,
    selectedHod: {},
    selectedSupervisor: {},
    selectedOtherEmployee: {},
    joiningDate: null,
    deleteAddresses: [],
    additional: {},
    admins: [],
    addedAdmins: [],
    deletedAdmins: []
};

const EmployeeDialog = React.memo(({
                                       open,
                                       onClose,
                                       onSave,
                                       employee,
                                       roles,
                                       sections,
                                       profiles,
                                       saveLoading,

                                       paginatedAdmins,
                                       currentAdminPage,
                                       adminPageSize,
                                       isLoading

                                   }) => {
    const [formData, setFormData] = useState(INITIAL_FORM_DATA);
    const [errors, setErrors] = useState({});
    const [isFormDirty, setIsFormDirty] = useState(false);
    const [hodDialogOpen, setHodDialogOpen] = useState(false);
    const [supervisorDialogOpen, setSupervisorDialogOpen] = useState(false);
    const [otherEmployeeDialogOpen, setOtherEmployeeDialogOpen] = useState(false);
    const [isInitialized, setIsInitialized] = useState(false);

    // Memoized employee data for comparison
    const memoizedEmployee = useMemo(() => employee, [JSON.stringify(employee)]);

    const dispatch = useDispatch();

    const handlePageChange = useCallback((newPage) => {
        dispatch(setCurrentAdminPage(newPage));
    }, [dispatch]);

    // Initialize form with employee data only once when dialog opens or employee changes
    useEffect(() => {
        if (!open) {
            setIsInitialized(false);
            return;
        }
        if (!isInitialized) {
            setFormData(memoizedEmployee ? {
                firstName: memoizedEmployee.firstName || '',
                lastName: memoizedEmployee.lastName || '',
                email: memoizedEmployee.email || '',
                password: '',
                phone: memoizedEmployee.phone || '',
                gender: memoizedEmployee.gender || '',
                isSltEmp: memoizedEmployee.isSltEmp || 0,
                isSltIntern: memoizedEmployee.isSltIntern || 0,
                isRoaster: memoizedEmployee.isRoaster || 0,
                active: memoizedEmployee.active || 1,
                employeeId: memoizedEmployee.employeeId || '',
                sltId: memoizedEmployee.sltId || '',
                userId: memoizedEmployee.userId || '',
                selectedRoles: memoizedEmployee.roles?.map(role => role.name) || [],
                selectedSections: memoizedEmployee.sections?.map(section => section.section) || [],
                selectedProfiles: memoizedEmployee.profiles || [],
                addresses: (() => {
                    const addresses = memoizedEmployee.addresses || [];
                    // Count how many addresses are marked as default
                    const defaultCount = addresses.filter(addr => addr.isDefault).length;

                    // If multiple defaults or no defaults, reset all to false
                    const shouldResetDefaults = defaultCount !== 1;

                    return addresses.map((addr, index) => ({
                        ...addr,
                        id: addr.addressId || addr.id || `temp-${Date.now()}`, // Use string IDs
                        isDefault: shouldResetDefaults ? false : addr.isDefault
                    }));
                })() || [],
                defaultAddress: memoizedEmployee.defaultAddress || 0,
                selectedHod: memoizedEmployee.hod || {},
                selectedSupervisor: memoizedEmployee.supervisor || {},
                selectedOtherEmployee: memoizedEmployee.other || {},
                joiningDate: memoizedEmployee?.join_date ? new Date(memoizedEmployee.join_date) : new Date(),
                deleteAddresses: [],
                additional: {},
                admins: [],
                addedAdmins: [],
                deletedAdmins: []
            } : INITIAL_FORM_DATA);

            setIsFormDirty(false);
            setErrors({});
            setIsInitialized(true);
        }
    }, [open, memoizedEmployee, isInitialized]);

    useEffect(() => {
        console.log(paginatedAdmins)
        console.log(saveLoading)
    }, []);

    // Optimized form handlers with debouncing for rapid changes
    const handleChange = useCallback((field, value) => {
        setFormData(prev => {
            if (prev[field] === value) return prev;
            return {...prev, [field]: value};
        });
        setIsFormDirty(true);
    }, []);

    const handleChangeForNonBoolean = useCallback((field, newValue) => {
        setFormData(prev => {
            const previousSelection = prev[field] || [];

            // Calculate net changes (what was truly added/removed)
            const netAdded = newValue.filter(item => !previousSelection.includes(item));
            const netRemoved = previousSelection.filter(item => !newValue.includes(item));

            // If no real change, reset additional data
            if (prev[field] === newValue) {
                return {
                    ...prev,
                    additional: {
                        ...prev.additional,
                        [`added${capitalizeFirstLetter(field.replace('selected', ''))}`]: [],
                        [`delete${capitalizeFirstLetter(field.replace('selected', ''))}`]: [],
                    }
                };
            }

            // Update the field and track changes in additional data
            return {
                ...prev,
                [field]: newValue,
                additional: {
                    ...prev.additional,
                    [`added${capitalizeFirstLetter(field.replace('selected', ''))}`]: netAdded,
                    [`delete${capitalizeFirstLetter(field.replace('selected', ''))}`]: netRemoved,
                }
            };
        });
        setIsFormDirty(true);
    }, []);

    // Helper function to capitalize first letter (e.g., "roles" → "Roles")
    const capitalizeFirstLetter = (str) => {
        return str.charAt(0).toUpperCase() + str.slice(1);
    };

    const handleSelectHod = useCallback((hod) => {
        setFormData(prev => {
            if (deepEqual(prev.selectedHod, hod)) return prev;
            return {...prev, selectedHod: hod};
        });
        setHodDialogOpen(false);
        setIsFormDirty(true);
    }, []);

    const handleSelectSupervisor = useCallback((supervisor) => {
        setFormData(prev => {
            if (deepEqual(prev.selectedSupervisor, supervisor)) return prev;
            return {...prev, selectedSupervisor: supervisor};
        });
        setSupervisorDialogOpen(false);
        setIsFormDirty(true);
    }, []);

    const handleSelectOtherEmployee = useCallback((employee) => {
        setFormData(prev => {
            if (deepEqual(prev.selectedOtherEmployee, employee)) return prev;
            return {...prev, selectedOtherEmployee: employee};
        });
        setOtherEmployeeDialogOpen(false);
        setIsFormDirty(true);
    }, []);

    // Address handlers with optimization
    const handleAddAddress = useCallback(() => {
        setFormData(prev => {
            const newAddress = {
                id: Date.now(), // Using timestamp for unique IDs
                streetName: '',
                city: '',
                state: '',
                postalCode: '',
                country: 'LK',
                isDefault: false, // Set as default only if it's the first address
            };
            return {...prev, addresses: [...prev.addresses, newAddress]};
        });
        setIsFormDirty(true);
    }, []);


    const handleRemoveAddress = useCallback((id) => {
        setFormData(prev => {
            const addressToRemove = prev.addresses.find(addr => addr.id === id);
            const isExistingAddress = addressToRemove && !addressToRemove.id.startsWith('temp-');

            return {
                ...prev,
                addresses: prev.addresses.filter(addr => addr.id !== id),
                deleteAddresses: isExistingAddress
                    ? [...prev.deleteAddresses, id]
                    : prev.deleteAddresses
            };
        });
        setIsFormDirty(true);
    }, []);

    const handleAddressChange = useCallback((id, field, value) => {
        setFormData(prev => {
            const addressIndex = prev.addresses.findIndex(addr => addr.id === id);
            if (addressIndex === -1 || prev.addresses[addressIndex][field] === value) {
                return prev;
            }

            const newAddresses = [...prev.addresses];
            newAddresses[addressIndex] = {...newAddresses[addressIndex], [field]: value};

            return {...prev, addresses: newAddresses};
        });
        setIsFormDirty(true);
    }, []);

    const handleSetDefaultAddress = useCallback((id) => {
        setFormData(prev => {
            // First set all addresses to non-default
            const resetAddresses = prev.addresses.map(addr => ({
                ...addr,
                isDefault: false
            }));

            // Then find and set the selected address as default
            const addressIndex = resetAddresses.findIndex(addr => addr.id === id);
            if (addressIndex >= 0) {
                resetAddresses[addressIndex] = {
                    ...resetAddresses[addressIndex],
                    isDefault: true
                };
            }

            return {...prev, addresses: resetAddresses};
        });
        setIsFormDirty(true);
    }, []);

    // Password generation
    const generateTemporaryPassword = useCallback(() => {
        const length = 10;
        const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()";
        let password = "";
        for (let i = 0; i < length; i++) {
            const randomIndex = Math.floor(Math.random() * charset.length);
            password += charset[randomIndex];
        }
        return password;
    }, []);

    const handleGenerateTemporaryPassword = useCallback(() => {
        const tempPassword = generateTemporaryPassword();
        setFormData(prev => ({...prev, password: tempPassword}));
        setIsFormDirty(true);
    }, [generateTemporaryPassword]);

    // Check if form has changes with deep comparison
    const hasChanges = useMemo(() => {
        if (!isFormDirty) return false;
        if (!memoizedEmployee) return true;

        const compareArrays = (a, b) => {
            if (a.length !== b.length) return false;
            return a.every((val, index) => val === b[index]);
        };

        const compareObjects = (a, b) => {
            const keysA = Object.keys(a);
            const keysB = Object.keys(b);
            if (keysA.length !== keysB.length) return false;
            return keysA.every(key => a[key] === b[key]);
        };

        const compareAddresses = (a, b) => {
            if (a.length !== b.length) return false;
            return a.every((addr, index) => {
                return addr.streetName === b[index].streetName &&
                    addr.city === b[index].city &&
                    addr.postalCode === b[index].postalCode &&
                    addr.country === b[index].country &&
                    addr.isDefault === b[index].isDefault;
            });
        };

        return (
            formData.firstName !== memoizedEmployee.firstName ||
            formData.lastName !== memoizedEmployee.lastName ||
            formData.email !== memoizedEmployee.email ||
            formData.phone !== memoizedEmployee.phone ||
            formData.gender !== memoizedEmployee.gender ||
            formData.isSltEmp !== memoizedEmployee.isSltEmp ||
            formData.isSltIntern !== memoizedEmployee.isSltIntern ||
            formData.isRoaster !== memoizedEmployee.isRoaster ||
            formData.active !== memoizedEmployee.active ||
            formData.employeeId !== memoizedEmployee.employeeId ||
            formData.sltId !== memoizedEmployee.sltId ||
            !compareArrays(formData.selectedRoles, memoizedEmployee.roles?.map(r => r.name) || []) ||
            !compareArrays(formData.selectedSections, memoizedEmployee.sections?.map(s => s.section) || []) ||
            !compareArrays(formData.selectedProfiles, memoizedEmployee.profiles || []) ||
            !compareArrays(formData.admins, memoizedEmployee.admins || []) ||
            !compareArrays(formData.addedAdmins, memoizedEmployee.addedAdmins || []) ||
            !compareArrays(formData.deletedAdmins, memoizedEmployee.deletedAdmins || []) ||

            // !compareObjects(formData.selectedHod, memoizedEmployee.hod || {}) ||
            // !compareObjects(formData.selectedSupervisor, memoizedEmployee.supervisor || {}) ||
            // !compareObjects(formData.selectedOtherEmployee, memoizedEmployee.other || {}) ||
            !compareAddresses(formData.addresses, memoizedEmployee.addresses?.map(a => ({
                ...a,
                id: a.id || 0,
                isDefault: a.isDefault || false
            })) || [])
        );
    }, [formData, memoizedEmployee, isFormDirty]);

    // Form submission with enhanced validation
    const handleSave = useCallback(async () => {
        // Early return if no changes
        if (!hasChanges) {
            onClose();
            return;
        }

        const newErrors = {};

        if (hasDuplicateAddresses) {
            setErrors({...errors, addresses: "Duplicate addresses found"});
            return;
        }

        // Check exactly one default address
        const defaultCount = formData.addresses.filter(addr => addr.isDefault).length;
        if (defaultCount !== 1) {
            setErrors({...errors, addresses: "Exactly one address must be set as default"});
            return;
        }


        // Validation
        if (!formData.firstName.trim()) newErrors.firstName = "First Name is required.";
        if (!formData.lastName.trim()) newErrors.lastName = "Last Name is required.";
        if (!formData.email.trim()) {
            newErrors.email = "Email is required.";
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            newErrors.email = "Email is invalid.";
        }
        if (!formData.sltId.trim()) newErrors.sltId = "SLT ID is required.";
        if (!formData.employeeId.trim()) newErrors.employeeId = "PEO TV ID is required.";
        if (!formData.password && !memoizedEmployee?.userId) newErrors.password = "Password is required.";
        if (!formData.phone.trim()) newErrors.phone = "Phone Number is required.";
        if (formData.selectedRoles.length === 0) newErrors.roles = "At least one role is required.";
        if (formData.selectedSections.length === 0) newErrors.sections = "At least one section is required.";
        if (formData.selectedProfiles.length === 0) newErrors.profiles = "At least one profile is required.";
        if (!formData.joiningDate) newErrors.joiningDate = "Joining date is required";
        if (!formData.gender) newErrors.gender = "Gender is required.";
        if (formData.addresses.length === 0) {
            newErrors.addresses = "At least one address is required.";
        } else {
            formData.addresses.forEach((address, index) => {
                if (!address.streetName.trim()) newErrors[`streetName-${index}`] = "Street Name is required.";
                if (!address.city.trim()) newErrors[`city-${index}`] = "City is required.";
                if (!address.postalCode.trim()) newErrors[`postalCode-${index}`] = "Postal Code is required.";
                if (!address.country.trim()) newErrors[`country-${index}`] = "Country is required.";
            });
        }
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        try {
            const addressData = formData.addresses.map(addr => ({
                addressId: addr.id,
                city: addr.city,
                country: addr.country || 'LK',
                streetName: addr.streetName,
                postalCode: addr.postalCode,
                isDefault: addr.isDefault,
            }));

            const employeeData = {
                userId: formData.userId,
                firstName: formData.firstName.trim(),
                lastName: formData.lastName.trim(),
                email: formData.email.trim(),
                password: formData.password,
                employeeId: formData.employeeId.trim(),
                sltId: formData.sltId.trim(),
                addresses: addressData,
                roles: formData.selectedRoles || [],
                sections: formData.selectedSections || [],
                profiles: formData.selectedProfiles.map(profile =>
                    typeof profile === 'string' ? profile : profile.id // or profile.name, depending on your structure
                ) || [],
                isSltEmp: formData.isSltEmp || 0,
                isSltIntern: formData.isSltIntern || 0,
                active: formData.active || 0,
                roaster: formData.isRoaster || 0,
                phone: formData.phone.trim(),
                gender: formData.gender,
                hod: formData.selectedHod ? formData.selectedHod.employeeId || '' : '',
                supervisor: formData.selectedHod ? formData.selectedSupervisor.employeeId || '' : '',
                other: formData.selectedHod ? formData.selectedOtherEmployee.employeeId || '' : '',
                profilePic: formData.profilePic || '',
                Authorities: formData.authorities || [],
                deleteAddresses: formData.deleteAddresses || [],
                joiningDate: formData.joiningDate.toISOString(),
                additional: formData.additional,
                admins: formData.admins,
                addedAdmins: formData.addedAdmins,
                deletedAdmins: formData.deletedAdmins
            };
            await onSave(employeeData);

        } catch (error) {
            console.error("Error saving employee:", error);
        }
    }, [formData, memoizedEmployee, hasChanges, onSave, onClose]);

    const formatDateForInput = (date) => {
        if (!date) return '';
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const hasDuplicateAddresses = formData.addresses.some((addr, index) => {
        return formData.addresses.some((otherAddr, otherIndex) => {
            if (index === otherIndex) return false;
            return (
                addr.city === otherAddr.city &&
                addr.streetName === otherAddr.streetName &&
                addr.postalCode === otherAddr.postalCode
            );
        });
    });

    // Memoized form sections to prevent unnecessary re-renders
    const renderBasicInfoSection = useMemo(() => (
        <>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="First Name"
                    value={formData.firstName}
                    onChange={(e) => handleChange('firstName', e.target.value)}
                    error={!!errors.firstName}
                    helperText={errors.firstName}
                    disabled={saveLoading}
                />
            </Grid>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Last Name"
                    value={formData.lastName}
                    onChange={(e) => handleChange('lastName', e.target.value.trim())}
                    error={!!errors.lastName}
                    helperText={errors.lastName}
                    disabled={saveLoading}
                />
            </Grid>
        </>
    ), [formData.firstName, formData.lastName, errors.firstName, errors.lastName, saveLoading]);

    const renderContactInfoSection = useMemo(() => (
        <>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => handleChange('email', e.target.value)}
                    error={!!errors.email}
                    helperText={errors.email}
                    disabled={saveLoading}
                />
            </Grid>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Phone Number"
                    value={formData.phone}
                    onChange={(e) => handleChange('phone', e.target.value)}
                    error={!!errors.phone}
                    helperText={errors.phone}
                    disabled={saveLoading}
                />
            </Grid>
        </>
    ), [formData.email, formData.phone, errors.email, errors.phone, saveLoading]);

    const renderIdSection = useMemo(() => (
        <>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="SLT ID"
                    value={formData.sltId}
                    onChange={(e) => handleChange('sltId', e.target.value.trim())}
                    error={!!errors.sltId}
                    helperText={errors.sltId}
                    disabled={saveLoading}
                />
            </Grid>
            <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="PEO TV ID"
                    value={formData.employeeId}
                    onChange={(e) => handleChange('employeeId', e.target.value.trim())}
                    error={!!errors.employeeId}
                    helperText={errors.employeeId}
                    disabled={saveLoading}
                />
            </Grid>
        </>
    ), [formData.sltId, formData.employeeId, errors.sltId, errors.employeeId, saveLoading]);

    const renderPasswordSection = useMemo(() => (
        <Grid item xs={12} sm={6}>
            <Grid container spacing={2} alignItems="center">
                <Grid item xs={9}>
                    <TextField
                        margin="normal"
                        required={!memoizedEmployee?.id}
                        fullWidth
                        label="Password"
                        type="password"
                        value={formData.password}
                        onChange={(e) => handleChange('password', e.target.value)}
                        error={!!errors.password}
                        helperText={errors.password}
                        disabled={saveLoading}
                    />
                </Grid>
                <Grid item xs={3}>
                    <Button
                        variant="outlined"
                        onClick={handleGenerateTemporaryPassword}
                        sx={{mt: 2}}
                        disabled={saveLoading}
                    >
                        Generate
                    </Button>
                </Grid>
            </Grid>
        </Grid>
    ), [formData.password, errors.password, memoizedEmployee, saveLoading, handleGenerateTemporaryPassword]);

    const renderGenderSection = useMemo(() => (
        <Grid item xs={11} sm={6}>
            <FormControl fullWidth margin="normal" error={!!errors.gender} disabled={saveLoading}>
                <InputLabel>Gender</InputLabel>
                <Select
                    value={formData.gender}
                    onChange={(e) => handleChange('gender', e.target.value)}
                    label="Gender"
                >
                    <MenuItem value="M">Male</MenuItem>
                    <MenuItem value="F">Female</MenuItem>
                </Select>
                {errors.gender && <FormHelperText>{errors.gender}</FormHelperText>}
            </FormControl>
        </Grid>
    ), [formData.gender, errors.gender, saveLoading]);

    const renderRolesSection = useMemo(() => (
        <Grid item xs={12} sm={6}>
            <FormControl fullWidth margin="normal" error={!!errors.roles} disabled={saveLoading}>
                <InputLabel>Roles</InputLabel>
                <Select
                    multiple
                    value={formData.selectedRoles}
                    onChange={(e) => handleChangeForNonBoolean('selectedRoles', e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                    label="Roles"
                >
                    {roles.map((role) => (
                        <MenuItem key={role} value={role}>
                            {role}
                        </MenuItem>
                    ))}
                </Select>
                {errors.roles && <FormHelperText>{errors.roles}</FormHelperText>}
            </FormControl>
        </Grid>
    ), [formData.selectedRoles, errors.roles, roles, saveLoading]);

    const renderSectionsSection = useMemo(() => (
        <Grid item xs={12} sm={6}>
            <FormControl fullWidth margin="normal" error={!!errors.sections} disabled={saveLoading}>
                <InputLabel>Sections</InputLabel>
                <Select
                    multiple
                    value={formData.selectedSections}
                    onChange={(e) => handleChangeForNonBoolean('selectedSections', e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                    label="Sections"
                >
                    {sections.map((section) => (
                        <MenuItem key={section} value={section}>
                            {section}
                        </MenuItem>
                    ))}
                </Select>
                {errors.sections && <FormHelperText>{errors.sections}</FormHelperText>}
            </FormControl>
        </Grid>
    ), [formData.selectedSections, errors.sections, sections, saveLoading]);

    const renderProfilesSection = useMemo(() => (
        <Grid item xs={12} sm={6}>
            <FormControl fullWidth margin="normal" error={!!errors.profiles} disabled={saveLoading}>
                <InputLabel>Profiles</InputLabel>
                <Select
                    multiple
                    value={formData.selectedProfiles}
                    onChange={(e) => handleChangeForNonBoolean('selectedProfiles', e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                    label="Profiles"
                >
                    {profiles.map((profile) => (
                        <MenuItem key={profile} value={profile}>
                            {profile}
                        </MenuItem>
                    ))}
                </Select>
                {errors.profiles && <FormHelperText>{errors.profiles}</FormHelperText>}
            </FormControl>
        </Grid>
    ), [formData.selectedProfiles, errors.profiles, profiles, saveLoading]);

    const renderCheckboxesSection = useMemo(() => (
        <Grid item xs={12}>
            <FormControlLabel
                control={
                    <Checkbox
                        checked={formData.isSltEmp === 1}
                        onChange={(e) => handleChange('isSltEmp', e.target.checked ? 1 : 0)}
                        disabled={saveLoading}
                    />
                }
                label="Is SLT Employee"
            />
            <FormControlLabel
                control={
                    <Checkbox
                        checked={formData.isSltIntern === 1}
                        onChange={(e) => handleChange('isSltIntern', e.target.checked ? 1 : 0)}
                        disabled={saveLoading}
                    />
                }
                label="Is SLT Intern"
            />
            <FormControlLabel
                control={
                    <Checkbox
                        checked={formData.isRoaster === 1}
                        onChange={(e) => handleChange('isRoaster', e.target.checked ? 1 : 0)}
                        disabled={saveLoading}
                    />
                }
                label="Is Roaster"
            />
            <FormControlLabel
                control={
                    <Checkbox
                        checked={formData.active === 1}
                        onChange={(e) => handleChange('active', e.target.checked ? 1 : 0)}
                        disabled={saveLoading}
                    />
                }
                label="Active"
            />
        </Grid>
    ), [formData.isSltEmp, formData.isSltIntern, formData.isRoaster, formData.active, saveLoading]);

    const renderAddressesSection = useMemo(() => (
        <Grid item xs={12}>
            <Typography variant="h6" sx={{mt: 3}}>
                Addresses
            </Typography>
            {formData.addresses.map((address, index) => (
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
                            disabled={saveLoading}
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
                            disabled={saveLoading}
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
                            disabled={saveLoading}
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
                            disabled={saveLoading}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <FormControlLabel
                            control={
                                <Radio
                                    checked={address.isDefault}
                                    onChange={() => handleSetDefaultAddress(address.id)}
                                    disabled={saveLoading}
                                />
                            }
                            label="Set as Default Address"
                        />
                        <Button
                            variant="outlined"
                            color="error"
                            onClick={() => handleRemoveAddress(address.id)}
                            sx={{ml: 2}}
                            disabled={saveLoading}
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
                disabled={saveLoading}
            >
                Add Address
            </Button>
        </Grid>
    ), [formData.addresses, errors, saveLoading, handleAddressChange, handleSetDefaultAddress, handleRemoveAddress, handleAddAddress]);

    const renderEmployeeAssignmentSection = useMemo(() => (
        <Grid item xs={12}>
            <Typography variant="h6" sx={{mt: 3}}>
                Assign Administratives
            </Typography>
            <Grid item xs={19} sm={4}>
                <Button
                    variant="outlined"
                    onClick={() => setHodDialogOpen(true)}
                    sx={{mt: 1}}
                    disabled={saveLoading}
                >
                    Select Administratives
                </Button>
            </Grid>
        </Grid>
    ), [formData.selectedHod, formData.selectedSupervisor, formData.selectedOtherEmployee, saveLoading]);

    const renderJoiningDateSection = useMemo(() => (
        <Grid item xs={12} sm={6}>
            <TextField
                required
                label="Joining Date"
                type="date"
                value={formData.joiningDate ?
                    formatDateForInput(formData.joiningDate) : ''}
                onChange={(e) => {
                    if (e.target.value) {
                        const date = new Date(e.target.value);
                        date.setHours(8, 30, 0, 0); // Set to 8:30 AM
                        handleChange('joiningDate', date);
                        setErrors(prev => ({...prev, joiningDate: undefined}));
                    } else {
                        handleChange('joiningDate', null);
                    }
                }}
                InputLabelProps={{
                    shrink: true,
                }}
                fullWidth
                margin="normal"
                error={!!errors.joiningDate}
                helperText={errors.joiningDate || "Time will be set to 08:30 AM"}
                disabled={saveLoading}
            />
        </Grid>
    ), [formData.joiningDate, errors.joiningDate, saveLoading]);

    const getEffectiveAdmins = () => {
        const backendAdmins = employee?.administratives || [];
        const addedAdmins = formData?.addedAdmins || [];
        const deletedAdmins = formData?.deletedAdmins || [];

        // 1. Filter out deleted admins from backend data
        const filteredBackendAdmins = backendAdmins.filter(
            admin => !deletedAdmins.includes(admin.userId)
        );

        // 2. Add newly added admins (if they aren't already in backend data)
        const newAdmins = addedAdmins
            .filter(userId =>
                !backendAdmins.some(admin => admin.userId === userId)
            )
            .map(userId => {
                // Try to find full user data (from paginatedAdmins or a lookup)
                const user = paginatedAdmins?.content?.find(u => u.userId === userId);
                return user || { userId, firstName: "New", lastName: "User" };
            });

        return [...filteredBackendAdmins, ...newAdmins];
    };
    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>{memoizedEmployee ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
            <DialogContent>
                <Grid container spacing={2} sx={{mt: 1}}>
                    {renderBasicInfoSection}
                    {renderJoiningDateSection}
                    {renderContactInfoSection}
                    {renderIdSection}
                    {renderPasswordSection}
                    {renderGenderSection}
                    {renderRolesSection}
                    {renderSectionsSection}
                    {renderProfilesSection}
                    {renderCheckboxesSection}
                    {renderAddressesSection}
                    {renderEmployeeAssignmentSection}
                </Grid>
                <EmployeeSelectionDialog
                    open={hodDialogOpen}
                    onClose={(result) => {
                        if (result?.selected) {  // Check if selection exists
                            setFormData(prev => ({
                                ...prev,
                                admins: result.selected.map(select=>select.userId),
                                addedAdmins: result.newlyAdded.map(add=>add.userId),
                                deletedAdmins: result.deleted.map(del=>del.userId)
                            }));
                        }
                        setIsFormDirty(true);
                        setHodDialogOpen(false);
                    }}
                    // employees={paginatedAdmins?.content?.map(user => ({
                    //     name: `${user.firstName} ${user.lastName}`,
                    //     ...user
                    // })) || []}
                    initialSelected={getEffectiveAdmins() || []}
                    title="Select Administrative"
                    // pagination={{
                    //     currentPage: currentAdminPage,
                    //     pageSize: adminPageSize,  // Now properly used
                    //     totalPages: paginatedAdmins?.totalPages || 1
                    // }}
                    // onPageChange={handlePageChange}
                    loading={isLoading}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={saveLoading}>
                    Cancel
                </Button>
                <Button
                    onClick={handleSave}
                    variant="contained"
                    disabled={!hasChanges || saveLoading}
                >
                    {saveLoading ? 'Saving...' : 'Save'}
                </Button>
            </DialogActions>
        </Dialog>
    );
});

export default EmployeeDialog;
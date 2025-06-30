'use client';

import React, { useState, useEffect, useMemo, useCallback } from "react";
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Container,
    CssBaseline,
    Dialog,
    FormControl,
    Grid,
    IconButton,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Typography,
} from "@mui/material";
import { Delete as DeleteIcon, Edit as EditIcon, MoreHoriz as MoreHorizIcon } from '@mui/icons-material';
import dynamic from 'next/dynamic';
import { debounce, isEqual } from 'lodash';
import DynamicDialog from '../DynamicDialog';
import EntityDialog from '../EntityDialog';
import SectionForm from '../SectionForm';
import ProfilesForm from '../ProfilesForm';
import AuthorityForm from '../AuthorityForm';
import RoleForm from '../RoleForm';
import { useDispatch, useSelector } from 'react-redux';
import {
    fetchManagementData,
    fetchPaginatedUsers,
    fetchPaginatedAdmins,
    setCurrentPage,
    saveEmployee,
    resetSaveStatus,
    deleteEmployee
} from '../../../../../../lib/redux/redux-lms/user/managementSlice';
import Menu from '@mui/material/Menu';
import { useRouter } from 'next/navigation';

const EmployeeDialog = dynamic(() => import('../EmployeeDialog'), {
    ssr: false,
    loading: () => <CircularProgress size={24} />
});

const ConfirmationDialog = dynamic(() => import('../../../ConfirmationDialog'), {
    ssr: false
});

const ManageEmployees = React.memo(() => {
    const dispatch = useDispatch();
    const {
        data: managementData,
        paginatedUsers,
        paginatedAdmins,
        loading: isLoading,
        error,
        currentPage,
        pageSize,
        currentAdminPage,
        adminPageSize,
        saveLoading,
        saveError,
        saveSuccess,
        deleteLoading,
        deleteError,
        deleteSuccess
    } = useSelector(state => state.management);

    const [openDialog, setOpenDialog] = useState(false);
    const [currentEmployee, setCurrentEmployee] = useState(null);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [selectedSections, setSelectedSections] = useState([]);
    const [selectedProfiles, setSelectedProfiles] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);
    const [openDialog_, setOpenDialog_] = useState(false);
    const [dialogType, setDialogType] = useState('');
    const [dialogOpen, setDialogOpen] = useState(false);
    const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
    const [employeeToDelete, setEmployeeToDelete] = useState(null);
    const [initialFormData, setInitialFormData] = useState(null);
    const [userId, setUserId] = useState(null);
    const router = useRouter();
    const [anchorEl, setAnchorEl] = React.useState(null);
    const open = Boolean(anchorEl);

    const handleMenuItemClick = (path) => {
        handleClose();
        router.push(path);
    };

    const handleClose = useCallback(() => {
        setAnchorEl(null);
    }, []);

    useEffect(() => {
        dispatch(fetchManagementData({ page: 0, limit: 0 }));
    }, [dispatch]);

    useEffect(() => {
        dispatch(fetchPaginatedUsers({ page: currentPage, limit: pageSize }));
    }, [currentPage, pageSize, dispatch]);

    useEffect(() => {
        dispatch(fetchPaginatedAdmins({ minPriority: 10, maxPriority: 99, page: currentAdminPage, limit: adminPageSize }));
    }, [currentAdminPage, adminPageSize, dispatch]);

    useEffect(() => {
        if (saveSuccess || deleteSuccess) {
            setSuccessOpen(true);
            dispatch(resetSaveStatus());
            dispatch(fetchManagementData({ page: 0, limit: 0 }));
            dispatch(fetchPaginatedUsers({ page: currentPage, limit: pageSize }));
            handleCloseDialog();
        }

        if (saveError || deleteError) {
            setErrorOpen(true);
        }
    }, [saveSuccess, saveError, deleteSuccess, deleteError, dispatch, currentPage, pageSize]);

    const filteredEmployees = useMemo(() => {
        if (!paginatedUsers?.content) return [];

        return paginatedUsers.content.filter((employee) => {
            const matchesRole = selectedRoles.length === 0 ||
                selectedRoles.some((role) => employee.roles?.some((r) => r.name === role));
            const matchesSection = selectedSections.length === 0 ||
                selectedSections.some((section) => employee.sections?.some((s) => s.section === section));
            const matchesProfile = selectedProfiles.length === 0 ||
                selectedProfiles.some((profile) => employee.profiles?.includes(profile));

            const matchesSearch =
                searchQuery.trim() === "" ||
                employee.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                employee.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                employee.roles?.some((role) => role.name?.toLowerCase().includes(searchQuery.toLowerCase())) ||
                employee.sections?.some((section) => section.section?.toLowerCase().includes(searchQuery.toLowerCase())) ||
                employee.profiles?.some((profile) => profile.name?.toLowerCase().includes(searchQuery.toLowerCase()));

            return matchesRole && matchesSection && matchesProfile && matchesSearch;
        });
    }, [paginatedUsers, selectedRoles, selectedSections, selectedProfiles, searchQuery]);

    const debouncedSearch = useMemo(
        () => debounce((query) => {
            setSearchQuery(query);
            dispatch(setCurrentPage(0));
        }, 300),
        [dispatch]
    );

    const handleSearchChange = (e) => {
        debouncedSearch(e.target.value);
    };

    useEffect(() => {
        return () => {
            debouncedSearch.cancel();
        };
    }, [debouncedSearch]);

    const handleOpenDialog_ = useCallback((type) => {
        setDialogType(type);
        setOpenDialog_(true);
    }, []);

    const handleCloseDialog_ = useCallback(() => {
        setOpenDialog_(false);
    }, []);

    const handleSubmit = useCallback((data) => {
        handleCloseDialog_();
    }, [handleCloseDialog_]);

    const handleSuccessOpen = useCallback(() => setSuccessOpen(true), []);
    const handleSuccessClose = useCallback(() => setSuccessOpen(false), []);
    const handleErrorOpen = useCallback(() => setErrorOpen(true), []);
    const handleErrorClose = useCallback(() => {
        setErrorOpen(false);
        dispatch(resetSaveStatus());
    }, [dispatch]);

    const renderForm = useCallback(() => {
        switch (dialogType) {
            case 'section': return <SectionForm onSubmit={handleSubmit}/>;
            case 'profile': return <ProfilesForm onSubmit={handleSubmit}/>;
            case 'authority': return <AuthorityForm onSubmit={handleSubmit}/>;
            case 'role': return <RoleForm onSubmit={handleSubmit}/>;
            default: return null;
        }
    }, [dialogType, handleSubmit]);

    const handleOpenDialog = useCallback((employee = null) => {
        setCurrentEmployee(employee);
        setInitialFormData(employee ? { ...employee } : null);
        setOpenDialog(true);
    }, []);

    const handleCloseDialog = useCallback(() => {
        setOpenDialog(false);
        setCurrentEmployee(null);
        setInitialFormData(null);
    }, []);

    const handleSaveEmployee = useCallback((employee) => {
        if (initialFormData && isEqual(initialFormData, employee)) {
            handleCloseDialog();
            return;
        }

        dispatch(saveEmployee({
            employee,
            isUpdate: !!employee.userId
        }));
    }, [dispatch, initialFormData, handleCloseDialog]);

    const handleClick = useCallback((event, id) => {
        setAnchorEl(event.currentTarget);
        setUserId(id)
    }, [userId]);

    const handleConfirmDelete = useCallback(() => {
        if (employeeToDelete) {
            dispatch(deleteEmployee(employeeToDelete));
        }
        setDeleteConfirmOpen(false);
    }, [dispatch, employeeToDelete]);

    const handleCancelDelete = useCallback(() => {
        setDeleteConfirmOpen(false);
        setEmployeeToDelete(null);
    }, []);

    const handleRefresh = useCallback(() => {
        dispatch(fetchManagementData({ page: 0, limit: 0 }));
        dispatch(fetchPaginatedUsers({ page: currentPage, limit: pageSize }));
    }, [dispatch, currentPage, pageSize]);

    const handleDialogOpen = useCallback(() => setDialogOpen(true), []);
    const handleDialogClose = useCallback(() => setDialogOpen(false), []);

    const items = useMemo(() => [
        { label: 'Add Employee', onClick: () => handleOpenDialog() },
        { label: 'Authority Section', onClick: () => handleOpenDialog_('authority') },
        { label: 'Section', onClick: () => handleOpenDialog_('section') },
        { label: 'Profiles Section', onClick: () => handleOpenDialog_('profile') },
        { label: 'Roles Section', onClick: () => handleOpenDialog_('role') },
    ], [handleOpenDialog, handleOpenDialog_]);

    return (
        <>
            {/*<SuccessDialog*/}
            {/*    open={successOpen}*/}
            {/*    onClose={handleSuccessClose}*/}
            {/*    title="Success!"*/}
            {/*    message="Your action was completed successfully."*/}
            {/*/>*/}

            {/*<ErrorDialog*/}
            {/*    open={errorOpen}*/}
            {/*    onClose={handleErrorClose}*/}
            {/*    title="Oops! Something Went Wrong"*/}
            {/*    message={saveError || deleteError || "There was an error processing your request. Please try again."}*/}
            {/*/>*/}

            <Menu
                id="demo-positioned-menu"
                aria-labelledby="demo-positioned-button"
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'left',
                }}
                transformOrigin={{
                    vertical: 'top',
                    horizontal: 'left',
                }}
            >
                <MenuItem onClick={()=> handleMenuItemClick(`/all-movements/${userId}`)}>Movements</MenuItem>
                <MenuItem onClick={()=> handleMenuItemClick(`/all-leaves/${userId}`)}>Leave</MenuItem>
                <MenuItem onClick={()=> handleMenuItemClick(`/single-employee-activities/${userId}`)}>Attendance</MenuItem>
                <MenuItem onClick={()=> handleMenuItemClick(`/in-outs/${userId}`)}>In-Outs</MenuItem>
            </Menu>

            <ConfirmationDialog
                open={deleteConfirmOpen}
                onClose={handleCancelDelete}
                onConfirm={handleConfirmDelete}
                title="Confirm Deletion"
                message="Are you sure you want to delete this employee? This action cannot be undone."
            />

            <DynamicDialog
                open={dialogOpen}
                onClose={handleDialogClose}
                title="Choose your option"
                items={items}
            />

            <EntityDialog
                open={openDialog_}
                onClose={handleCloseDialog_}
                title={`Add ${dialogType}`}
                onSubmit={handleSubmit}
            >
                {renderForm()}
            </EntityDialog>

            <Container>
                <CssBaseline/>
                <Box sx={{mt: 4}}>
                    <Typography variant="h4" gutterBottom>
                        Manage Employees
                    </Typography>

                    {isLoading && (
                        <Box sx={{display: "flex", justifyContent: "center", my: 4}}>
                            <CircularProgress/>
                            <Typography variant="body1" sx={{ml: 2}}>
                                Loading employees...
                            </Typography>
                        </Box>
                    )}

                    {error && (
                        <Alert severity="error" sx={{mb: 2}}>
                            {error}
                        </Alert>
                    )}

                    <Grid container spacing={2} justifyContent="flex-end" sx={{mb: 2}}>
                        <Grid item>
                            <Button
                                variant="contained"
                                onClick={handleRefresh}
                                sx={{mr: 2}}
                                disabled={isLoading}
                            >
                                <Typography component="span" sx={{fontSize: "30px", lineHeight: 1}}>
                                    ⟲
                                </Typography>
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                variant="contained"
                                onClick={handleDialogOpen}
                                sx={{mr: 2}}
                                disabled={isLoading}
                            >
                                <Typography component="span" sx={{fontSize: "30px", lineHeight: 1}}>
                                    +
                                </Typography>
                            </Button>
                        </Grid>
                    </Grid>

                    <TextField
                        label="Search"
                        variant="outlined"
                        fullWidth
                        defaultValue={searchQuery}
                        onChange={handleSearchChange}
                        sx={{mb: 2}}
                        disabled={isLoading}
                    />

                    <Box sx={{display: "flex", justifyContent: "space-between", mb: 2, gap: 2}}>
                        <FormControl fullWidth disabled={isLoading}>
                            <InputLabel>Roles</InputLabel>
                            <Select
                                multiple
                                value={selectedRoles}
                                onChange={(e) => setSelectedRoles(e.target.value)}
                                renderValue={(selected) => selected.join(", ")}
                            >
                                {managementData?.roleNames?.map((role) => (
                                    <MenuItem key={role} value={role}>
                                        {role}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <FormControl fullWidth disabled={isLoading}>
                            <InputLabel>Sections</InputLabel>
                            <Select
                                multiple
                                value={selectedSections}
                                onChange={(e) => setSelectedSections(e.target.value)}
                                renderValue={(selected) => selected.join(", ")}
                            >
                                {managementData?.sectionNames?.map((section) => (
                                    <MenuItem key={section} value={section}>
                                        {section}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <FormControl fullWidth disabled={isLoading}>
                            <InputLabel>Profiles</InputLabel>
                            <Select
                                multiple
                                value={selectedProfiles}
                                onChange={(e) => setSelectedProfiles(e.target.value)}
                                renderValue={(selected) => selected.join(", ")}
                            >
                                {managementData?.profileNames?.map((profile) => (
                                    <MenuItem key={profile} value={profile}>
                                        {profile}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Box>

                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>SLT ID</TableCell>
                                    <TableCell>PEOID</TableCell>
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
                                {filteredEmployees.length > 0 ? (
                                    filteredEmployees.map((employee) => (
                                        <TableRow key={employee.userId}>
                                            <TableCell>{employee.sltId}</TableCell>
                                            <TableCell>{employee.employeeId}</TableCell>
                                            <TableCell>{employee.firstName}</TableCell>
                                            <TableCell>{employee.lastName}</TableCell>
                                            <TableCell>{employee.email}</TableCell>
                                            <TableCell>{employee.roles?.map((role) => role.name).join(", ")}</TableCell>
                                            <TableCell>{employee.sections?.map((section) => section.section).join(", ")}</TableCell>
                                            <TableCell>{employee.profiles?.map((profile) => profile.name).join(", ")}</TableCell>
                                            <TableCell>
                                                <IconButton
                                                    onClick={() => handleOpenDialog(employee)}
                                                    disabled={isLoading || deleteLoading}
                                                >
                                                    <EditIcon/>
                                                </IconButton>
                                                <IconButton
                                                    onClick={(event) => handleClick(event, employee.userId)}
                                                >
                                                    <MoreHorizIcon/>
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={7} align="center">
                                            {isLoading ? 'Loading...' : 'No employees found'}
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    <Box sx={{display: "flex", justifyContent: "space-between", mt: 2}}>
                        <Button
                            variant="contained"
                            onClick={() => dispatch(setCurrentPage(Math.max(currentPage - 1, 0)))}
                            disabled={currentPage === 0 || isLoading}
                        >
                            Previous Page
                        </Button>
                        <Typography variant="body1">
                            Page {currentPage + 1} / {paginatedUsers?.totalPages || 1}
                        </Typography>
                        <Button
                            variant="contained"
                            onClick={() => dispatch(setCurrentPage(currentPage + 1))}
                            disabled={currentPage >= (paginatedUsers?.totalPages || 1) - 1 || isLoading}
                        >
                            Next Page
                        </Button>
                    </Box>
                    <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
                        <EmployeeDialog
                            open={openDialog}
                            onClose={handleCloseDialog}
                            onSave={handleSaveEmployee}
                            employee={currentEmployee}
                            roles={managementData?.roleNames || []}
                            sections={managementData?.sectionNames || []}
                            profiles={managementData?.profileNames || []}
                            saveLoading={saveLoading}
                            initialData={initialFormData}

                            paginatedAdmins={paginatedAdmins}
                            currentAdminPage={currentAdminPage}
                            adminPageSize={adminPageSize}
                            isLoading={isLoading}

                        />
                    </Dialog>
                </Box>
            </Container>
        </>
    );
});

ManageEmployees.displayName = 'ManageEmployees';

export default ManageEmployees;
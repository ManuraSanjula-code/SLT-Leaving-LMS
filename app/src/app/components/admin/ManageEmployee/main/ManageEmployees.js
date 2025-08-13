'use client';

import React, { useState, useEffect, useMemo, useCallback } from "react";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
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
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Tabs,
  Tab,
  Menu,
  TablePagination
} from "@mui/material";
import { 
  PersonAdd as PersonAddIcon,
  GroupAdd as GroupAddIcon,
  Category as CategoryIcon,
  Workspaces as WorkspacesIcon,
  Security as SecurityIcon,
  VerifiedUser as VerifiedUserIcon,
  Refresh as RefreshIcon,
  Edit as EditIcon,
  MoreHoriz as MoreHorizIcon,
} from '@mui/icons-material';
import dynamic from 'next/dynamic';
import { debounce, isEqual } from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/navigation';
import {
    fetchManagementData,
    fetchPaginatedUsers,
    setCurrentPage,
    setPageSize,
    saveEmployee,
    resetSaveStatus,
    deleteEmployee
} from '../../../../../../lib/redux/redux-lms/user/managementSlice';

const EmployeeDialog = dynamic(() => import('../EmployeeDialog'), {
  ssr: false,
  loading: () => <CircularProgress size={24} />
});

const ConfirmationDialog = dynamic(() => import('../../../ConfirmationDialog'), {
  ssr: false
});

const SectionForm = dynamic(() => import('../SectionForm'));
const ProfilesForm = dynamic(() => import('../ProfilesForm'));
const AuthorityForm = dynamic(() => import('../AuthorityForm'));
const RoleForm = dynamic(() => import('../RoleForm'));

const ManageEmployees = () => {
  const dispatch = useDispatch();
  const {
    data: managementData,
    paginatedUsers,
    loading: isLoading,
    error,
    currentPage,
    pageSize,
    saveLoading,
    saveError,
    saveSuccess,
    deleteLoading,
    deleteError,
    deleteSuccess
  } = useSelector(state => state.management);

  // State management
  const [openDialog, setOpenDialog] = useState(false);
  const [currentEmployee, setCurrentEmployee] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [selectedSections, setSelectedSections] = useState([]);
  const [selectedProfiles, setSelectedProfiles] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [employeeToDelete, setEmployeeToDelete] = useState(null);
  const [initialFormData, setInitialFormData] = useState(null);
  const [userId, setUserId] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  const [activeTab, setActiveTab] = useState(0);
  const [dialogState, setDialogState] = useState({
    open: false,
    type: ''
  });

  const open = Boolean(anchorEl);
  const router = useRouter();

  // Handlers
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleSnackbarClose = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  const showMessage = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleOpenMenu = (event, id) => {
    setAnchorEl(event.currentTarget);
    setUserId(id);
  };

  const handleCloseMenu = () => {
    setAnchorEl(null);
  };

  const handleMenuItemClick = (path) => {
    handleCloseMenu();
    router.push(path);
  };

  const handleOpenDialog = (employee = null) => {
    setCurrentEmployee(employee);
    setInitialFormData(employee ? { ...employee } : null);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setCurrentEmployee(null);
    setInitialFormData(null);
  };

  const handleOpenFormDialog = (type) => {
    setDialogState({ open: true, type });
  };

  const handleCloseFormDialog = () => {
    setDialogState({ open: false, type: '' });
  };

  const handleFormSubmit = () => {
    handleCloseFormDialog();
    dispatch(fetchManagementData({ page: 0, limit: 0 }));
  };

  const handleSaveEmployee = (employee) => {
    if (initialFormData && isEqual(initialFormData, employee)) {
      handleCloseDialog();
      return;
    }
    dispatch(saveEmployee({
      employee,
      isUpdate: !!employee.userId
    }));
  };

  const handleDeleteClick = (employeeId) => {
    setEmployeeToDelete(employeeId);
    setDeleteConfirmOpen(true);
  };

  const handleConfirmDelete = () => {
    if (employeeToDelete) {
      dispatch(deleteEmployee(employeeToDelete));
    }
    setDeleteConfirmOpen(false);
  };

  const handleCancelDelete = () => {
    setDeleteConfirmOpen(false);
    setEmployeeToDelete(null);
  };

  const handleRefresh = () => {
    dispatch(fetchManagementData({ page: 0, limit: 0 }));
    dispatch(fetchPaginatedUsers({ page: currentPage, limit: pageSize }));
  };

  const handleChangePage = (event, newPage) => {
    dispatch(setCurrentPage(newPage));
  };

  const handleChangeRowsPerPage = (event) => {
    const newSize = parseInt(event.target.value, 10);
    dispatch(setPageSize(newSize));
    dispatch(setCurrentPage(0));
  };

  useEffect(() => {
    dispatch(fetchManagementData({ page: 0, limit: 0 }));
  }, [dispatch]);

  useEffect(() => {
    dispatch(fetchPaginatedUsers({ page: currentPage, limit: pageSize }));
  }, [currentPage, pageSize, dispatch]);

  useEffect(() => {
    if (saveSuccess) {
      showMessage('Operation completed successfully!');
      dispatch(resetSaveStatus());
      handleCloseDialog();
    }

    if (deleteSuccess) {
      showMessage('Record deleted successfully!');
      dispatch(resetSaveStatus());
    }

    if (saveError) {
      showMessage(saveError || 'Operation failed', 'error');
      dispatch(resetSaveStatus());
    }

    if (deleteError) {
      showMessage(deleteError || 'Deletion failed', 'error');
      dispatch(resetSaveStatus());
    }
  }, [saveSuccess, saveError, deleteSuccess, deleteError, dispatch]);

  // Search debouncing
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
    return () => debouncedSearch.cancel();
  }, [debouncedSearch]);

  // Filter employees
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
        employee.sltId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        employee.employeeId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        employee.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        employee.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        employee.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        employee.roles?.some((role) => role.name?.toLowerCase().includes(searchQuery.toLowerCase())) ||
        employee.sections?.some((section) => section.section?.toLowerCase().includes(searchQuery.toLowerCase())) ||
        employee.profiles?.some((profile) => profile.name?.toLowerCase().includes(searchQuery.toLowerCase()));

      return matchesRole && matchesSection && matchesProfile && matchesSearch;
    }).sort((a, b) => {
      const dateA = a.join_date || 0;
      const dateB = b.join_date || 0;
      return dateB - dateA;
    });
  }, [paginatedUsers, selectedRoles, selectedSections, selectedProfiles, searchQuery]);

  return (
    <>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbar.severity}>
          {snackbar.message}
        </Alert>
      </Snackbar>

      <Container maxWidth="xl">
        <CssBaseline />
        <Box sx={{ mt: 4 }}>
          <Typography variant="h4" gutterBottom>
            Employee Management System
          </Typography>

          <Tabs value={activeTab} onChange={handleTabChange} sx={{ mb: 3 }}>
            <Tab label="Employees" icon={<PersonAddIcon />} />
            <Tab label="System Configuration" icon={<WorkspacesIcon />} />
          </Tabs>

          {activeTab === 0 && (
            <>
              <Grid container spacing={2} justifyContent="space-between" sx={{ mb: 2 }}>
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => handleOpenDialog()}
                    startIcon={<PersonAddIcon />}
                  >
                    Add Employee
                  </Button>
                </Grid>
                <Grid item>
                  <Button
                    variant="outlined"
                    onClick={handleRefresh}
                    startIcon={<RefreshIcon />}
                  >
                    Refresh
                  </Button>
                </Grid>
              </Grid>

              <TextField
                label="Search Employees"
                variant="outlined"
                fullWidth
                value={searchQuery}
                onChange={handleSearchChange}
                sx={{ mb: 2 }}
              />

              <Box sx={{ display: "flex", gap: 2, mb: 3 }}>
                <FormControl fullWidth>
                  <InputLabel>Filter by Role</InputLabel>
                  <Select
                    multiple
                    value={selectedRoles}
                    onChange={(e) => setSelectedRoles(e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                  >
                    {managementData?.roleNames?.map((role) => (
                      <MenuItem key={role} value={role}>{role}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel>Filter by Section</InputLabel>
                  <Select
                    multiple
                    value={selectedSections}
                    onChange={(e) => setSelectedSections(e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                  >
                    {managementData?.sectionNames?.map((section) => (
                      <MenuItem key={section} value={section}>{section}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel>Filter by Profile</InputLabel>
                  <Select
                    multiple
                    value={selectedProfiles}
                    onChange={(e) => setSelectedProfiles(e.target.value)}
                    renderValue={(selected) => selected.join(", ")}
                  >
                    {managementData?.profileNames?.map((profile) => (
                      <MenuItem key={profile} value={profile}>{profile}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>

              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>SLT ID</TableCell>
                      <TableCell>Employee ID</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Roles</TableCell>
                      <TableCell>Sections</TableCell>
                      <TableCell>Profiles</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredEmployees.length > 0 ? (
                      filteredEmployees.map((employee) => (
                        <TableRow key={employee.userId}>
                          <TableCell>{employee.sltId}</TableCell>
                          <TableCell>{employee.employeeId}</TableCell>
                          <TableCell>{`${employee.firstName} ${employee.lastName}`}</TableCell>
                          <TableCell>{employee.email}</TableCell>
                          <TableCell>
                            {employee.roles?.map(r => r.name).join(', ')}
                          </TableCell>
                          <TableCell>
                            {employee.sections?.map(s => s.section).join(', ')}
                          </TableCell>
                          <TableCell>
                            {employee.profiles?.map(p => p.name).join(', ')}
                          </TableCell>
                          <TableCell>
                            <IconButton onClick={() => handleOpenDialog(employee)}>
                              <EditIcon />
                            </IconButton>
                            <IconButton onClick={(e) => handleOpenMenu(e, employee.userId)}>
                              <MoreHorizIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={8} align="center">
                          {isLoading ? 'Loading...' : 'No employees found'}
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
                <TablePagination
                  rowsPerPageOptions={[5, 10, 25]}
                  component="div"
                  count={paginatedUsers?.totalElements || 0}
                  rowsPerPage={pageSize}
                  page={currentPage}
                  onPageChange={handleChangePage}
                  onRowsPerPageChange={handleChangeRowsPerPage}
                />
              </TableContainer>
            </>
          )}

          {activeTab === 1 && (
            <>
              <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>
                System Configuration
              </Typography>

              <Grid container spacing={3}>
                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <SecurityIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6" sx={{ mt: 1 }}>Roles</Typography>
                      <Button
                        variant="contained"
                        onClick={() => handleOpenFormDialog('role')}
                        sx={{ mt: 2 }}
                        fullWidth
                      >
                        Manage Roles
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <VerifiedUserIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6" sx={{ mt: 1 }}>Authorities</Typography>
                      <Button
                        variant="contained"
                        onClick={() => handleOpenFormDialog('authority')}
                        sx={{ mt: 2 }}
                        fullWidth
                      >
                        Manage Authorities
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <CategoryIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6" sx={{ mt: 1 }}>Sections</Typography>
                      <Button
                        variant="contained"
                        onClick={() => handleOpenFormDialog('section')}
                        sx={{ mt: 2 }}
                        fullWidth
                      >
                        Manage Sections
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <GroupAddIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6" sx={{ mt: 1 }}>Profiles</Typography>
                      <Button
                        variant="contained"
                        onClick={() => handleOpenFormDialog('profile')}
                        sx={{ mt: 2 }}
                        fullWidth
                      >
                        Manage Profiles
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </>
          )}
        </Box>
      </Container>

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
      />

      <Dialog
        open={dialogState.open}
        onClose={handleCloseFormDialog}
        maxWidth="md"
        fullWidth
      >
        <Box sx={{ p: 3 }}>
          <Typography variant="h5" gutterBottom>
            {`${dialogState.type.charAt(0).toUpperCase() + dialogState.type.slice(1)} Management`}
          </Typography>
          {dialogState.type === 'role' && <RoleForm onSubmit={handleFormSubmit} />}
          {dialogState.type === 'authority' && <AuthorityForm onSubmit={handleFormSubmit} />}
          {dialogState.type === 'section' && <SectionForm onSubmit={handleFormSubmit} />}
          {dialogState.type === 'profile' && <ProfilesForm onSubmit={handleFormSubmit} />}
        </Box>
      </Dialog>

      <ConfirmationDialog
        open={deleteConfirmOpen}
        onClose={handleCancelDelete}
        onConfirm={handleConfirmDelete}
        title="Confirm Deletion"
        message="Are you sure you want to delete this record?"
      />

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleCloseMenu}
      >
        <MenuItem onClick={() => handleMenuItemClick(`/all-movements/${userId}`)}>Movements</MenuItem>
        <MenuItem onClick={() => handleMenuItemClick(`/all-leaves/${userId}`)}>Leave</MenuItem>
        <MenuItem onClick={() => handleMenuItemClick(`/single-employee-activities/${userId}`)}>Attendance</MenuItem>
        <MenuItem onClick={() => handleMenuItemClick(`/in-outs/${userId}`)}>In-Outs</MenuItem>
        <MenuItem onClick={() => handleMenuItemClick(`/employee-edit/${userId}`)}>
          Edit Profile
        </MenuItem>
      </Menu>
    </>
  );
};

export default ManageEmployees;
// components/management/RoleForm.js
'use client';

import React, {useCallback, useEffect, useMemo, useState} from "react";
import {
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    InputAdornment,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Pagination,
    TextField,
    Typography,
} from "@mui/material";
import {Delete, Edit, Search} from "@mui/icons-material";
import {useDispatch, useSelector} from 'react-redux';
import {deleteRole, fetchManagementData, saveRole} from '../../../store/managementSlice';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const RoleForm = ({onSubmit}) => {
    const dispatch = useDispatch();
    const {data, loading, error, saveLoading, saveError, saveSuccess} = useSelector(state => state.management);

    const [openDialog, setOpenDialog] = useState(false);
    const [currentRole, setCurrentRole] = useState(null);
    const [formData, setFormData] = useState({name: "", priority: "", publicId: "", users: [], authorities: []});
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [selectedAuthorities, setSelectedAuthorities] = useState([]);
    const [deletedUsers, setDeletedUsers] = useState([]);
    const [deletedAuthorities, setDeletedAuthorities] = useState([]);
    const [userPage, setUserPage] = useState(1);
    const [authPage, setAuthPage] = useState(1);
    const [searchQuery, setSearchQuery] = useState("");
    const [formErrors, setFormErrors] = useState({});
    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);

    const usersPerPage = 10;

    useEffect(() => {
        dispatch(fetchManagementData());
    }, [dispatch]);

    useEffect(() => {
        if (saveSuccess) {
            setSuccessOpen(true);
            handleCloseDialog();
            dispatch(fetchManagementData());
        }
        if (saveError) {
            setErrorOpen(true);
        }
    }, [saveSuccess, saveError, dispatch]);

    const handleOpenDialog = useCallback((role = null) => {
        setCurrentRole(role);
        setFormData(role || {name: "", priority: 0, publicId: "", users: [], authorities: []});

        if (role) {
            const roleUserIds = role.users.map(user => user.userId);
            const roleAuthorityIds = role.authorities.map(auth => auth.id);
            setSelectedUsers(roleUserIds);
            setSelectedAuthorities(roleAuthorityIds);
            setDeletedUsers([]);
            setDeletedAuthorities([]);
        } else {
            setSelectedUsers([]);
            setSelectedAuthorities([]);
            setDeletedUsers([]);
            setDeletedAuthorities([]);
        }

        setOpenDialog(true);
    }, []);

    const handleCloseDialog = useCallback(() => {
        setOpenDialog(false);
        setCurrentRole(null);
        setFormData({name: "", priority: 0, publicId: "", users: [], authorities: []});
        setSelectedUsers([]);
        setSelectedAuthorities([]);
        setDeletedUsers([]);
        setDeletedAuthorities([]);
        setUserPage(1);
        setSearchQuery("");
    }, []);

    const handleChange = useCallback((e) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    }, []);

    const handleUserSelection = useCallback((userId) => {
        setSelectedUsers(prev =>
            prev.includes(userId)
                ? prev.filter(id => id !== userId)
                : [...prev, userId]
        );
    }, []);

    const handleAuthoritySelection = useCallback((authorityId) => {
        setSelectedAuthorities(prev =>
            prev.includes(authorityId)
                ? prev.filter(id => id !== authorityId)
                : [...prev, authorityId]
        );
    }, []);

    const validateForm = useCallback(() => {
        const errors = {name: '', authorities: '', priority: ''};
        let isValid = true;

        if (!formData.name.trim()) {
            errors.name = 'Name is required';
            isValid = false;
        } else if (formData.name.length < 3) {
            errors.name = 'Name must be at least 3 characters long';
            isValid = false;
        }
        if (formData.priority === 0) {
            errors.priority = 'Role must have priority';
            isValid = false;
        }
        if (selectedAuthorities.length === 0) {
            errors.authorities = 'Please select at least one authority';
            isValid = false;
        }

        setFormErrors(errors);
        return isValid;
    }, [formData.name, formData.priority, selectedAuthorities]);

    const handleSubmit = useCallback(async () => {
        if (!validateForm()) return;

        const roleData = {
            name: formData.name,
            deletedUsers,
            addedUsers: selectedUsers,
            deletedAuthorities,
            addedAuthorities: selectedAuthorities,
            priority: parseInt(formData.priority),
        };

        console.log("Submitting:", roleData);
        dispatch(saveRole({
            roleData,
            isUpdate: !!currentRole,
            roleId: formData.publicId,
        }));

        /*
         const roleData = {
            ...formData,
            users: selectedUsers,
            authorities: selectedAuthorities,
            priority: parseInt(formData.priority),
            deletedUsers,
            deletedAuthorities,
            isUpdate: !!currentRole
        };
        {
                name: updatedRole.name,
                deletedUsers,
                addedUsers: addedUsers.map(user => user.userId),
                deletedAuthorities,
                addedAuthorities,
            }
            dispatch(saveRole(roleData));
        * */
    }, [formData, selectedUsers, selectedAuthorities, deletedUsers, deletedAuthorities, currentRole, validateForm, dispatch]);

    const handleDelete = useCallback((id) => {
        dispatch(deleteRole(id));
    }, [dispatch]);

    const filteredUsers = useMemo(() => {
        if (!data?.users) return [];
        return data.users.filter(user =>
            user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.email?.toLowerCase().includes(searchQuery.toLowerCase())
        );
    }, [data?.users, searchQuery]);

    const paginatedUsers = useMemo(() =>
            filteredUsers.slice((userPage - 1) * usersPerPage, userPage * usersPerPage),
        [filteredUsers, userPage]);

    const paginatedAuthorities = useMemo(() => {
        if (!data?.authorities) return [];
        return data.authorities.slice((authPage - 1) * usersPerPage, authPage * usersPerPage);
    }, [data?.authorities, authPage]);

    const isFormDirty = useMemo(() => {
        if (!currentRole) return true;

        const isNameChanged = formData.name !== currentRole.name;
        const iPriorityChanged = formData.priority !== currentRole.priority;

        const areUsersChanged =
            selectedUsers.length !== currentRole.users.length ||
            !selectedUsers.every(userId => currentRole.users.some(user => user.userId === userId));
        const areAuthoritiesChanged =
            selectedAuthorities.length !== currentRole.authorities.length ||
            !selectedAuthorities.every(authId => currentRole.authorities.some(auth => auth.id === authId));

        return isNameChanged || areUsersChanged || areAuthoritiesChanged || iPriorityChanged;
    }, [currentRole, formData.name, formData.priority, selectedUsers, selectedAuthorities]);

    return (
        <>
            <SuccessDialog
                open={successOpen}
                onClose={() => setSuccessOpen(false)}
                title="Success!"
                message="Role saved successfully."
            />

            <ErrorDialog
                open={errorOpen}
                onClose={() => setErrorOpen(false)}
                title="Error"
                message={saveError || "Failed to save role"}
            />

            <div>
                <Button variant="contained" onClick={() => handleOpenDialog()}>
                    Add Role
                </Button>

                <List>
                    {data?.roles?.map(role => (
                        <ListItem key={role.id}>
                            <ListItemText
                                primary={role.name}
                                secondary={`Authorities: ${role.authorities?.map(auth => auth.name).join(", ")}`}
                            />
                            <IconButton onClick={() => handleOpenDialog(role)}>
                                <Edit/>
                            </IconButton>
                            <IconButton onClick={() => handleDelete(role.id)}>
                                <Delete/>
                            </IconButton>
                        </ListItem>
                    ))}
                </List>

                <Dialog open={openDialog} onClose={handleCloseDialog} fullWidth maxWidth="md">
                    <DialogTitle>{currentRole ? "Edit Role" : "Add Role"}</DialogTitle>
                    <DialogContent>
                        <Typography variant="h6" gutterBottom>
                            Role Details
                        </Typography>
                        <Box sx={{display: "flex", flexWrap: "wrap", gap: 2}}>
                            <TextField
                                label="Name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                                error={!!formErrors.name}
                                helperText={formErrors.name}
                            />
                            <TextField
                                readOnly
                                disabled
                                label="Public Id"
                                name="publicId"
                                value={formData.publicId || ''}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                            />
                            <TextField
                                label="Priority"
                                name="priority"
                                value={formData.priority}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                                error={!!formErrors.priority}
                                helperText={formErrors.priority}
                            />
                        </Box>

                        <Typography variant="h6" gutterBottom sx={{mt: 3}}>
                            Assigned Authorities
                        </Typography>
                        <Box sx={{maxHeight: "300px", overflowY: "auto"}}>
                            <List>
                                {paginatedAuthorities.map(authority => (
                                    <ListItem key={authority.id}>
                                        <ListItemIcon>
                                            <Checkbox
                                                checked={selectedAuthorities.includes(parseInt(authority.id))}
                                                onChange={() => handleAuthoritySelection(parseInt(authority.id))}
                                            />
                                        </ListItemIcon>
                                        <ListItemText primary={authority.name}/>
                                    </ListItem>
                                ))}
                            </List>
                            <Pagination
                                count={Math.ceil(data?.authorities?.length / usersPerPage) || 1}
                                page={authPage}
                                onChange={(e, value) => setAuthPage(value)}
                                sx={{mt: 2, display: "flex", justifyContent: "center"}}
                            />
                        </Box>

                        <Typography variant="h6" gutterBottom sx={{mt: 3}}>
                            Assigned Users
                        </Typography>
                        <TextField
                            label="Search Users"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            fullWidth
                            margin="normal"
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Search/>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Box sx={{maxHeight: "300px", overflowY: "auto"}}>
                            <List>
                                {paginatedUsers.map(user => (
                                    <ListItem key={user.userId}>
                                        <ListItemIcon>
                                            <Checkbox
                                                checked={selectedUsers.includes(user.userId)}
                                                onChange={() => handleUserSelection(user.userId)}
                                            />
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={`${user.firstName} ${user.lastName} (${user.email})`}
                                        />
                                    </ListItem>
                                ))}
                            </List>
                            <Pagination
                                count={Math.ceil(filteredUsers.length / usersPerPage)}
                                page={userPage}
                                onChange={(e, value) => setUserPage(value)}
                                sx={{mt: 2, display: "flex", justifyContent: "center"}}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Cancel</Button>
                        <Button
                            onClick={handleSubmit}
                            color="primary"
                            disabled={!isFormDirty || saveLoading}
                        >
                            {saveLoading ? 'Saving...' : currentRole ? "Update" : "Add"}
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        </>
    );
};

export default React.memo(RoleForm);
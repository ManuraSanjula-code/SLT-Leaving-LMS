'use client';

import React, { useCallback, useEffect, useMemo, useState } from "react";
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
import { Delete, Edit, Search } from "@mui/icons-material";
import { useDispatch, useSelector } from 'react-redux';
import { deleteRole, fetchManagementData, saveRole } from '../../../../../lib/redux/redux-lms/user/managementSlice';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const RoleForm = ({ onSubmit }) => {
    const dispatch = useDispatch();
    const {
        data,
        loading,
        error,
        saveLoading,
        saveError,
        saveSuccess,
        currentPage,
        pageSize
    } = useSelector(state => state.management);

    const [openDialog, setOpenDialog] = useState(false);
    const [currentRole, setCurrentRole] = useState(null);
    const [formData, setFormData] = useState({
        name: "",
        priority: "",
        publicId: "",
        users: [],
        authorities: []
    });
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [selectedAuthorities, setSelectedAuthorities] = useState([]);
    const [deletedUsers, setDeletedUsers] = useState([]);
    const [deletedAuthorities, setDeletedAuthorities] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [formErrors, setFormErrors] = useState({});
    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);

    useEffect(() => {
        dispatch(fetchManagementData({ usersPage: currentPage, usersSize: pageSize }));
    }, [dispatch, currentPage, pageSize]);

    useEffect(() => {
        if (saveSuccess) {
            setSuccessOpen(true);
            handleCloseDialog();
            dispatch(fetchManagementData({ usersPage: currentPage, usersSize: pageSize }));
        }
        if (saveError) {
            setErrorOpen(true);
        }
    }, [saveSuccess, saveError, dispatch, currentPage, pageSize, handleCloseDialog]);

    const handlePageChange = (event, newPage) => {
        dispatch(fetchManagementData({
            usersPage: newPage - 1,
            usersSize: pageSize
        }));
    };

    const handleOpenDialog = useCallback((role = null) => {
        setCurrentRole(role);
        setFormData(role || {
            name: "",
            priority: 0,
            publicId: "",
            users: [],
            authorities: []
        });

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
        setFormData({
            name: "",
            priority: 0,
            publicId: "",
            users: [],
            authorities: []
        });
        setSelectedUsers([]);
        setSelectedAuthorities([]);
        setDeletedUsers([]);
        setDeletedAuthorities([]);
        setSearchQuery("");
    }, []);

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    }, []);

    const handleAuthoritySelection = useCallback((authorityId) => {
        setSelectedAuthorities(prev => {
            const newSelected = prev.includes(authorityId)
                ? prev.filter(id => id !== authorityId)
                : [...prev, authorityId];

            // If this is an edit of an existing role
            if (currentRole) {
                setDeletedAuthorities(prevDeleted => {
                    // If authority was originally in the role and is now being removed
                    const wasInOriginal = currentRole.authorities.some(a => a.id === authorityId);
                    if (wasInOriginal && !newSelected.includes(authorityId)) {
                        return [...prevDeleted, authorityId];
                    }
                    // If authority was previously marked for deletion but is now being re-added
                    if (prevDeleted.includes(authorityId) && newSelected.includes(authorityId)) {
                        return prevDeleted.filter(id => id !== authorityId);
                    }
                    return prevDeleted;
                });
            }
            return newSelected;
        });
    }, [currentRole]);

    const handleUserSelection = useCallback((userId) => {
        setSelectedUsers(prev => {
            const newSelected = prev.includes(userId)
                ? prev.filter(id => id !== userId)
                : [...prev, userId];

            // If this is an edit of an existing role
            if (currentRole) {
                setDeletedUsers(prevDeleted => {
                    // If user was originally in the role and is now being removed
                    const wasInOriginal = currentRole.users.some(u => u.userId === userId);
                    if (wasInOriginal && !newSelected.includes(userId)) {
                        return [...prevDeleted, userId];
                    }
                    // If user was previously marked for deletion but is now being re-added
                    if (prevDeleted.includes(userId) && newSelected.includes(userId)) {
                        return prevDeleted.filter(id => id !== userId);
                    }
                    return prevDeleted;
                });
            }
            return newSelected;
        });
    }, [currentRole]);

    const validateForm = useCallback(() => {
        const errors = { name: '', authorities: '', priority: '' };
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

        const addedUsers = selectedUsers
            .filter(userId => !currentRole?.users.some(user => user.userId === userId))
            .map(userId => data.users.content.find(user => user.userId === userId));

        const addedAuthorities = selectedAuthorities
            .filter(authId => !currentRole?.authorities.some(auth => auth.id === authId));

        const roleData = {
            name: formData.name,
            priority: parseInt(formData.priority),
            publicId: formData.publicId,
            deletedUsers,
            addedUsers: addedUsers.map(user => user.userId),
            deletedAuthorities,
            addedAuthorities,
        };

        dispatch(saveRole({
            roleData,
            isUpdate: !!currentRole,
            roleId: currentRole?.id
        }));
    }, [
        formData,
        selectedUsers,
        selectedAuthorities,
        deletedUsers,
        deletedAuthorities,
        currentRole,
        validateForm,
        dispatch,
        data?.users?.content
    ]);

    const handleDelete = useCallback((id) => {
        dispatch(deleteRole(id));
    }, [dispatch]);

    const filteredUsers = useMemo(() => {
        if (!data?.users?.content) return [];
        return data.users.content.filter(user =>
            user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.email?.toLowerCase().includes(searchQuery.toLowerCase())
        );
    }, [data?.users?.content, searchQuery]);

    const isFormDirty = useMemo(() => {
        if (!currentRole) return true;

        const isNameChanged = formData.name !== currentRole.name;
        const isPriorityChanged = formData.priority !== currentRole.priority;

        const areUsersChanged =
            selectedUsers.length !== currentRole.users.length ||
            !selectedUsers.every(userId => currentRole.users.some(user => user.userId === userId));
        const areAuthoritiesChanged =
            selectedAuthorities.length !== currentRole.authorities.length ||
            !selectedAuthorities.every(authId => currentRole.authorities.some(auth => auth.id === authId));

        return isNameChanged || areUsersChanged || areAuthoritiesChanged || isPriorityChanged;
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
                                secondary={`Priority: ${role.priority} | Authorities: ${role.authorities?.map(auth => auth.name).join(", ")}`}
                            />
                            <IconButton onClick={() => handleOpenDialog(role)}>
                                <Edit />
                            </IconButton>
                            <IconButton onClick={() => handleDelete(role.id)}>
                                <Delete />
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
                        <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2 }}>
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
                                disabled
                                label="Public ID"
                                name="publicId"
                                value={formData.publicId || ''}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                            />
                            <TextField
                                label="Priority"
                                name="priority"
                                type="number"
                                value={formData.priority}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                                error={!!formErrors.priority}
                                helperText={formErrors.priority}
                            />
                        </Box>

                        <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                            Assigned Authorities
                            {formErrors.authorities && (
                                <Typography color="error" variant="body2">
                                    {formErrors.authorities}
                                </Typography>
                            )}
                        </Typography>
                        <Box sx={{ maxHeight: "300px", overflowY: "auto" }}>
                            <List>
                                {data?.authorities?.map(authority => (
                                    <ListItem key={authority.id}>
                                        <ListItemIcon>
                                            <Checkbox
                                                checked={selectedAuthorities.includes(parseInt(authority.id))}
                                                onChange={() => handleAuthoritySelection(parseInt(authority.id))}
                                            />
                                        </ListItemIcon>
                                        <ListItemText primary={authority.name} />
                                    </ListItem>
                                ))}
                            </List>
                        </Box>

                        <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
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
                                        <Search />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Box sx={{ maxHeight: "300px", overflowY: "auto" }}>
                            <List>
                                {filteredUsers.map(user => (
                                    <ListItem key={user.userId}>
                                        <ListItemIcon>
                                            <Checkbox
                                                checked={selectedUsers.includes(user.userId)}
                                                onChange={() => handleUserSelection(user.userId)}
                                            />
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={`${user.firstName} ${user.lastName}`}
                                            secondary={user.email}
                                        />
                                    </ListItem>
                                ))}
                            </List>
                            <Pagination
                                count={data?.users?.totalPages || 1}
                                page={(data?.users?.pageable?.pageNumber || 0) + 1}
                                onChange={handlePageChange}
                                sx={{ mt: 2, display: "flex", justifyContent: "center" }}
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
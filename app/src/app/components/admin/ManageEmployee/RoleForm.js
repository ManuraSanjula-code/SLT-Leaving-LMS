import React, { useState, useEffect, useRef } from "react";
import {
    TextField,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    List,
    ListItem,
    ListItemText,
    IconButton,
    Checkbox,
    ListItemIcon,
    Typography,
    Pagination,
    Box,
    InputAdornment,
} from "@mui/material";
import { Delete, Edit, Search } from "@mui/icons-material";
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const RoleForm = () => {
    const [roles, setRoles] = useState([]);
    const [users, setUsers] = useState([]); // All users from the server
    const [openDialog, setOpenDialog] = useState(false);
    const [currentRole, setCurrentRole] = useState(null);
    const [formData, setFormData] = useState({ name: "", users: [], authorities: [] });
    const [selectedUsers, setSelectedUsers] = useState([]); // Users selected for the role
    const [selectedAuthorities, setSelectedAuthorities] = useState([]); // Authorities selected for the role
    const [deletedUsers, setDeletedUsers] = useState([]); // Users to be removed from the role
    const [deletedAuthorities, setDeletedAuthorities] = useState([]); // Authorities to be removed from the role
    const [newUserId, setNewUserId] = useState(""); // Input for adding a new user by userId
    const [newAuthorityName, setNewAuthorityName] = useState(""); // Input for adding a new authority by name
    const [userPage, setUserPage] = useState(1); // Pagination for assigned users
    const [authPage, setAuthPage] = useState(1); // Pagination for assigned authorities
    const [searchQuery, setSearchQuery] = useState(""); // Search query for filtering users
    const usersPerPage = 10; // Number of users to display per page
    const [formErrors, setFormErrors] = useState({});
    const [authorities, setAuthorities] = useState([]);
    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);

    const resolveRef = useRef(null); // To store the resolve function

    const handleSuccessOpen = () => {
        setSuccessOpen(true);
    };

    // Fetch roles, users, and authorities from the server on component mount
    useEffect(() => {
        // Fetch roles
        fetch("http://localhost:8080/users/roles")
            .then((response) => response.json())
            .then((data) => {
                setRoles(data || []);
            })
            .catch((err) => console.error("Error fetching roles:", err));

        // Fetch users
        fetch("http://localhost:8080/users")
            .then((response) => response.json())
            .then((data) => setUsers(data || []))
            .catch((err) => console.error("Error fetching users:", err));

        // Fetch authorities
        fetch('http://localhost:8080/users/authorities')
            .then((response) => response.json())
            .then((data) => {
                const transformedData = data.map((authority) => ({
                    id: authority.ID,
                    name: authority.name,
                }));
                setAuthorities(transformedData || []);
            })
            .catch((err) => {
                console.error(err);
            });
    }, []);

    // Open dialog for adding or editing a role
    const handleOpenDialog = (role = null) => {
        setCurrentRole(role);
        setFormData(role || { name: "", users: [], authorities: [] });

        // Initialize selectedUsers and selectedAuthorities based on the current role's data
        if (role) {
            const roleUserIds = role.users.map((user) => user.userId); // Use userId
            const roleAuthorityIds = role.authorities.map((authority) => authority.id);

            setSelectedUsers(roleUserIds);
            setSelectedAuthorities(roleAuthorityIds);

            // Ensure formData.authorities contains the correct authority objects
            const roleAuthorities = role.authorities.map((authority) => ({
                id: authority.id,
                name: authority.name,
            }));
            setFormData((prev) => ({
                ...prev,
                authorities: roleAuthorities,
            }));

            setDeletedUsers([]); // Reset deletedUsers when opening the dialog
            setDeletedAuthorities([]); // Reset deletedAuthorities when opening the dialog
        } else {
            setSelectedUsers([]);
            setSelectedAuthorities([]);
            setDeletedUsers([]);
            setDeletedAuthorities([]);
        }

        setOpenDialog(true);
    };

    // Use useEffect to synchronize selectedUsers and selectedAuthorities
    useEffect(() => {
        if (currentRole) {
            const roleUserIds = currentRole.users.map((user) => user.userId); // Use userId
            const roleAuthorityIds = currentRole.authorities.map((authority) => authority.id);

            setSelectedUsers(roleUserIds);
            setSelectedAuthorities(roleAuthorityIds);
        }
    }, [currentRole]);

    useEffect(()=>{}, [selectedAuthorities])

    // Close dialog
    const handleCloseDialog = () => {
        setOpenDialog(false);
        setCurrentRole(null);
        setFormData({ name: "", users: [], authorities: [] });
        setSelectedUsers([]);
        setSelectedAuthorities([]);
        setDeletedUsers([]);
        setDeletedAuthorities([]);
        setNewUserId("");
        setNewAuthorityName("");
        setUserPage(1); // Reset pagination
        setSearchQuery(""); // Reset search query
    };

    // Handle form input changes
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    // Handle user selection for the role
    const handleUserSelection = (userId) => {
        if (selectedUsers.includes(userId)) {
            // User is being unchecked, add to deletedUsers
            setSelectedUsers((prev) => prev.filter((id) => id !== userId));
            setDeletedUsers((prev) => [...prev, userId]);
        } else {
            // User is being checked, remove from deletedUsers if present
            setSelectedUsers((prev) => [...prev, userId]);
            setDeletedUsers((prev) => prev.filter((id) => id !== userId));
        }
    };

    // Handle authority selection for the role
    const handleAuthoritySelection = (authorityId) => {
        if (selectedAuthorities.includes(authorityId)) {
            // Authority is being unchecked, add to deletedAuthorities
            setSelectedAuthorities((prev) => prev.filter((id) => id !== authorityId));
            setDeletedAuthorities((prev) => [...prev, authorityId]);
        } else {
            // Authority is being checked, remove from deletedAuthorities if present
            setSelectedAuthorities((prev) => [...prev, authorityId]);
            setDeletedAuthorities((prev) => prev.filter((id) => id !== authorityId));
        }
    };

    const handleAuthoritySelection_ = (authorityId) => {
        console.log("Authority ID Selected:", authorityId); // Debugging log
        if (selectedAuthorities.includes(Number(authorityId))) {
            // Authority is being unchecked, remove from selectedAuthorities
            setSelectedAuthorities((prev) => prev.filter((id) => id !== Number(authorityId)));
            setDeletedAuthorities((prev) => [...prev, authorityId]);
        } else {
            // Authority is being checked, add to selectedAuthorities
            setSelectedAuthorities((prev) => [...prev, Number(authorityId)]);
            setDeletedAuthorities((prev) => prev.filter((id) => id !== authorityId));
        }
    };

    // Validate form data
    const validateForm = () => {
        let isValid = true;
        const errors = { name: '', users: '', authorities: '' };

        if (!formData.name.trim()) {
            errors.name = 'Name is required';
            isValid = false;
        } else if (formData.name.length < 3) {
            errors.name = 'Name must be at least 3 characters long';
            isValid = false;
        }

        if (selectedAuthorities.length === 0) {
            errors.authorities = 'Please select at least one authority';
            isValid = false;
        }

        setFormErrors(errors);
        return isValid;
    };

    const handleSubmit = () => {
        if (!validateForm()) {
            return; // Prevent submission if validation fails
        }

        const updatedRole = {
            ...formData,
            users: selectedUsers.map((userId) => users.find((user) => user.userId === userId)), // Use userId
            authorities: selectedAuthorities,
            deletedUsers,
            deletedAuthorities,
        };

        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const sendRequest = async (url, method, body) => {
            console.log(body)
            try {
                const response = await fetch(url, {
                    method,
                    headers: { "Content-Type": "application/json" },
                    credentials: 'include',
                    body: JSON.stringify(body),
                });
                const data = await response.json();
                return data;
            } catch (err) {
                await showErrorDialog();
                console.error("Error updating role:", err);
                throw err;
            }
        };

        if (currentRole) {
            // Update existing role
            const addedUsers = selectedUsers
                .filter((userId) => !currentRole.users.some((user) => user.userId === userId)) // Use userId
                .map((userId) => users.find((user) => user.userId === userId));

            const addedAuthorities = selectedAuthorities;

            sendRequest(`http://localhost:8080/users/roles/${userId}`, "POST", {
                name: updatedRole.name,
                deletedUsers,
                addedUsers: addedUsers.map((user) => user.userId), // Use userId
                deletedAuthorities,
                addedAuthorities,
            })
                .then(async () => {
                    setRoles(
                        roles.map((role) =>
                            role.id === currentRole.id ? { ...role, ...updatedRole } : role
                        )
                    );
                    await showSuccessDialog();
                });
        } else {
            // Add new role
            const newRole = {
                id: Date.now(), // Use a unique ID
                ...updatedRole,
                addedUsers: selectedUsers.map((userId) => users.find((user) => user.userId === userId)), // Include new users
                addedAuthorities: selectedAuthorities
            };

            sendRequest(`http://localhost:8080/users/roles/${userId}`, "POST", {
                name: newRole.name,
                addedUsers: newRole.addedUsers.map((user) => user.userId), // Use userId
                addedAuthorities: newRole.addedAuthorities,
            })
                .then(async (response) => {
                    setRoles([...roles, newRole]);
                    await showSuccessDialog();
                });
        }

        handleCloseDialog();
    };

    // Handle deleting a role
    const handleDelete = (id) => {
        const userId = localStorage.getItem('userId');
        if (!userId) return;

        fetch(`http://localhost:8080/users/delete/role/${id}/${userId}`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            credentials: 'include',
        })
            .then((response) => response.json())
            .then(async (data) => {
                setRoles(roles.filter((role) => role.id !== id));
            })
            .catch(async (err) => {
                console.error("Error updating role:", err);
            });
    };

    // Get users assigned to the current role
    const getAssignedUsers = () => {
        if (!currentRole) return [];
        return currentRole.users || [];
    };

    // Get authorities assigned to the current role
    const getAssignedAuthorities = () => {
        if (!currentRole) return [];
        return currentRole.authorities || [];
    };

    // Filter users based on search query
    const filteredUsers = users.filter(
        (user) =>
            user.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            user.email.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Pagination logic for assigned users
    const handleUserPageChange = (event, value) => {
        setUserPage(value);
    };

    const handleAuthPageChange = (event, value) => {
        setAuthPage(value);
    };

    // Reset pagination to page 1 when search query changes
    useEffect(() => {
        setUserPage(1);
    }, [searchQuery]);

    const paginatedUsers = filteredUsers.slice(
        (userPage - 1) * usersPerPage,
        userPage * usersPerPage
    );

    const paginatedAuthorities = authorities.slice(
        (authPage - 1) * usersPerPage,
        authPage * usersPerPage
    );

    const handleSuccessClose = () => {
        setSuccessOpen(false);
        if (resolveRef.current) {
            resolveRef.current(); // Resolve the Promise
            resolveRef.current = null; // Clear the ref
        }
    };

    const handleErrorOpen = () => {
        setErrorOpen(true);
    };

    const handleErrorClose = () => {
        setErrorOpen(false);
        if (resolveRef.current) {
            resolveRef.current(); // Resolve the Promise
            resolveRef.current = null; // Clear the ref
        }
    };

    const showSuccessDialog = () => {
        return new Promise((resolve) => {
            resolveRef.current = resolve; // Store the resolve function
            handleSuccessOpen(); // Open the dialog
        });
    };

    const showErrorDialog = () => {
        return new Promise((resolve) => {
            resolveRef.current = resolve; // Store the resolve function
            handleErrorOpen(); // Open the dialog
        });
    };

    return (
        <>
            <SuccessDialog
                open={successOpen}
                onClose={handleSuccessClose}
                title="Success!"
                message="Your action was completed successfully."
            />

            <ErrorDialog
                open={errorOpen}
                onClose={handleErrorClose}
                title="Oops! Something Went Wrong"
                message="There was an error processing your request. Please try again."
            />

            <div>
                <Button variant="contained" onClick={() => handleOpenDialog()}>
                    Add Role
                </Button>

                <List>
                    {roles.map((role) => (
                        <ListItem key={role.id}>
                            <ListItemText
                                primary={role.name}
                                secondary={`Authorities: ${role.authorities
                                    .map((authority) => authority.name)
                                    .join(", ")}`}
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
                        {/* Role Details Section */}
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
                        </Box>

                        {/* Assigned Authorities Section */}
                        <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                            Assigned Authorities
                        </Typography>
                        <Box sx={{ maxHeight: "300px", overflowY: "auto" }}>
                            <List>
                                {paginatedAuthorities.map((authority) => {
                                    return (
                                        <ListItem key={authority.id}>
                                            <ListItemIcon>
                                                <Checkbox
                                                    checked={selectedAuthorities.includes(Number(authority.id))}
                                                    onChange={() => handleAuthoritySelection_(authority.id)}
                                                />
                                            </ListItemIcon>
                                            <ListItemText primary={authority.name} />
                                        </ListItem>
                                    );
                                })}
                            </List>
                            <Pagination
                                count={Math.ceil(authorities.length / usersPerPage)}
                                page={authPage}
                                onChange={handleAuthPageChange}
                                sx={{ mt: 2, display: "flex", justifyContent: "center" }}
                            />
                        </Box>

                        {/* Assigned Users Section */}
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
                                {paginatedUsers.map((user) => (
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
                                onChange={handleUserPageChange}
                                sx={{ mt: 2, display: "flex", justifyContent: "center" }}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Cancel</Button>
                        <Button onClick={handleSubmit} color="primary">
                            {currentRole ? "Update" : "Add"}
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        </>
    );
};

export default RoleForm;
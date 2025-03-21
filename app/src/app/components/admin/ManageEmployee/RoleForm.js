import React, {useState, useEffect, useRef} from "react";
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
import {Delete, Edit, Search} from "@mui/icons-material";
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

// Function to generate a large number of dummy users
const generateDummyUsers = (count) => {
    const users = [];
    for (let i = 1; i <= count; i++) {
        users.push({
            userId: `user${i}`, // Use userId instead of id
            firstName: `User${i}`,
            lastName: `Last${i}`,
            email: `user${i}@example.com`,
        });
    }
    return users;
};

const RoleForm = () => {
    const [roles, setRoles] = useState([]);
    const [users, setUsers] = useState([]); // All users from the server
    const [openDialog, setOpenDialog] = useState(false);
    const [currentRole, setCurrentRole] = useState(null);
    const [formData, setFormData] = useState({name: "", users: [], authorities: []});
    const [selectedUsers, setSelectedUsers] = useState([]); // Users selected for the role
    const [selectedAuthorities, setSelectedAuthorities] = useState([]); // Authorities selected for the role
    const [deletedUsers, setDeletedUsers] = useState([]); // Users to be removed from the role
    const [deletedAuthorities, setDeletedAuthorities] = useState([]); // Authorities to be removed from the role
    const [newUserId, setNewUserId] = useState(""); // Input for adding a new user by userId
    const [newAuthorityName, setNewAuthorityName] = useState(""); // Input for adding a new authority by name
    const [userPage, setUserPage] = useState(1); // Pagination for assigned users
    const [searchQuery, setSearchQuery] = useState(""); // Search query for filtering users
    const usersPerPage = 10; // Number of users to display per page

    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);

    const resolveRef = useRef(null); // To store the resolve function

    const handleSuccessOpen = () => {
        setSuccessOpen(true);
    };

    // Fetch roles and users from the server on component mount
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
    }, []);

    // Open dialog for adding or editing a role
    const handleOpenDialog = (role = null) => {
        setCurrentRole(role);
        setFormData(role || {name: "", users: [], authorities: []});

        // Initialize selectedUsers and selectedAuthorities based on the current role's data
        if (role) {
            const roleUserIds = role.users.map((user) => user.userId); // Use userId
            const roleAuthorityIds = role.authorities.map((authority) => authority.id);
            setSelectedUsers(roleUserIds);
            setSelectedAuthorities(roleAuthorityIds);
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

    // Close dialog
    const handleCloseDialog = () => {
        setOpenDialog(false);
        setCurrentRole(null);
        setFormData({name: "", users: [], authorities: []});
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
        const {name, value} = e.target;
        setFormData({...formData, [name]: value});
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

    // Handle adding a new user by userId
    const handleAddNewUser = () => {
        const userToAdd = users.find((user) => user.userId === newUserId);
        if (userToAdd) {
            if (!selectedUsers.includes(userToAdd.userId)) {
                setSelectedUsers((prev) => [...prev, userToAdd.userId]);
                setNewUserId(""); // Clear input after adding
            } else {
                alert("User is already added to this role.");
            }
        } else {
            alert("User not found.");
        }
    };

    // Handle adding a new authority by name
    const handleAddNewAuthority = () => {
        if (newAuthorityName.trim()) {

            fetch(`http://localhost:8080/users/check/auth/${newAuthorityName}`)
                .then((response) => response.json())
                .then(async (result) => {
                    console.log(result)
                    if (result) {
                        // await showErrorDialog();
                        await showSuccessDialog()
                        const newAuthority = {
                            id: Date.now(),
                            name: newAuthorityName.trim(),
                        };
                        setSelectedAuthorities((prev) => [...prev, newAuthority.id]);
                        setFormData((prev) => ({
                            ...prev,
                            authorities: [...prev.authorities, newAuthority],
                        }));

                    } else {
                        await showErrorDialog();
                        setNewAuthorityName("");
                    }

                })
                .catch(async (error) => {
                    await showErrorDialog();
                });

        } else {
            alert("Authority name cannot be empty.");
        }
    };

    // Handle form submission (add or update role)
    const handleSubmit_ = () => {
        const updatedRole = {
            ...formData,
            users: selectedUsers.map((userId) => users.find((user) => user.userId === userId)), // Use userId
            authorities: selectedAuthorities.map((id) =>
                formData.authorities.find((authority) => authority.id === id)
            ),
            deletedUsers,
            deletedAuthorities,
        };

        if (currentRole) {
            // Update existing role
            const addedUsers = selectedUsers
                .filter((userId) => !currentRole.users.some((user) => user.userId === userId)) // Use userId
                .map((userId) => users.find((user) => user.userId === userId));

            const addedAuthorities = selectedAuthorities
                .filter((id) => !currentRole.authorities.some((authority) => authority.id === id))
                .map((id) => formData.authorities.find((authority) => authority.id === id));

            const userId = localStorage.getItem('userId');
            if (!userId) return;

            // Send PUT request to update the role
            fetch(`http://localhost:8080/users/roles/${userId}`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                credentials: 'include',
                body: JSON.stringify({
                    name: updatedRole.name,
                    deletedUsers,
                    addedUsers: addedUsers.map((user) => user.userId), // Use userId
                    deletedAuthorities,
                    addedAuthorities: addedAuthorities.map((authority) => authority.name),
                }),
            })
                .then((response) => response.json())
                .then(async (data) => {
                    setRoles(
                        roles.map((role) =>
                            role.id === currentRole.id ? {...role, ...updatedRole} : role
                        )
                    );
                    await showSuccessDialog()
                })
                .catch(async (err) => {
                    await showErrorDialog();
                    console.error("Error updating role:", err)
                });
        } else {
            // Add new role
            const newRole = {
                id: Date.now(), // Use a unique ID
                ...updatedRole,
            };
            setRoles([...roles, newRole]);

        }
        handleCloseDialog();
    };

    const handleSubmit = () => {
        const updatedRole = {
            ...formData,
            users: selectedUsers.map((userId) => users.find((user) => user.userId === userId)), // Use userId
            authorities: selectedAuthorities.map((id) =>
                formData.authorities.find((authority) => authority.id === id)
            ),
            deletedUsers,
            deletedAuthorities,
        };

        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const sendRequest = async (url, method, body) => {
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

            const addedAuthorities = selectedAuthorities
                .filter((id) => !currentRole.authorities.some((authority) => authority.id === id))
                .map((id) => formData.authorities.find((authority) => authority.id === id));

            sendRequest(`http://localhost:8080/users/roles/${userId}`, "POST", {
                name: updatedRole.name,
                deletedUsers,
                addedUsers: addedUsers.map((user) => user.userId), // Use userId
                deletedAuthorities,
                addedAuthorities: addedAuthorities.map((authority) => authority.name),
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
                addedAuthorities: selectedAuthorities.map((id) =>
                    formData.authorities.find((authority) => authority.id === id)
                ),
            };

            sendRequest(`http://localhost:8080/users/roles/${userId}`, "POST", {
                name: newRole.name,
                addedUsers: newRole.addedUsers.map((user) => user.userId), // Use userId
                addedAuthorities: newRole.addedAuthorities.map((authority) => authority.name),
            })
                .then(async() => {
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
            headers: {"Content-Type": "application/json"},
            credentials: 'include',
        })
            .then((response) => response.json())
            .then(async (data) => {
                setRoles(roles.filter((role) => role.id !== id));
            })
            .catch(async (err) => {
                console.error("Error updating role:", err)
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

    // Reset pagination to page 1 when search query changes
    useEffect(() => {
        setUserPage(1);
    }, [searchQuery]);

    const paginatedUsers = filteredUsers.slice(
        (userPage - 1) * usersPerPage,
        userPage * usersPerPage
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

            {/* Error Dialog */}
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
                        {/* Role Details Section */}
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
                            />
                        </Box>

                        {/* Assigned Authorities Section */}
                        <Typography variant="h6" gutterBottom sx={{mt: 3}}>
                            Assigned Authorities
                        </Typography>
                        <List>
                            {getAssignedAuthorities().map((authority) => (
                                <ListItem key={authority.id}>
                                    <ListItemIcon>
                                        <Checkbox
                                            checked={selectedAuthorities.includes(authority.id)}
                                            onChange={() => handleAuthoritySelection(authority.id)}
                                        />
                                    </ListItemIcon>
                                    <ListItemText primary={authority.name}/>
                                </ListItem>
                            ))}
                        </List>
                        <Box sx={{mt: 2}}>
                            <TextField
                                label="Add New Authority"
                                value={newAuthorityName}
                                onChange={(e) => setNewAuthorityName(e.target.value)}
                                fullWidth
                                margin="normal"
                            />
                            <Button onClick={handleAddNewAuthority} color="primary">
                                Add Authority
                            </Button>
                        </Box>

                        {/* Assigned Users Section */}
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
                                sx={{mt: 2, display: "flex", justifyContent: "center"}}
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
import React, {useEffect, useRef, useState} from 'react';
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    List,
    ListItem,
    ListItemText,
    TextField,
} from '@mui/material';
import {Delete, Edit} from '@mui/icons-material';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const AuthorityForm = () => {
    const [authorities, setAuthorities] = useState([]);
    const [openDialog, setOpenDialog] = useState(false);
    const [currentAuthority, setCurrentAuthority] = useState(null);
    const [formData, setFormData] = useState({name: '', weight: ''});
    const [formErrors, setFormErrors] = useState({name: ''});
    const [successOpen, setSuccessOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);
    const resolveRef = useRef(null);

    useEffect(() => {
        fetch('http://localhost:8080/users/authorities')
            .then((response) => response.json())
            .then((data) => {
                const transformedData = data.map((authority) => ({
                    id: authority.ID,
                    name: authority.name,
                    weight: String(authority.weight)
                }));
                setAuthorities(transformedData || []);
            })
            .catch((err) => {
                console.error(err);
            });
    }, []);

    const handleOpenDialog = (authority = null) => {
        setCurrentAuthority(authority);
        setFormData(authority || {name: '', weight: ''});
        setFormErrors({name: '', weight: ''});
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setCurrentAuthority(null);
        setFormData({name: '', weight: ''});
        setFormErrors({name: '', weight: ''});
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData({...formData, [name]: value});
        setFormErrors({...formErrors, [name]: ''});
    };

    const validateForm = () => {
        let isValid = true;
        const errors = {name: '', weight: ''};

        if (!formData.name.trim()) {
            errors.name = 'Name is required';
            isValid = false;
        } else if (formData.name.length < 3) {
            errors.name = 'Name must be at least 3 characters long';
            isValid = false;
        }
        if (!formData.weight.trim()) {
            errors.weight = 'Authority must have weight';
            isValid = false;
        }
        setFormErrors(errors);
        return isValid;
    };

    const handleSubmit = () => {
        if (!validateForm()) {
            return;
        }

        if (currentAuthority) {
            const oldName = currentAuthority.name;
            const newName = formData.name;
            saveAuth(newName, oldName, currentAuthority.id);
        } else {
            const newName = formData.name;
            saveAuth(newName, null, null);
        }
        handleCloseDialog();
    };

    const saveAuth = async (newName, oldName, id) => {
        const userId = sessionStorage.getItem('userId');
        if (!userId) return;

        fetch(`http://localhost:8080/users/auth/${userId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
            body: JSON.stringify({
                newName,
                oldName,
                weight: parseInt(formData.weight)
            }),
        })
            .then((response) => response.json())
            .then(async (data) => {
                if (id) {
                    setAuthorities((prevAuthorities) =>
                        prevAuthorities.map((auth) =>
                            auth.id === id ? {...auth, name: newName} : auth
                        )
                    );
                } else {
                    const newAuthority = {
                        id: Date.now(),
                        name: newName,
                    };
                    setAuthorities((prevAuthorities) => [...prevAuthorities, newAuthority]);
                }
                await showSuccessDialog();
            })
            .catch(async (err) => {
                await showErrorDialog();
                console.error('Error updating role:', err);
            });
    };

    const handleDelete = (id) => {
        const userId = sessionStorage.getItem('userId');
        if (!userId) return;

        fetch(`http://localhost:8080/users/auth/${id}/${userId}`, {
            method: 'DELETE',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return id;
            })
            .then(async (data) => {
                setAuthorities((prevAuthorities) =>
                    prevAuthorities.filter((auth) => auth.id !== id)
                );
            })
            .catch(async (err) => {
                console.error('Error deleting authority:', err);
            });
    };

    const handleSuccessOpen = () => {
        setSuccessOpen(true);
    };

    const handleSuccessClose = () => {
        setSuccessOpen(false);
        if (resolveRef.current) {
            resolveRef.current();
            resolveRef.current = null;
        }
    };

    const handleErrorOpen = () => {
        setErrorOpen(true);
    };

    const handleErrorClose = () => {
        setErrorOpen(false);
        if (resolveRef.current) {
            resolveRef.current();
            resolveRef.current = null;
        }
    };

    const showSuccessDialog = () => {
        return new Promise((resolve) => {
            resolveRef.current = resolve;
            handleSuccessOpen();
        });
    };

    const showErrorDialog = () => {
        return new Promise((resolve) => {
            resolveRef.current = resolve;
            handleErrorOpen();
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
                    Add Authority
                </Button>

                <List>
                    {authorities.map((auth) => (
                        <ListItem key={auth.id}>
                            <ListItemText primary={auth.name}/>
                            <IconButton onClick={() => handleOpenDialog(auth)}>
                                <Edit/>
                            </IconButton>
                            <IconButton onClick={() => handleDelete(auth.id)}>
                                <Delete/>
                            </IconButton>
                        </ListItem>
                    ))}
                </List>

                <Dialog
                    open={openDialog}
                    onClose={handleCloseDialog}
                    maxWidth="sm"
                    fullWidth
                >
                    <DialogTitle>{currentAuthority ? 'Edit Authority' : 'Add Authority'}</DialogTitle>
                    <DialogContent>
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
                            label="Weight"
                            name="weight"
                            value={formData.weight}
                            onChange={handleChange}
                            fullWidth
                            margin="normal"
                            error={!!formErrors.weight}
                            helperText={formErrors.weight}
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Cancel</Button>
                        <Button onClick={handleSubmit} color="primary">
                            {currentAuthority ? 'Update' : 'Add'}
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        </>
    );
};

export default AuthorityForm;
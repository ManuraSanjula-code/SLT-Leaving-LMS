import React, { useState, useEffect,useCallback } from 'react';
import {
    Button,
    Box,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Typography,
    Grid,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton,
    FormControlLabel,
    Checkbox
} from '@mui/material';
import { Edit, Delete, Add } from '@mui/icons-material';
import { useSelector } from 'react-redux';

const HolidayManagement = ({ open, onClose }) => {
    const { userDetails } = useSelector((state) => state.auth);
    const [holidays, setHolidays] = useState([]);
    const [year, setYear] = useState(new Date().getFullYear());
    const [openAddDialog, setOpenAddDialog] = useState(false);
    const [openEditDialog, setOpenEditDialog] = useState(false);
    const [currentHoliday, setCurrentHoliday] = useState(null);

    const [holidayDate, setHolidayDate] = useState('');
    const [description, setDescription] = useState('');
    const [isRecurring, setIsRecurring] = useState(false);



    const fetchHolidays = useCallback(async () => {
        try {
            const loggedInUserId = sessionStorage.getItem('userId');
            if (!loggedInUserId) {
                alert('User session not found. Please log in again.');
                return;
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${loggedInUserId}?year=${year}`, {
                method: 'GET',
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setHolidays(data);
            } else {
                throw new Error('Failed to fetch holidays');
            }
        } catch (error) {
            console.error('Error fetching holidays:', error);
            alert(`Error fetching holidays: ${error.message}`);
        }
    }, [year])

    useEffect(() => {
        if (open) {
            fetchHolidays();
        } else {
            setHolidays([]);
            setYear(new Date().getFullYear());
        }
    }, [open, year, fetchHolidays]);

    const handleAddHoliday = () => {
        setHolidayDate('');
        setDescription('');
        setIsRecurring(false);
        setOpenAddDialog(true);
    };

    const handleEditHoliday = (holiday) => {
        setCurrentHoliday(holiday);
        setHolidayDate(holiday.holidayDate);
        setDescription(holiday.description);
        setIsRecurring(holiday.isRecurring);
        setOpenEditDialog(true);
    };

    const handleDeleteHoliday = async (id) => {
        if (window.confirm('Are you sure you want to delete this holiday?')) {
            try {
                const loggedInUserId = sessionStorage.getItem('userId');
                if (!loggedInUserId) {
                    alert('User session not found. Please log in again.');
                    return;
                }
                const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${id}/${loggedInUserId}`, {
                    method: 'DELETE',
                    credentials: 'include'
                });

                if (response.ok) {
                    alert('Holiday deleted successfully!');
                    fetchHolidays();
                } else {
                    throw new Error('Failed to delete holiday');
                }
            } catch (error) {
                console.error('Error deleting holiday:', error);
                alert(`Error deleting holiday: ${error.message}`);
            }
        }
    };

    const handleSubmitAdd = async () => {
        if (!holidayDate || !description) {
            alert('Please fill all required fields');
            return;
        }

        const holidayData = {
            holidayDate,
            description,
            isRecurring
        };

        try {
            const loggedInUserId = sessionStorage.getItem('userId');
            if (!loggedInUserId) {
                alert('User session not found. Please log in again.');
                return;
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${loggedInUserId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(holidayData)
            });

            if (response.ok) {
                alert('Holiday added successfully!');
                setOpenAddDialog(false);
                fetchHolidays();
            } else {
                throw new Error('Failed to add holiday');
            }
        } catch (error) {
            console.error('Error adding holiday:', error);
            alert(`Error adding holiday: ${error.message}`);
        }
    };

    const handleSubmitEdit = async () => {
        if (!holidayDate || !description || !currentHoliday) {
            alert('Please fill all required fields');
            return;
        }

        const holidayData = {
            holidayDate,
            description,
            isRecurring
        };

        try {
            const loggedInUserId = sessionStorage.getItem('userId');
            if (!loggedInUserId) {
                alert('User session not found. Please log in again.');
                return;
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${currentHoliday.id}/${loggedInUserId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(holidayData)
            });

            if (response.ok) {
                alert('Holiday updated successfully!');
                setOpenEditDialog(false);
                fetchHolidays();
            } else {
                throw new Error('Failed to update holiday');
            }
        } catch (error) {
            console.error('Error updating holiday:', error);
            alert(`Error updating holiday: ${error.message}`);
        }
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            fullWidth
            maxWidth="md"
        >
            <DialogTitle>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="h6">Holiday Management</Typography>
                    <TextField
                        label="Year"
                        type="number"
                        value={year}
                        onChange={(e) => setYear(e.target.value)}
                        size="small"
                        sx={{ width: 120 }}
                    />
                </Box>
            </DialogTitle>

            <DialogContent>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                    {userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50 && (
                        <Button
                            variant="contained"
                            startIcon={<Add />}
                            onClick={handleAddHoliday}
                        >
                            Add Holiday
                        </Button>
                    )}
                </Box>

                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Date</TableCell>
                                <TableCell>Description</TableCell>
                                <TableCell>Recurring</TableCell>
                                {userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50 && (
                                    <TableCell>Actions</TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {holidays.map((holiday) => (
                                <TableRow key={holiday.id}>
                                    <TableCell>{holiday.holidayDate}</TableCell>
                                    <TableCell>{holiday.description}</TableCell>
                                    <TableCell>{holiday.isRecurring ? 'Yes' : 'No'}</TableCell>
                                    {userDetails.highestRolePriority > 29 && userDetails.highestRolePriority < 50 && (
                                        <TableCell>
                                            <IconButton
                                                color="primary"
                                                onClick={() => handleEditHoliday(holiday)}
                                            >
                                                <Edit />
                                            </IconButton>
                                            <IconButton
                                                color="error"
                                                onClick={() => handleDeleteHoliday(holiday.id)}
                                            >
                                                <Delete />
                                            </IconButton>
                                        </TableCell>
                                    )}
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {/* Add Holiday Dialog */}
                <Dialog open={openAddDialog} onClose={() => setOpenAddDialog(false)}>
                    <DialogTitle>Add New Holiday</DialogTitle>
                    <DialogContent>
                        <Box sx={{ mt: 2 }}>
                            <TextField
                                label="Holiday Date"
                                type="date"
                                fullWidth
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                value={holidayDate}
                                onChange={(e) => setHolidayDate(e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <TextField
                                label="Description"
                                fullWidth
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={isRecurring}
                                        onChange={(e) => setIsRecurring(e.target.checked)}
                                    />
                                }
                                label="Recurring Holiday"
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpenAddDialog(false)}>Cancel</Button>
                        <Button onClick={handleSubmitAdd} color="primary">
                            Add
                        </Button>
                    </DialogActions>
                </Dialog>

                {/* Edit Holiday Dialog */}
                <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)}>
                    <DialogTitle>Edit Holiday</DialogTitle>
                    <DialogContent>
                        <Box sx={{ mt: 2 }}>
                            <TextField
                                label="Holiday Date"
                                type="date"
                                fullWidth
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                value={holidayDate}
                                onChange={(e) => setHolidayDate(e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <TextField
                                label="Description"
                                fullWidth
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={isRecurring}
                                        onChange={(e) => setIsRecurring(e.target.checked)}
                                    />
                                }
                                label="Recurring Holiday"
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpenEditDialog(false)}>Cancel</Button>
                        <Button onClick={handleSubmitEdit} color="primary">
                            Update
                        </Button>
                    </DialogActions>
                </Dialog>
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    );
};

export default HolidayManagement;
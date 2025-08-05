import React, { useEffect } from 'react';
import {
    Button,
    Box,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Typography,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton,
    FormControlLabel,
    Checkbox,
    CircularProgress,
    Alert
} from '@mui/material';
import { Edit, Delete, Add } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import {
    fetchHolidays,
    addHoliday,
    updateHoliday,
    deleteHoliday,
    setYear,
    openAddDialog,
    closeAddDialog,
    openEditDialog,
    closeEditDialog,
    updateFormData,
    clearHolidays,
    clearError
} from '../../../../lib/redux/redux-lms/other/holidaySlice';

const HolidayManagement = ({ open, onClose }) => {
    const dispatch = useDispatch();
    const { userDetails } = useSelector((state) => state.auth);
    
    const holidays = useSelector((state) => state.holidays?.holidays || []);
    const year = useSelector((state) => state.holidays?.year || new Date().getFullYear());
    const loading = useSelector((state) => state.holidays?.loading || false);
    const error = useSelector((state) => state.holidays?.error || null);
    
    const openAddDialogState = useSelector((state) => state.holidays?.openAddDialog || false);
    const openEditDialogState = useSelector((state) => state.holidays?.openEditDialog || false);
    const currentHoliday = useSelector((state) => state.holidays?.currentHoliday || null);
    const formData = useSelector((state) => state.holidays?.formData || {
        holidayDate: '',
        description: '',
        isRecurring: false
    });

    const getUserId = () => {
        const userId = sessionStorage.getItem('userId');
        if (!userId) {
            alert('User session not found. Please log in again.');
            return null;
        }
        return userId;
    };

    useEffect(() => {
        if (open) {
            const userId = getUserId();
            if (userId) {
                dispatch(fetchHolidays({ userId, year }));
            }
        } else {
            dispatch(clearHolidays());
        }
    }, [open, year, dispatch]);

    useEffect(() => {
        if (error) {
            alert(`Error: ${error}`);
            dispatch(clearError());
        }
    }, [error, dispatch]);

    const handleYearChange = (e) => {
        dispatch(setYear(e.target.value));
    };

    const handleAddHoliday = () => {
        dispatch(openAddDialog());
    };

    const handleEditHoliday = (holiday) => {
        dispatch(openEditDialog(holiday));
    };

    const handleDeleteHoliday = async (id) => {
        if (window.confirm('Are you sure you want to delete this holiday?')) {
            const userId = getUserId();
            if (userId) {
                const result = await dispatch(deleteHoliday({ holidayId: id, userId }));
                if (deleteHoliday.fulfilled.match(result)) {
                    alert('Holiday deleted successfully!');
                }
            }
        }
    };

    const handleSubmitAdd = async () => {
        if (!formData.holidayDate || !formData.description) {
            alert('Please fill all required fields');
            return;
        }

        const userId = getUserId();
        if (userId) {
            const result = await dispatch(addHoliday({ userId, holidayData: formData }));
            if (addHoliday.fulfilled.match(result)) {
                alert('Holiday added successfully!');
                dispatch(fetchHolidays({ userId, year }));
            }
        }
    };

    const handleSubmitEdit = async () => {
        if (!formData.holidayDate || !formData.description || !currentHoliday) {
            alert('Please fill all required fields');
            return;
        }

        const userId = getUserId();
        if (userId) {
            const result = await dispatch(updateHoliday({
                holidayId: currentHoliday.id,
                userId,
                holidayData: formData
            }));
            if (updateHoliday.fulfilled.match(result)) {
                alert('Holiday updated successfully!');
                dispatch(fetchHolidays({ userId, year }));
            }
        }
    };

    const handleFormChange = (field, value) => {
        dispatch(updateFormData({ [field]: value }));
    };

    const handleCloseAddDialog = () => {
        dispatch(closeAddDialog());
    };

    const handleCloseEditDialog = () => {
        dispatch(closeEditDialog());
    };

    const canManageHolidays = userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50;
    const canEditDelete = userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50;

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
                        onChange={handleYearChange}
                        size="small"
                        sx={{ width: 120 }}
                    />
                </Box>
            </DialogTitle>

            <DialogContent>
                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                    {canManageHolidays && (
                        <Button
                            variant="contained"
                            startIcon={<Add />}
                            onClick={handleAddHoliday}
                            disabled={loading}
                        >
                            Add Holiday
                        </Button>
                    )}
                </Box>

                {loading ? (
                    <Box display="flex" justifyContent="center" p={3}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Date</TableCell>
                                    <TableCell>Description</TableCell>
                                    <TableCell>Recurring</TableCell>
                                    {canManageHolidays && (
                                        <TableCell>Actions</TableCell>
                                    )}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {holidays.map((holiday) => (
                                    <TableRow key={holiday.id}>
                                        <TableCell>{holiday.holidayDate}</TableCell>
                                        <TableCell>{holiday.description}</TableCell>
                                        <TableCell>{holiday.recurring ? 'Yes' : 'No'}</TableCell>
                                        {canEditDelete && (
                                            <TableCell>
                                                <IconButton
                                                    color="primary"
                                                    onClick={() => handleEditHoliday(holiday)}
                                                    disabled={loading}
                                                >
                                                    <Edit />
                                                </IconButton>
                                                <IconButton
                                                    color="error"
                                                    onClick={() => handleDeleteHoliday(holiday.id)}
                                                    disabled={loading}
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
                )}

                <Dialog open={openAddDialogState} onClose={handleCloseAddDialog}>
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
                                value={formData.holidayDate}
                                onChange={(e) => handleFormChange('holidayDate', e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <TextField
                                label="Description"
                                fullWidth
                                value={formData.description}
                                onChange={(e) => handleFormChange('description', e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={formData.isRecurring}
                                        onChange={(e) => handleFormChange('isRecurring', e.target.checked)}
                                    />
                                }
                                label="Recurring Holiday"
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseAddDialog} disabled={loading}>
                            Cancel
                        </Button>
                        <Button onClick={handleSubmitAdd} color="primary" disabled={loading}>
                            {loading ? <CircularProgress size={20} /> : 'Add'}
                        </Button>
                    </DialogActions>
                </Dialog>

                <Dialog open={openEditDialogState} onClose={handleCloseEditDialog}>
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
                                value={formData.holidayDate}
                                onChange={(e) => handleFormChange('holidayDate', e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <TextField
                                label="Description"
                                fullWidth
                                value={formData.description}
                                onChange={(e) => handleFormChange('description', e.target.value)}
                                sx={{ mb: 2 }}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={formData.isRecurring}
                                        onChange={(e) => handleFormChange('isRecurring', e.target.checked)}
                                    />
                                }
                                label="Recurring Holiday"
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseEditDialog} disabled={loading}>
                            Cancel
                        </Button>
                        <Button onClick={handleSubmitEdit} color="primary" disabled={loading}>
                            {loading ? <CircularProgress size={20} /> : 'Update'}
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
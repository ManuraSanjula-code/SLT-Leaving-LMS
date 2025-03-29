import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Button,
    List,
    ListItem,
    ListItemText,
    ListItemSecondaryAction,
    Checkbox,
    Typography,
    Chip,
    Box,
    CircularProgress
} from '@mui/material';

const EmployeeSelectionDialog = ({
                                     open,
                                     onClose,
                                     title,
                                     employees = [],
                                     initialSelected = [],
                                     pagination,
                                     onPageChange,
                                     loading
                                 }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [selected, setSelected] = useState([]);
    const [newlyAdded, setNewlyAdded] = useState([]);
    const [deleted, setDeleted] = useState([]);

    // Initialize selection when dialog opens
    useEffect(() => {
        if (open) {
            setSelected([...initialSelected]);
            setNewlyAdded([]);
            setDeleted([]);
        }
    }, [open, initialSelected]);

    const filteredEmployees = employees.filter(employee =>
        employee.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.employeeId?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleToggle = (employee) => {
        const currentIndex = selected.findIndex(item => item.userId === employee.userId);
        const newSelected = [...selected];

        if (currentIndex === -1) {
            newSelected.push(employee);
            setSelected(newSelected);

            const deletedIndex = deleted.findIndex(item => item.userId === employee.userId);
            if (deletedIndex !== -1) {
                setDeleted(deleted.filter((_, i) => i !== deletedIndex));
            } else if (!initialSelected.some(item => item.userId === employee.userId)) {
                setNewlyAdded(prev => [...prev, employee]);
            }
        } else {
            newSelected.splice(currentIndex, 1);
            setSelected(newSelected);

            if (initialSelected.some(item => item.userId === employee.userId)) {
                setDeleted(prev => [...prev, employee]);
            } else {
                setNewlyAdded(newlyAdded.filter(item => item.userId !== employee.userId));
            }
        }
    };

    const handleClose = () => {
        onClose({
            selected,
            newlyAdded,
            deleted
        });
    };

    const getHighestPriorityRole = (roles) => {
        if (!roles || roles.length === 0) return 'No Role';
        const sorted = [...roles].sort((a, b) => a.priority - b.priority);
        return sorted[0].name.replace('ROLE_', '');
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
            <DialogTitle>
                {title}
                <Typography variant="subtitle2" color="textSecondary">
                    {selected.length} selected
                </Typography>
            </DialogTitle>
            <DialogContent>
                <TextField
                    margin="normal"
                    fullWidth
                    label="Search by name or ID"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    disabled={loading}
                />

                {loading ? (
                    <Box display="flex" justifyContent="center" my={4}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <>
                        <List sx={{ maxHeight: 400, overflow: 'auto' }}>
                            {filteredEmployees.map((employee) => {
                                const isSelected = selected.some(item => item.userId === employee.userId);
                                const highestRole = getHighestPriorityRole(employee.roles);

                                return (
                                    <ListItem
                                        key={employee.userId}
                                        button
                                        onClick={() => handleToggle(employee)}
                                    >
                                        <Checkbox
                                            edge="start"
                                            checked={isSelected}
                                            tabIndex={-1}
                                            disableRipple
                                        />
                                        <ListItemText
                                            primary={`${employee.name} (${employee.employeeId})`}
                                            secondary={
                                                <>
                                                    <span>{employee.sltId}</span>
                                                    <br />
                                                    <Chip
                                                        label={highestRole}
                                                        size="small"
                                                        style={{ marginTop: 4 }}
                                                    />
                                                </>
                                            }
                                        />
                                    </ListItem>
                                );
                            })}
                        </List>

                        {pagination && (
                            <Box display="flex" justifyContent="space-between" mt={2}>
                                <Button
                                    variant="outlined"
                                    onClick={() => onPageChange(pagination.currentPage - 1)}
                                    disabled={pagination.currentPage <= 0 || loading}
                                >
                                    Previous
                                </Button>
                                <Typography variant="body1">
                                    Page {pagination.currentPage + 1} of {pagination.totalPages}
                                </Typography>
                                <Button
                                    variant="outlined"
                                    onClick={() => onPageChange(pagination.currentPage + 1)}
                                    disabled={pagination.currentPage >= pagination.totalPages - 1 || loading}
                                >
                                    Next
                                </Button>
                            </Box>
                        )}
                    </>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={() => {
                    setSelected([...initialSelected]);
                    setNewlyAdded([]);
                    setDeleted([]);
                }}>
                    Reset
                </Button>
                <Button onClick={handleClose} color="primary">
                    Done
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default EmployeeSelectionDialog;
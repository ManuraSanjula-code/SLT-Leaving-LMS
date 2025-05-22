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
    CircularProgress,
    Pagination
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { fetchPaginatedAdmins } from '../../../../../lib/redux/redux-lms/user/managementSlice';

const EmployeeSelectionDialog = ({
                                     open,
                                     onClose,
                                     title,
                                     initialSelected = [],
                                     loading
                                 }) => {
    const dispatch = useDispatch();
    const { paginatedAdmins, currentAdminPage, adminPageSize } = useSelector(state => state.management);

    const [searchTerm, setSearchTerm] = useState('');
    const [selected, setSelected] = useState([]);
    const [newlyAdded, setNewlyAdded] = useState([]);
    const [deleted, setDeleted] = useState([]);

    // Fetch admins when dialog opens or page changes
    useEffect(() => {
        if (open) {
            dispatch(fetchPaginatedAdmins({
                page: currentAdminPage,
                limit: adminPageSize
            }));
        }
    }, [open, currentAdminPage, adminPageSize, dispatch]);

    // Initialize selection when dialog opens
    useEffect(() => {
        if (open) {
            setSelected([...initialSelected]);
            setNewlyAdded([]);
            setDeleted([]);
        }
    }, [open, initialSelected]);

    const filteredEmployees = paginatedAdmins?.content?.filter(admin =>
        admin.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        admin.employeeId?.toLowerCase().includes(searchTerm.toLowerCase())
    ) || [];

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

    const handlePageChange = (event, newPage) => {
        dispatch(fetchPaginatedAdmins({
            page: newPage - 1,
            limit: adminPageSize
        }));
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
                                            primary={`${employee.firstName + " " + employee.lastName} (${employee.employeeId})`}
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

                        {paginatedAdmins && (
                            <Box display="flex" justifyContent="center" mt={2}>
                                <Pagination
                                    count={paginatedAdmins.totalPages}
                                    page={paginatedAdmins.pageable?.pageNumber + 1 || 1}
                                    onChange={handlePageChange}
                                    color="primary"
                                    disabled={loading}
                                />
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
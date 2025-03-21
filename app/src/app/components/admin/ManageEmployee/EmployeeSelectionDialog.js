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
} from '@mui/material';


const EmployeeSelectionDialog = ({ open, onClose, onSelect, title, employees }) => {
    const [searchTerm, setSearchTerm] = useState('');

    const filteredEmployees = employees.filter(employee =>
        employee.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <TextField
                    margin="normal"
                    fullWidth
                    label="Search"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                <List>
                    {filteredEmployees.map((employee) => (
                        <ListItem button key={employee.employeeId} onClick={() => onSelect(employee)}>
                            <ListItemText primary={employee.name} secondary={employee.employeeId} />
                        </ListItem>
                    ))}
                </List>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>
            </DialogActions>
        </Dialog>
    );
};

export default EmployeeSelectionDialog;
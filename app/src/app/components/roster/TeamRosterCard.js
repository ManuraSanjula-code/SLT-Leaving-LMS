'use client'

import React from 'react';
import {
    TableContainer,
    Table,
    TableHead,
    TableBody,
    TableRow,
    TableCell,
    Paper,
    Typography,
    IconButton,
    TextField,
    Box,
    Chip,
    Divider,
    Card,
    CardContent
} from '@mui/material';
const TeamRosterCard = ({
                            team,
                            teamDetails,
                            employees,
                            editMode,
                            editingEmployee,
                            editData,
                            onEditInputChange
                        }) => {
    return (
        <Card sx={{ mb: 3 }}>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    {teamDetails.name} ({teamDetails.shortName})
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <TableContainer component={Paper} elevation={0}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Employee ID</TableCell>
                                <TableCell>Name</TableCell>
                                <TableCell align="center">Total Shifts</TableCell>
                                <TableCell align="center">Rotation Shifts</TableCell>
                                <TableCell align="center">Off Days</TableCell>
                                <TableCell align="center">Double Duty</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {team.employees.map((employee) => {
                                const employeeDetails = employees[employee.employeeId] || {
                                    name: 'Unknown Employee',
                                    employeeId: 'Unknown ID',
                                    shortName: 'UE'
                                };

                                const isEditing = editMode &&
                                    editingEmployee &&
                                    editingEmployee.teamId === team.teamId &&
                                    editingEmployee.employeeId === employee.employeeId;

                                return (
                                    <TableRow
                                        key={employee.employeeId}
                                        sx={{ bgcolor: isEditing ? 'action.hover' : 'inherit' }}
                                    >
                                        <TableCell>
                                            <Chip
                                                label={employeeDetails.employeeId}
                                                size="small"
                                                color="primary"
                                                variant="outlined"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            {employeeDetails.name}
                                            <Typography variant="caption" color="textSecondary" sx={{ ml: 1 }}>
                                                ({employeeDetails.shortName})
                                            </Typography>
                                        </TableCell>
                                        <TableCell align="center">
                                            {isEditing ? (
                                                <TextField
                                                    type="number"
                                                    size="small"
                                                    value={editData.totalShift}
                                                    onChange={(e) => onEditInputChange('totalShift', e.target.value)}
                                                    inputProps={{ min: 0, style: { textAlign: 'center' } }}
                                                />
                                            ) : (
                                                employee.totalShift
                                            )}
                                        </TableCell>
                                        <TableCell align="center">
                                            {isEditing ? (
                                                <TextField
                                                    type="number"
                                                    size="small"
                                                    value={editData.rotShift}
                                                    onChange={(e) => onEditInputChange('rotShift', e.target.value)}
                                                    inputProps={{ min: 0, style: { textAlign: 'center' } }}
                                                />
                                            ) : (
                                                employee.rotShift
                                            )}
                                        </TableCell>
                                        <TableCell align="center">
                                            {isEditing ? (
                                                <TextField
                                                    type="number"
                                                    size="small"
                                                    value={editData.offDay}
                                                    onChange={(e) => onEditInputChange('offDay', e.target.value)}
                                                    inputProps={{ min: 0, style: { textAlign: 'center' } }}
                                                />
                                            ) : (
                                                employee.offDay
                                            )}
                                        </TableCell>
                                        <TableCell align="center">
                                            {isEditing ? (
                                                <TextField
                                                    type="number"
                                                    size="small"
                                                    value={editData.dduty}
                                                    onChange={(e) => onEditInputChange('dduty', e.target.value)}
                                                    inputProps={{ min: 0, style: { textAlign: 'center' } }}
                                                />
                                            ) : (
                                                employee.dduty
                                            )}
                                        </TableCell>

                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </TableContainer>
            </CardContent>
        </Card>
    );
};

export default TeamRosterCard;
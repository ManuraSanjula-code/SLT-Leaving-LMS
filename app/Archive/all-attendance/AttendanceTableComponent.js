'use client'
import React from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Box,
    Chip,
    CircularProgress,
    Typography
} from '@mui/material';

const AttendanceTableComponent = ({
                                      data = [],
                                      isLoading,
                                      formatDate,
                                      getStatusChip,
                                      rowsPerPage
                                  }) => {
    return (
        <TableContainer sx={{ maxHeight: 440 }}>
            <Table stickyHeader aria-label="attendance table">
                <TableHead>
                    <TableRow>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Employee ID</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Date</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Shift Code</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Shift Time</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Status</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', bgcolor: 'primary.light', color: 'primary.contrastText' }}>Actions</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {isLoading && data.length === 0 ? (
                        Array.from({ length: rowsPerPage }).map((_, index) => (
                            <TableRow key={`loading-${index}`}>
                                <TableCell colSpan={6} align="center">
                                    <Box sx={{ py: 1 }}>
                                        <CircularProgress size={20} />
                                    </Box>
                                </TableCell>
                            </TableRow>
                        ))
                    ) : data.length > 0 ? (
                        data.map((row) => (
                            <TableRow
                                key={row.id || `${row.employeeID}-${row.date}`}
                                hover
                                sx={{ '&:nth-of-type(odd)': { backgroundColor: 'rgba(0, 0, 0, 0.04)' } }}
                            >
                                <TableCell>{row.employeeID || 'N/A'}</TableCell>
                                <TableCell>{formatDate(row.date)}</TableCell>
                                <TableCell>{row.shiftCode || 'N/A'}</TableCell>
                                <TableCell>{row.shiftTime || 'N/A'}</TableCell>
                                <TableCell>{getStatusChip(row)}</TableCell>
                                <TableCell>
                                    <Chip
                                        label="View Details"
                                        size="small"
                                        variant="outlined"
                                        color="primary"
                                        onClick={() => alert(`Details for ${row.employeeID}`)}
                                        disabled={isLoading}
                                        sx={{
                                            '&:hover': {
                                                backgroundColor: 'primary.light',
                                                color: 'primary.contrastText',
                                            }
                                        }}
                                    />
                                </TableCell>
                            </TableRow>
                        ))
                    ) : (
                        <TableRow>
                            <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                <Typography variant="subtitle1" color="text.secondary">
                                    No attendance records found
                                </Typography>
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default AttendanceTableComponent;
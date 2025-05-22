'use client'

import React from 'react';
import {
    Box,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Grid,
    InputAdornment
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';

const SearchFilterComponent = ({
                                   searchTerm,
                                   onSearchChange,
                                   filterStatus,
                                   onFilterStatusChange,
                                   isLoading
                               }) => {
    return (
        <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} md={6}>
                <TextField
                    label="Search by Employee ID or Shift"
                    variant="outlined"
                    fullWidth
                    value={searchTerm}
                    onChange={onSearchChange}
                    disabled={isLoading}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon color="primary" />
                            </InputAdornment>
                        ),
                        sx: {
                            borderRadius: 1,
                            height: 56,
                        }
                    }}
                />
            </Grid>
            <Grid item xs={12} md={6}>
                <FormControl fullWidth disabled={isLoading}>
                    <InputLabel>Status Filter</InputLabel>
                    <Select
                        value={filterStatus}
                        onChange={onFilterStatusChange}
                        label="Status Filter"
                        startAdornment={
                            <InputAdornment position="start">
                                <FilterListIcon color="primary" />
                            </InputAdornment>
                        }
                        sx={{
                            borderRadius: 1,
                            height: 56,
                        }}
                    >
                        <MenuItem value="all">All Statuses</MenuItem>
                        <MenuItem value="absent">Absent</MenuItem>
                        <MenuItem value="late">Late</MenuItem>
                        <MenuItem value="leave">On Leave</MenuItem>
                    </Select>
                </FormControl>
            </Grid>
        </Grid>
    );
};

export default SearchFilterComponent;
'use client'

import React from 'react';
import {
    Typography,
    Grid,
    TextField,
    InputAdornment,
    IconButton,
    Paper
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

const RosterHeader = ({
                          currentMonth,
                          currentYear,
                          searchTerm,
                          onSearchChange,
                          onPreviousMonth,
                          onNextMonth,
                          loading,
                          editMode,
                          getMonthName
                      }) => {
    return (
        <Paper sx={{ p: 2, mb: 3 }}>
            <Grid container alignItems="center" spacing={2}>
                <Grid item xs={12} md={4}>
                    <Typography variant="h5">Roster Management</Typography>
                </Grid>
                <Grid item xs={12} md={4} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <IconButton onClick={onPreviousMonth} disabled={loading || editMode}>
                        <ArrowBackIcon />
                    </IconButton>
                    <Typography variant="h6" sx={{ mx: 2 }}>
                        {getMonthName(currentMonth)} {currentYear}
                    </Typography>
                    <IconButton onClick={onNextMonth} disabled={loading || editMode}>
                        <ArrowForwardIcon />
                    </IconButton>
                </Grid>
                <Grid item xs={12} md={4}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        placeholder="Search by name, ID or team"
                        value={searchTerm}
                        onChange={onSearchChange}
                        disabled={loading || editMode}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon />
                                </InputAdornment>
                            ),
                        }}
                    />
                </Grid>
            </Grid>
        </Paper>
    );
};

export default RosterHeader;
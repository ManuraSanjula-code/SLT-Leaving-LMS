'use client'

import React from 'react';
import {
    Box,
    Alert,
    Typography,
    Paper,
    Grid,
    IconButton
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

const EmptyState = ({
                        currentMonth,
                        currentYear,
                        onPreviousMonth,
                        onNextMonth,
                        getMonthName
                    }) => {
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: 'calc(100vh - 64px)', p: 3 }}>
            <Alert severity="warning" sx={{ mb: 3, width: '100%', maxWidth: 600 }}>
                No roster data found for {getMonthName(currentMonth)} {currentYear}
            </Alert>
            <Paper sx={{ p: 2, mb: 3, width: '100%', maxWidth: 600 }}>
                <Grid container alignItems="center" spacing={2}>
                    <Grid item xs={12} md={4}>
                        <Typography variant="h5">Roster Management</Typography>
                    </Grid>
                    <Grid item xs={12} md={4} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                        <IconButton onClick={onPreviousMonth}>
                            <ArrowBackIcon />
                        </IconButton>
                        <Typography variant="h6" sx={{ mx: 2 }}>
                            {getMonthName(currentMonth)} {currentYear}
                        </Typography>
                        <IconButton onClick={onNextMonth}>
                            <ArrowForwardIcon />
                        </IconButton>
                    </Grid>
                </Grid>
            </Paper>
        </Box>
    );
};

export default EmptyState;
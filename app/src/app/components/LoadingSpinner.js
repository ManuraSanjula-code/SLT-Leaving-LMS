'use client';

import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

const LoadingSpinner = () => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        width: '100vw',
        backgroundColor: '#f5f5f5',
      }}
    >
      <CircularProgress size={60} thickness={4} />
      <Typography variant="h6" sx={{ mt: 2 }}>
        Loading...
      </Typography>
    </Box>
  );
};

export default LoadingSpinner;
"use client"

import React from 'react';
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  TextField,
  Button,
  MenuItem,
} from '@mui/material';

const ApplyLeave = () => {
  const handleSubmit = (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    console.log({
      leaveType: data.get('leaveType'),
      startDate: data.get('startDate'),
      endDate: data.get('endDate'),
      reason: data.get('reason'),
    });
  };

  return (
    <Container component="main" maxWidth="sm">
      <CssBaseline />
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h5" gutterBottom>
          Apply for Leave
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="leaveType"
            label="Leave Type"
            name="leaveType"
            select
            defaultValue=""
          >
            <MenuItem value="sick">Sick Leave</MenuItem>
            <MenuItem value="casual">Casual Leave</MenuItem>
            <MenuItem value="earned">Earned Leave</MenuItem>
          </TextField>
          <TextField
            margin="normal"
            required
            fullWidth
            id="startDate"
            label="Start Date"
            name="startDate"
            type="date"
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            id="endDate"
            label="End Date"
            name="endDate"
            type="date"
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            id="reason"
            label="Reason"
            name="reason"
            multiline
            rows={4}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
          >
            Submit Leave Request
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default ApplyLeave;
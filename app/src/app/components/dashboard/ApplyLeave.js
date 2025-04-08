"use client"

import React, { useState, useEffect } from 'react';
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  TextField,
  Button,
  MenuItem,
  Snackbar,
  Alert,
  FormControlLabel,
  Checkbox,
  CircularProgress,
  Grid,
  Card,
  CardContent
} from '@mui/material';

const ApplyLeave = () => {
  const [userId, setUserId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [fetchingBalance, setFetchingBalance] = useState(false);
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });
  const [leaveBalances, setLeaveBalances] = useState({
    CASUAL: 0,
    ANNUAL: 0,
    SICK: 0,
    SPECIAL: 0,
    DUTY: 0,
    MATERNITY_LEAVE: 0
  });

  const [formData, setFormData] = useState({
    fromDate: '',
    toDate: '',
    leaveType: '', // Will be HALF_DAY or FULL_DAY
    leaveCategory: '', // Will be CASUAL, ANNUAL, etc.
    description: '',
    isHalfDay: false,
    numOfDays: 0,
    happenDate: '',
    isUnauthorized: false,
    isManualRequest: false
  });

  const [errors, setErrors] = useState({});

  // Leave category options matching backend values
  const leaveCategories = [
    { value: "CASUAL", label: "Casual Leave" },
    { value: "ANNUAL", label: "Annual Leave" },
    { value: "SICK", label: "Sick Leave" },
    { value: "SPECIAL", label: "Special Leave" },
    { value: "DUTY", label: "Duty Leave" },
    { value: "MATERNITY_LEAVE", label: "Maternity Leave" }
  ];

  useEffect(() => {
    // Get userId from localStorage on component mount
    const storedUserId = localStorage.getItem('userId');
    if (storedUserId) {
      setUserId(storedUserId);
      fetchLeaveBalances(storedUserId);
    } else {
      setNotification({
        open: true,
        message: 'User ID not found. Please log in again.',
        severity: 'error'
      });
    }
  }, []);

  const fetchLeaveBalances = async (employeeId) => {
    setFetchingBalance(true);
    try {
      // Replace with your actual API endpoint
      const response = await fetch(`http://localhost:8080/lms/management/leave/balance/${employeeId}`, {
        credentials: 'include' // Include cookies with the request
      });

      if (!response.ok) {
        throw new Error(`Error fetching leave balances: ${response.status}`);
      }

      const balances = await response.json();
      setLeaveBalances(balances);
    } catch (error) {
      console.error('Error fetching leave balances:', error);
      setNotification({
        open: true,
        message: 'Failed to fetch leave balances. Please try again later.',
        severity: 'warning'
      });
    } finally {
      setFetchingBalance(false);
    }
  };

  useEffect(() => {
    // Calculate number of days when dates change
    if (formData.fromDate && formData.toDate) {
      const start = new Date(formData.fromDate);
      const end = new Date(formData.toDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // +1 to include both start and end dates

      setFormData({
        ...formData,
        numOfDays: formData.isHalfDay ? diffDays / 2 : diffDays,
        leaveType: formData.isHalfDay ? "HALF_DAY" : "FULL_DAY"
      });
    }
  }, [formData.fromDate, formData.toDate, formData.isHalfDay]);

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;

    if (name === "isHalfDay") {
      setFormData({
        ...formData,
        isHalfDay: checked,
        leaveType: checked ? "HALF_DAY" : "FULL_DAY"
      });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value
      });
    }

    // Clear error when field is edited
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: null
      });
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.fromDate) newErrors.fromDate = 'Start date is required';
    if (!formData.toDate) newErrors.toDate = 'End date is required';
    if (!formData.leaveCategory) newErrors.leaveCategory = 'Leave category is required';
    if (!formData.description) newErrors.description = 'Reason is required';

    if (formData.fromDate && formData.toDate) {
      const start = new Date(formData.fromDate);
      const end = new Date(formData.toDate);
      if (end < start) {
        newErrors.toDate = 'End date cannot be before start date';
      }
    }

    // Validate available leave balance
    if (formData.leaveCategory && formData.numOfDays > 0) {
      const selectedCategory = formData.leaveCategory;
      if (leaveBalances[selectedCategory] < formData.numOfDays) {
        newErrors.leaveCategory = `Insufficient ${selectedCategory.toLowerCase()} leave balance`;
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) return;
    if (!userId) {
      setNotification({
        open: true,
        message: 'User ID not found. Please log in again.',
        severity: 'error'
      });
      return;
    }

    setLoading(true);

    try {
      // Format dates for backend
      const payload = {
        ...formData,
        fromDate: new Date(formData.fromDate).toISOString(),
        toDate: new Date(formData.toDate).toISOString(),
        happenDate: formData.happenDate ? new Date(formData.happenDate).toISOString() : null
      };

      const response = await fetch(`http://localhost:8080/lms/management/leave/create/${userId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'include' // Include cookies with the request
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }

      const result = await response.json();

      setNotification({
        open: true,
        message: 'Leave request submitted successfully!',
        severity: 'success'
      });

      // Refresh leave balances after submission
      fetchLeaveBalances(userId);

      // Reset form after successful submission
      setFormData({
        fromDate: '',
        toDate: '',
        leaveType: '',
        leaveCategory: '',
        description: '',
        isHalfDay: false,
        numOfDays: 0,
        happenDate: '',
        isUnauthorized: false,
        isManualRequest: false
      });

    } catch (error) {
      console.error('Error submitting leave request:', error);
      setNotification({
        open: true,
        message: `Failed to submit leave request: ${error.message}`,
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCloseNotification = () => {
    setNotification({ ...notification, open: false });
  };

  return (
      <Container component="main" maxWidth="md">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Apply for Leave
          </Typography>

          {/* Leave Balance Cards */}
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Available Leave Balance
          </Typography>

          {fetchingBalance ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                <CircularProgress />
              </Box>
          ) : (
              <Grid container spacing={2} sx={{ mb: 3 }}>
                {leaveCategories.map((category) => (
                    <Grid item xs={6} sm={4} key={category.value}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="subtitle1">{category.label}</Typography>
                          <Typography variant="h6" color="primary">{leaveBalances[category.value]} days</Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                ))}
              </Grid>
          )}

          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
                margin="normal"
                required
                fullWidth
                id="leaveCategory"
                label="Leave Category"
                name="leaveCategory"
                select
                value={formData.leaveCategory}
                onChange={handleChange}
                error={!!errors.leaveCategory}
                helperText={errors.leaveCategory}
            >
              {leaveCategories.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label} ({leaveBalances[option.value]} days remaining)
                  </MenuItem>
              ))}
            </TextField>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="fromDate"
                    label="Start Date"
                    name="fromDate"
                    type="date"
                    value={formData.fromDate}
                    onChange={handleChange}
                    InputLabelProps={{ shrink: true }}
                    error={!!errors.fromDate}
                    helperText={errors.fromDate}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="toDate"
                    label="End Date"
                    name="toDate"
                    type="date"
                    value={formData.toDate}
                    onChange={handleChange}
                    InputLabelProps={{ shrink: true }}
                    error={!!errors.toDate}
                    helperText={errors.toDate}
                />
              </Grid>
            </Grid>

            <FormControlLabel
                control={
                  <Checkbox
                      checked={formData.isHalfDay}
                      onChange={handleChange}
                      name="isHalfDay"
                  />
                }
                label="Half Day"
            />

            {formData.numOfDays > 0 && (
                <Typography variant="body2" sx={{ mt: 1, mb: 2 }}>
                  Number of days: <strong>{formData.numOfDays}</strong> ({formData.isHalfDay ? "Half Day" : "Full Day"})
                </Typography>
            )}

            <TextField
                margin="normal"
                fullWidth
                id="happenDate"
                label="Event Date (if applicable)"
                name="happenDate"
                type="date"
                value={formData.happenDate}
                onChange={handleChange}
                InputLabelProps={{ shrink: true }}
            />

            <TextField
                margin="normal"
                required
                fullWidth
                id="description"
                label="Reason"
                name="description"
                multiline
                rows={4}
                value={formData.description}
                onChange={handleChange}
                error={!!errors.description}
                helperText={errors.description}
            />

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isManualRequest}
                          onChange={handleChange}
                          name="isManualRequest"
                      />
                    }
                    label="Manual Request"
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isUnauthorized}
                          onChange={handleChange}
                          name="isUnauthorized"
                      />
                    }
                    label="Unauthorized Leave"
                />
              </Grid>
            </Grid>

            <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading || !userId}
            >
              {loading ? <CircularProgress size={24} /> : 'Submit Leave Request'}
            </Button>
          </Box>
        </Box>

        <Snackbar
            open={notification.open}
            autoHideDuration={6000}
            onClose={handleCloseNotification}
        >
          <Alert
              onClose={handleCloseNotification}
              severity={notification.severity}
              sx={{ width: '100%' }}
          >
            {notification.message}
          </Alert>
        </Snackbar>
      </Container>
  );
};

export default ApplyLeave;
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
  CardContent,
  Divider
} from '@mui/material';

const ApplyLeave = () => {
  const [userId, setUserId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [fetchingBalance, setFetchingBalance] = useState(false);
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });
  const [leaveBalances, setLeaveBalances] = useState([]);

  const [formData, setFormData] = useState({
    fromDate: '',
    toDate: '',
    leaveCategory: '', // HALF_DAY or FULL_DAY
    leaveType: '', // Annual Leave, Medical Leave, etc.
    description: '',
    isHalfDay: false,
    isFullDay: true, // Default to true
    numOfDays: 0,
    happenDate: '',
    isUnauthorized: false,
    isManualRequest: true, // Default to true as per requirements
    isAbsent: false,
    isLateCover: false,
    isLate: false,
    unSuccessful: false
  });

  const [errors, setErrors] = useState({});

  // Updated leave types to match server data
  const leaveTypes = [
    { value: "Annual Leave", label: "Annual Leave" },
    { value: "Medical Leave", label: "Medical Leave" },
    { value: "Casual Leave", label: "Casual Leave" },
    { value: "Maternity Leave", label: "Maternity Leave" }
  ];

  useEffect(() => {
    // Get userId from sessionStorage on component mount
    const storedUserId = sessionStorage.getItem('userId');
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
      // Updated URL endpoint
      const response = await fetch(`http://localhost:8080/lms/leave-balance/${employeeId}`, {
        credentials: 'include' // Include cookies with the request
      });
      if (!response.ok) {
        throw new Error(`Error fetching leave balances: ${response.status}`);
      }

      const data = await response.json();
      if (data && data.leaveDetails) {
        setLeaveBalances(data.leaveDetails);
      } else {
        throw new Error('Invalid response format');
      }
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
        leaveCategory: formData.isHalfDay ? "HALF_DAY" : "FULL_DAY"
      });
    }
  }, [formData.fromDate, formData.toDate, formData.isHalfDay]);

  // Update isFullDay when isHalfDay changes
  useEffect(() => {
    if (formData.isHalfDay) {
      setFormData(prev => ({
        ...prev,
        isFullDay: false
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        isFullDay: true
      }));
    }
  }, [formData.isHalfDay]);

  // Update isManualRequest based on other flags
  useEffect(() => {
    if (formData.isHalfDay || formData.isUnauthorized || formData.isAbsent ||
        formData.isLateCover || formData.isLate || formData.unSuccessful) {
      setFormData(prev => ({
        ...prev,
        isManualRequest: false
      }));
    }
  }, [
    formData.isHalfDay,
    formData.isUnauthorized,
    formData.isAbsent,
    formData.isLateCover,
    formData.isLate,
    formData.unSuccessful
  ]);

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;

    if (type === 'checkbox') {
      setFormData(prev => {
        const updatedData = { ...prev, [name]: checked };

        // Handle half day and full day relationship
        if (name === 'isHalfDay') {
          updatedData.isFullDay = !checked;
          updatedData.leaveCategory = checked ? "HALF_DAY" : "FULL_DAY";
        }

        if (name === 'isFullDay') {
          updatedData.isHalfDay = !checked;
          updatedData.leaveCategory = !checked ? "HALF_DAY" : "FULL_DAY";
        }

        // If any of these are being checked, set isManualRequest to false
        if ((name === 'isHalfDay' || name === 'isUnauthorized' || name === 'isAbsent' ||
            name === 'isLateCover' || name === 'isLate' || name === 'unSuccessful') && checked) {
          updatedData.isManualRequest = false;
        }

        return updatedData;
      });
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
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
    if (!formData.leaveType) newErrors.leaveType = 'Leave type is required';
    if (!formData.description) newErrors.description = 'Reason is required';

    if (formData.fromDate && formData.toDate) {
      const start = new Date(formData.fromDate);
      const end = new Date(formData.toDate);
      if (end < start) {
        newErrors.toDate = 'End date cannot be before start date';
      }
    }

    // Validate available leave balance
    if (formData.leaveType && formData.numOfDays > 0) {
      const selectedType = formData.leaveType;
      const typeBalance = leaveBalances.find(
          balance => balance.leaveTypeName === selectedType
      );

      if (typeBalance && typeBalance.remainingLeaves < formData.numOfDays) {
        newErrors.leaveType = `Insufficient ${selectedType.toLowerCase()} leave balance`;
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
        userId: userId,
        fromDate: new Date(formData.fromDate).toISOString(),
        toDate: new Date(formData.toDate).toISOString(),
        happenDate: formData.happenDate ? new Date(formData.happenDate).toISOString() : null,
        numOfDays: Math.round(formData.numOfDays) // Ensure we send an integer
      };
      console.log(payload);
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
        leaveCategory: '',
        leaveType: '',
        description: '',
        isHalfDay: false,
        isFullDay: true,
        numOfDays: 0,
        happenDate: '',
        isUnauthorized: false,
        isManualRequest: true, // Reset to default true
        isAbsent: false,
        isLateCover: false,
        isLate: false,
        unSuccessful: false
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

  // Helper function to get remaining leave balance
  const getRemainingLeaveBalance = (typeName) => {
    const leaveType = leaveBalances.find(b => b.leaveTypeName === typeName);
    return leaveType ? leaveType.remainingLeaves : 0;
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
                {leaveBalances.map((type) => (
                    <Grid item xs={6} sm={4} key={type.leaveTypeName}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="subtitle1">{type.leaveTypeName}</Typography>
                          <Typography variant="h6" color="primary">{type.remainingLeaves} days</Typography>
                          <Typography variant="caption" color="textSecondary">
                            Total: {type.totalLeaves} days
                          </Typography>
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
                id="leaveType"
                label="Leave Type"
                name="leaveType"
                select
                value={formData.leaveType}
                onChange={handleChange}
                error={!!errors.leaveType}
                helperText={errors.leaveType}
            >
              {leaveTypes.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label} ({getRemainingLeaveBalance(option.value)} days remaining)
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

            {/* Leave day type selection */}
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
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
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isFullDay}
                          onChange={handleChange}
                          name="isFullDay"
                      />
                    }
                    label="Full Day"
                />
              </Grid>
            </Grid>

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

            <Divider sx={{ my: 2 }} />
            <Typography variant="subtitle1" gutterBottom>
              Leave Request Options
            </Typography>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isManualRequest}
                          onChange={handleChange}
                          name="isManualRequest"
                          disabled={formData.isHalfDay || formData.isUnauthorized || formData.isAbsent ||
                              formData.isLateCover || formData.isLate || formData.unSuccessful}
                      />
                    }
                    label="Manual Request"
                />
              </Grid>

              <Grid item xs={12} sm={4}>
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

              <Grid item xs={12} sm={4}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isAbsent}
                          onChange={handleChange}
                          name="isAbsent"
                      />
                    }
                    label="Absent"
                />
              </Grid>
            </Grid>

            {/* New fields from LeaveReq */}
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isLateCover}
                          onChange={handleChange}
                          name="isLateCover"
                      />
                    }
                    label="Late Cover"
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isLate}
                          onChange={handleChange}
                          name="isLate"
                      />
                    }
                    label="Late"
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.unSuccessful}
                          onChange={handleChange}
                          name="unSuccessful"
                      />
                    }
                    label="Unsuccessful"
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
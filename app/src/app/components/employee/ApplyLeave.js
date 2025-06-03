"use client"

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchLeaveBalances,
  submitLeaveRequest,
  setUserId,
  updateFormField,
  calculateDays,
  validateForm,
  closeNotification,
  selectUserId,
  selectFormData,
  selectErrors,
  selectIsValid, // Import the new selector
  selectLeaveBalances,
  selectLoading,
  selectFetchingBalance,
  selectNotification,
  leaveHelpers
} from '../../../../lib/redux/redux-lms/leave/apply/leaveSlice';
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
  const dispatch = useDispatch();

  // Select from Redux store
  const userId = useSelector(selectUserId);
  const formData = useSelector(selectFormData);
  const errors = useSelector(selectErrors);
  const isValid = useSelector(selectIsValid); // Use the new selector
  const leaveBalances = useSelector(selectLeaveBalances);
  const loading = useSelector(selectLoading);
  const fetchingBalance = useSelector(selectFetchingBalance);
  const notification = useSelector(selectNotification);

  // Destructure leave helpers
  const { getRemainingLeaveBalance, leaveTypes } = leaveHelpers;

  // Initialize user data from session storage
  useEffect(() => {
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {
      dispatch(setUserId(storedUserId));
      dispatch(fetchLeaveBalances(storedUserId));
    } else {
      dispatch({
        type: 'leaveApplication/setNotification',
        payload: {
          open: true,
          message: 'User ID not found. Please log in again.',
          severity: 'error'
        }
      });
    }
  }, [dispatch]);

  // Calculate days when dates or half-day changes
  useEffect(() => {
    if (formData.fromDate && formData.toDate) {
      dispatch(calculateDays());
    }
  }, [dispatch, formData.fromDate, formData.toDate, formData.isHalfDay]);

  // Handle form field changes
  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;
    dispatch(updateFormField({ name, value, checked, type }));
  };

  // Handle form submission - FIXED: Use selector instead of action payload
  const handleSubmit = async (event) => {
    event.preventDefault();

    // Dispatch validation and then check the result from state
    dispatch(validateForm());

    // We need to get the validation result after the state update
    // So we'll use a setTimeout to check the state in the next tick
    setTimeout(() => {
      // Get the current state
      const currentIsValid = isValid;

      if (!currentIsValid) return;

      if (!userId) {
        dispatch({
          type: 'leaveApplication/setNotification',
          payload: {
            open: true,
            message: 'User ID not found. Please log in again.',
            severity: 'error'
          }
        });
        return;
      }

      dispatch(submitLeaveRequest({ formData, userId }))
          .unwrap()
          .then(() => {
            // Refresh leave balances after successful submission
            dispatch(fetchLeaveBalances(userId));
          });
    }, 0);
  };

  // Handle notification close
  const handleCloseNotification = () => {
    dispatch(closeNotification());
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
                    {option.label} ({getRemainingLeaveBalance(leaveBalances, option.value)} days remaining)
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
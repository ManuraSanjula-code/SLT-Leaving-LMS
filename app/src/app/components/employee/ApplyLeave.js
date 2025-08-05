"use client"

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchLeaveBalances,
  submitLeaveRequest,
  setUserId,
  updateFormField,
  calculateDays,
  setComponentBehavior,
  validateForm,
  closeNotification,
  selectUserId,
  selectFormData,
  selectErrors,
  selectIsValid,
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
  Divider,
  FormControl,
  InputLabel,
  Select,
  Chip
} from '@mui/material';

const ApplyLeave = () => {
  const dispatch = useDispatch();

  const userId = useSelector(selectUserId);
  const formData = useSelector(selectFormData);
  const errors = useSelector(selectErrors);
  const isValid = useSelector(selectIsValid);
  const leaveBalances = useSelector(selectLeaveBalances);
  const loading = useSelector(selectLoading);
  const fetchingBalance = useSelector(selectFetchingBalance);
  const notification = useSelector(selectNotification);

  const {
    getRemainingLeaveBalance,
    leaveTypes,
    componentBehaviors,
    getCategoryType,
    isManualRequestAllowed,
    allowedManualCategories,
    restrictedCategories
  } = leaveHelpers;

  const isManualRequestDisabled = restrictedCategories.includes(formData.componentBehavior);

  const isManualRequestAllowedForCategory = allowedManualCategories.includes(formData.componentBehavior);

  const currentCategoryType = getCategoryType(formData.componentBehavior);

  useEffect(() => {
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {

      dispatch(setUserId(storedUserId));
      dispatch(fetchLeaveBalances(storedUserId));

      const urlParams = new URLSearchParams(window.location.search);

      const fromDate = urlParams.get('fromDate');
      const toDate = urlParams.get('toDate');
      const happenDate = urlParams.get('happenDate');
      const componentBehavior = urlParams.get('componentBehavior');

      if (fromDate) {
        dispatch(updateFormField({ name: 'fromDate', value: fromDate }));
      }
      if (toDate) {
        dispatch(updateFormField({ name: 'toDate', value: toDate }));
      }
      if (happenDate) {
        dispatch(updateFormField({ name: 'happenDate', value: happenDate }));
      }
      if (componentBehavior) {
        dispatch(setComponentBehavior(componentBehavior));
      }

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

  useEffect(() => {
    if (isManualRequestAllowedForCategory && !formData.isManualRequest) {
      dispatch(updateFormField({
        name: 'isManualRequest',
        value: true,
        checked: true,
        type: 'checkbox'
      }));
    }
  }, [dispatch, formData.componentBehavior, isManualRequestAllowedForCategory, formData.isManualRequest]);

  useEffect(() => {
    if (isManualRequestDisabled && formData.isManualRequest) {
      dispatch(updateFormField({
        name: 'isManualRequest',
        value: false,
        checked: false,
        type: 'checkbox'
      }));
    }
  }, [dispatch, formData.componentBehavior, isManualRequestDisabled, formData.isManualRequest]);

  useEffect(() => {
    if (formData.fromDate && formData.toDate) {
      dispatch(calculateDays());
    }
  }, [dispatch, formData.fromDate, formData.toDate, formData.componentBehavior]);

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;
    dispatch(updateFormField({ name, value, checked, type }));
  };

  const handleComponentBehaviorChange = (event) => {
    const newBehavior = event.target.value;
    dispatch(setComponentBehavior(newBehavior));
  };

  const handleManualRequestChange = (event) => {
    if (!isManualRequestDisabled) {
      handleChange(event);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    dispatch(validateForm());

    setTimeout(() => {
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
            dispatch(fetchLeaveBalances(userId));
          });
    }, 0);
  };

  const handleCloseNotification = () => {
    dispatch(closeNotification());
  };

  const getCategoryTypeColor = (type) => {
    switch (type) {
      case 'leave':
        return 'primary';
      case 'attendance':
        return 'secondary';
      default:
        return 'default';
    }
  };

  return (
      <Container component="main" maxWidth="md">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Apply for Leave
          </Typography>

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

            <FormControl fullWidth margin="normal" required>
              <InputLabel id="componentBehavior-label">Leave Category</InputLabel>
              <Select
                  labelId="componentBehavior-label"
                  id="componentBehavior"
                  name="componentBehavior"
                  value={formData.componentBehavior}
                  label="Leave Category"
                  onChange={handleComponentBehaviorChange}
                  error={!!errors.componentBehavior}
              >
                {componentBehaviors.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <span>{option.label}</span>
                        <Chip
                            label={option.type}
                            size="small"
                            color={getCategoryTypeColor(option.type)}
                            variant="outlined"
                        />
                        {option.allowsManualRequest && (
                            <Chip
                                label="Manual OK"
                                size="small"
                                color="success"
                                variant="outlined"
                            />
                        )}
                      </Box>
                    </MenuItem>
                ))}
              </Select>
              {errors.componentBehavior && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 2 }}>
                    {errors.componentBehavior}
                  </Typography>
              )}
            </FormControl>

            <Box sx={{ mt: 1, mb: 2 }}>
              <Typography variant="body2" color="textSecondary">
                <strong>Category Type:</strong>{' '}
                <Chip
                    label={currentCategoryType.charAt(0).toUpperCase() + currentCategoryType.slice(1)}
                    size="small"
                    color={getCategoryTypeColor(currentCategoryType)}
                    variant="filled"
                />
              </Typography>
            </Box>

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
                    disabled={formData.componentBehavior === 'HALF_DAY'}
                />
              </Grid>
            </Grid>

            {formData.numOfDays > 0 && (
                <Typography variant="body2" sx={{ mt: 1, mb: 2 }}>
                  Number of days: <strong>{formData.numOfDays}</strong>
                  {formData.componentBehavior === 'HALF_DAY' ? " (Half Day)" : " (Full Day)"}
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
              Request Options
            </Typography>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          checked={formData.isManualRequest}
                          onChange={handleManualRequestChange}
                          name="isManualRequest"
                          disabled={isManualRequestDisabled}
                      />
                    }
                    label={
                      <Box>
                        <Typography component="span">Manual Request</Typography>
                        {isManualRequestDisabled && (
                            <Typography variant="caption" color="error" display="block">
                              Not available for {currentCategoryType} categories
                            </Typography>
                        )}
                        {isManualRequestAllowedForCategory && (
                            <Typography variant="caption" color="success.main" display="block">
                              Available for this category
                            </Typography>
                        )}
                      </Box>
                    }
                />
                {errors.isManualRequest && (
                    <Typography variant="caption" color="error" display="block" sx={{ ml: 4 }}>
                      {errors.isManualRequest}
                    </Typography>
                )}
              </Grid>
            </Grid>

            <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" color="textSecondary">
                <strong>Selected Category:</strong> {componentBehaviors.find(cb => cb.value === formData.componentBehavior)?.label}
                <Chip
                    label={currentCategoryType}
                    size="small"
                    color={getCategoryTypeColor(currentCategoryType)}
                    variant="outlined"
                    sx={{ ml: 1 }}
                />
              </Typography>

              {formData.componentBehavior === 'HALF_DAY' && (
                  <Typography variant="caption" color="textSecondary" display="block" sx={{ mt: 1 }}>
                    Half day leave must be for a single day only. Manual Request is available.
                  </Typography>
              )}
              {formData.componentBehavior === 'FULL_DAY' && (
                  <Typography variant="caption" color="textSecondary" display="block" sx={{ mt: 1 }}>
                    Full day leave. Manual Request is available.
                  </Typography>
              )}
              {restrictedCategories.includes(formData.componentBehavior) && (
                  <Typography variant="caption" color="error" display="block" sx={{ mt: 1 }}>
                    Manual Request is not available for this category.
                  </Typography>
              )}

              <Box sx={{ mt: 2, p: 1, bgcolor: isManualRequestAllowedForCategory ? 'success.50' : 'error.50', borderRadius: 1 }}>
                <Typography variant="caption" color={isManualRequestAllowedForCategory ? 'success.main' : 'error.main'}>
                  <strong>Manual Request:</strong> {isManualRequestAllowedForCategory ? 'Allowed' : 'Not Allowed'} for this category
                </Typography>
              </Box>
            </Box>

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
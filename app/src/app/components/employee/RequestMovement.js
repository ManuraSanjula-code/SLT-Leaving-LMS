import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  submitMovementRequest,
  updateFormField,
  setUserId,
  clearError,
  clearSuccessMessage,
  selectMovementRequestForm,
  selectMovementRequestStatus,
  selectMovementRequestError,
  selectMovementRequestSuccess,
  MovementType
} from '../../../../lib/redux/redux-lms/movement/req/movementRequestSlice';
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
  FormControl,
  InputLabel,
  Select,
  Grid,
  CircularProgress,
  FormHelperText,
  Divider
} from '@mui/material';
import { useSearchParams } from 'next/navigation';

const MOVEMENT_TYPES = {
  [MovementType.FULLDAY]: 'Full Day',
  [MovementType.OFFICE_TO_HOME]: 'Office to Home',
  [MovementType.HOME_TO_OFFICE]: 'Home to Office',
  [MovementType.REMOTEWORK]: 'Remote Work'
};

const RequestMovement = () => {
  const searchParams = useSearchParams();
  const dispatch = useDispatch();
  const formData = useSelector(selectMovementRequestForm);
  const status = useSelector(selectMovementRequestStatus);
  const error = useSelector(selectMovementRequestError);
  const successMessage = useSelector(selectMovementRequestSuccess);

  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  const isLoading = status === 'loading';
  const showError = Boolean(error);
  const showSuccess = Boolean(successMessage);

  const normalizeTime = (time) => {
    if (!time) return null;

    if (/^\d{1,2}:\d{2}$/.test(time)) {
      return `${time}:00`;
    }
    if (/^\d{1,2}:\d{2}:\d{2}$/.test(time)) {
      return time;
    }

    return null;
  };


  useEffect(() => {
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {
      dispatch(setUserId(storedUserId));
    }
  }, [dispatch]);

  useEffect(() => {
    if (searchParams) {
      const mappedData = {
        employeeId: searchParams.get('employeeId') || '',
        movementType: searchParams.get('movementType') || MovementType.FULLDAY,
        happenDate: searchParams.get('happenDate') || '',
        logTime: searchParams.get('logTime') || '',
        inTime: searchParams.get('inTime') || '',
        outTime: searchParams.get('outTime') || '',
        comment: searchParams.get('comment') || '',
      };

      Object.entries(mappedData).forEach(([field, value]) => {
        dispatch(updateFormField({ name: field, value }));
      });

      const terminalId = searchParams.get('terminalId');
      if (terminalId) {
        dispatch(updateFormField({
          name: 'comment',
          value: `${mappedData.comment}\nTerminal ID: ${terminalId}`
        }));
      }

      // Add context about the issue if present
      const issueContext = [];
      if (searchParams.get('isLate') === 'true') {
        issueContext.push('Late arrival');
      }
      if (searchParams.get('isUnauthorized') === 'true') {
        issueContext.push('Unauthorized absence');
      }
      if (searchParams.get('hasIssues') === 'true') {
        issueContext.push('Swipe issues');
      }

      if (issueContext.length > 0) {
        dispatch(updateFormField({
          name: 'comment',
          value: `${mappedData.comment}\nIssue: ${issueContext.join(', ')}`
        }));
      }
    }
  }, [searchParams, dispatch]);

  const validateField = (name, value) => {
    switch (name) {
      case 'employeeId':
        if (!value || value.trim() === '') return 'Employee ID is required';
        if (value.length < 3) return 'Employee ID must be at least 3 characters';
        return '';

      case 'movementType':
        if (!value) return 'Movement Type is required';
        return '';

      case 'happenDate':
        if (!value) return 'Date is required';
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate > today) return 'Date cannot be in the future';
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        if (selectedDate < thirtyDaysAgo) return 'Date cannot be more than 30 days ago';
        return '';

      case 'comment':
        if (!value || value.trim() === '') return 'Reason/Comment is required';
        if (value.trim().length < 10) return 'Comment must be at least 10 characters';
        if (value.length > 500) return 'Comment cannot exceed 500 characters';
        return '';

      case 'destination':
        if (!value || value.trim() === '') return 'Destination is required';
        if (value.trim().length < 2) return 'Destination must be at least 2 characters';
        if (value.length > 100) return 'Destination cannot exceed 100 characters';
        return '';

      case 'category':
        if (!value || value.trim() === '') return 'Category is required';
        if (value.trim().length < 2) return 'Category must be at least 2 characters';
        if (value.length > 50) return 'Category cannot exceed 50 characters';
        return '';

      case 'logTime':
        if (!value) return 'Log time is required';
        const logDate = new Date(value);
        if (isNaN(logDate.getTime())) return 'Invalid log time format';
        return '';

      case 'inTime':
        if (!value) return 'In time is required';
        // if (!/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]?$/.test(value)) return 'Invalid time format (HH:MM)';
        return '';

      case 'outTime':
        if (!value) return 'Out time is required';
        // if (!/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]?$/.test(value)) return 'Invalid time format (HH:MM)';
        if (formData.inTime && value <= formData.inTime) return 'Out time must be after in time';
        return '';

      default:
        return '';
    }
  };

  const validateForm = () => {
    const newErrors = {};
    const fields = [
      'employeeId', 'movementType', 'happenDate', 'comment',
      'destination', 'category', 'logTime', 'inTime', 'outTime'
    ];

    fields.forEach(field => {
      const error = validateField(field, formData[field]);
      if (error) newErrors[field] = error;
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    dispatch(updateFormField({ name, value }));
    setTouched(prev => ({ ...prev, [name]: true }));

    const error = validateField(name, value);
    setErrors(prev => ({ ...prev, [name]: error }));
  };

  const handleBlur = (event) => {
    const { name, value } = event.target;
    setTouched(prev => ({ ...prev, [name]: true }));
    const error = validateField(name, value);
    setErrors(prev => ({ ...prev, [name]: error }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const newTouched = {
      employeeId: true,
      movementType: true,
      happenDate: true,
      comment: true,
      destination: true,
      category: true,
      logTime: true,
      inTime: true,
      outTime: true
    };
    setTouched(newTouched);

    if (!validateForm()) return;

    const submissionData = {
      ...formData,
      inTime: normalizeTime(formData.inTime),
      outTime: normalizeTime(formData.outTime),
      inTimeRaw: formData.inTime,
      outTimeRaw: formData.outTime,
      employeeId: formData.employeeId?.trim(),
      comment: formData.comment?.trim(),
      destination: formData.destination?.trim(),
      category: formData.category?.trim()
    };

    dispatch(submitMovementRequest(submissionData));
  };

  const handleCloseError = () => dispatch(clearError());
  const handleCloseSuccess = () => dispatch(clearSuccessMessage());
  const shouldShowError = (fieldName) => touched[fieldName] && errors[fieldName];

  return (
      <Container component="main" maxWidth="md">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Request Movement
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="employeeId"
                    label="Employee ID"
                    name="employeeId"
                    value={formData.employeeId || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('employeeId')}
                    helperText={shouldShowError('employeeId') ? errors.employeeId : ''}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="normal" error={shouldShowError('movementType')} required>
                  <InputLabel>Movement Type*</InputLabel>
                  <Select
                      name="movementType"
                      value={formData.movementType || ''}
                      onChange={handleChange}
                      onBlur={handleBlur}
                  >
                    {Object.entries(MOVEMENT_TYPES).map(([key, value]) => (
                        <MenuItem key={key} value={key}>{value}</MenuItem>
                    ))}
                  </Select>
                  {shouldShowError('movementType') && <FormHelperText>{errors.movementType}</FormHelperText>}
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Date"
                    name="happenDate"
                    type="date"
                    value={formData.happenDate || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('happenDate')}
                    helperText={shouldShowError('happenDate') ? errors.happenDate : ''}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>

              {/*<Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Category"
                    name="category"
                    value={formData.category || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('category')}
                    helperText={shouldShowError('category') ? errors.category : ''}
                />
              </Grid>*/}

              <Grid item xs={12}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Destination"
                    name="destination"
                    value={formData.destination || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('destination')}
                    helperText={shouldShowError('destination') ? errors.destination : ''}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Reason/Comment"
                    name="comment"
                    multiline
                    rows={4}
                    value={formData.comment || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('comment')}
                    helperText={shouldShowError('comment') ? errors.comment : `${formData.comment?.length || 0}/500 characters`}
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }}>
                  <Typography variant="body2">Time Details</Typography>
                </Divider>
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Log Time"
                    name="logTime"
                    type="datetime-local"
                    value={formData.logTime || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('logTime')}
                    helperText={shouldShowError('logTime') ? errors.logTime : ''}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="In Time"
                    name="inTime"
                    type="time"
                    value={formData.inTime || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('inTime')}
                    helperText={shouldShowError('inTime') ? errors.inTime : ''}
                    InputLabelProps={{ shrink: true }}
                    inputProps={{ step: 1 }}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    label="Out Time"
                    name="outTime"
                    type="time"
                    value={formData.outTime || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('outTime')}
                    helperText={shouldShowError('outTime') ? errors.outTime : ''}
                    InputLabelProps={{ shrink: true }}
                    inputProps={{ step: 1 }}
                />
              </Grid>
            </Grid>

            <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Submit Movement Request'}
            </Button>

            {Object.keys(errors).length > 0 && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  Please fix the following errors:
                  <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
                    {Object.entries(errors).map(([field, error]) => (
                        error && <li key={field}>{error}</li>
                    ))}
                  </ul>
                </Alert>
            )}
          </Box>
        </Box>

        <Snackbar open={showError} autoHideDuration={6000} onClose={handleCloseError}>
          <Alert onClose={handleCloseError} severity="error">{error}</Alert>
        </Snackbar>

        <Snackbar open={showSuccess} autoHideDuration={6000} onClose={handleCloseSuccess}>
          <Alert onClose={handleCloseSuccess} severity="success">{successMessage}</Alert>
        </Snackbar>
      </Container>
  );
};

export default RequestMovement;
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  submitMovementRequest,
  updateFormField,
  setUserId,
  setComponentBehavior,
  clearError,
  clearSuccessMessage,
  selectMovementRequestForm,
  selectMovementRequestStatus,
  selectMovementRequestError,
  selectMovementRequestSuccess
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

const MOVEMENT_TYPES = {
  ABSENT: 'Absent',
  UNSUCCESSFUL: 'Unsuccessful',
  REMOTEWORK: 'Remote Work',
  UNAUTHORIZED: 'Unauthorized'
};

const COMPONENT_BEHAVIORS = {
  UNSUCCESSFUL: 'Unsuccessful',
  UNAUTHORIZED: 'Unauthorized',
  ABSENT: 'Absent'
};

const RequestMovement = () => {
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

  const DEFAULT_LOG_TIME = '1990-01-01T00:00';
  const DEFAULT_TIME = '00:00';

  useEffect(() => {
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {
      dispatch(setUserId(storedUserId));
    }

    if (!formData.logTime) {
      dispatch(updateFormField({ name: 'logTime', value: DEFAULT_LOG_TIME }));
    }
    if (!formData.inTime) {
      dispatch(updateFormField({ name: 'inTime', value: DEFAULT_TIME }));
    }
    if (!formData.outTime) {
      dispatch(updateFormField({ name: 'outTime', value: DEFAULT_TIME }));
    }
  }, [dispatch]);

  const validateField = (name, value) => {
    switch (name) {
      case 'employeeId':
        if (!value || value.trim() === '') {
          return 'Employee ID is required';
        }
        if (value.length < 3) {
          return 'Employee ID must be at least 3 characters';
        }
        return '';

      case 'movementType':
        if (!value || value === '') {
          return 'Movement Type is required';
        }
        return '';

      case 'componentBehavior':
        if (!value || value === '') {
          return 'Component Behavior is required';
        }
        return '';

      case 'happenDate':
        if (!value || value === '') {
          return 'Date is required';
        }
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate > today) {
          return 'Date cannot be in the future';
        }
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        if (selectedDate < thirtyDaysAgo) {
          return 'Date cannot be more than 30 days ago';
        }
        return '';

      case 'comment':
        if (!value || value.trim() === '') {
          return 'Reason/Comment is required';
        }
        if (value.trim().length < 10) {
          return 'Comment must be at least 10 characters';
        }
        if (value.length > 500) {
          return 'Comment cannot exceed 500 characters';
        }
        return '';

      case 'destination':
        if (!value || value.trim() === '') {
          return 'Destination is required';
        }
        if (value.trim().length < 2) {
          return 'Destination must be at least 2 characters';
        }
        if (value.length > 100) {
          return 'Destination cannot exceed 100 characters';
        }
        return '';

      case 'category':
        if (value && value.trim() !== '') {
          if (value.trim().length < 2) {
            return 'Category must be at least 2 characters';
          }
          if (value.length > 50) {
            return 'Category cannot exceed 50 characters';
          }
        }
        return '';

      case 'logTime':
        if (value && value !== DEFAULT_LOG_TIME) {
          const logDate = new Date(value);
          if (isNaN(logDate.getTime())) {
            return 'Invalid log time format';
          }
        }
        return '';

      case 'inTime':
      case 'outTime':
        if (value && value !== DEFAULT_TIME) {
          const timeRegex = /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/;
          if (!timeRegex.test(value)) {
            return 'Invalid time format (HH:MM)';
          }
        }
        return '';

      default:
        return '';
    }
  };

  const validateForm = () => {
    const newErrors = {};
    const requiredFields = ['employeeId', 'movementType', 'happenDate', 'comment', 'destination', 'componentBehavior'];

    requiredFields.forEach(field => {
      const error = validateField(field, formData[field]);
      if (error) {
        newErrors[field] = error;
      }
    });

    const optionalFields = ['category', 'logTime', 'inTime', 'outTime'];
    optionalFields.forEach(field => {
      const error = validateField(field, formData[field]);
      if (error) {
        newErrors[field] = error;
      }
    });

    if (formData.inTime && formData.outTime &&
        formData.inTime !== DEFAULT_TIME && formData.outTime !== DEFAULT_TIME) {
      const inTime = new Date(`1970-01-01T${formData.inTime}`);
      const outTime = new Date(`1970-01-01T${formData.outTime}`);
      if (outTime <= inTime) {
        newErrors.outTime = 'Out time must be after in time';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;
    const newValue = type === 'checkbox' ? checked : value;

    dispatch(updateFormField({
      name,
      value: newValue
    }));

    setTouched(prev => ({ ...prev, [name]: true }));

    if (touched[name] || type !== 'checkbox') {
      const error = validateField(name, newValue);
      setErrors(prev => ({
        ...prev,
        [name]: error
      }));
    }
  };

  const handleComponentBehaviorChange = (event) => {
    const behavior = event.target.value;
    dispatch(setComponentBehavior(behavior));

    setTouched(prev => ({ ...prev, componentBehavior: true }));
    const error = validateField('componentBehavior', behavior);
    setErrors(prev => ({ ...prev, componentBehavior: error }));
  };

  const handleBlur = (event) => {
    const { name, value } = event.target;
    setTouched(prev => ({ ...prev, [name]: true }));

    const error = validateField(name, value);
    setErrors(prev => ({
      ...prev,
      [name]: error
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const allFields = ['employeeId', 'movementType', 'happenDate', 'comment', 'destination', 'category', 'componentBehavior', 'logTime', 'inTime', 'outTime'];
    const newTouched = {};
    allFields.forEach(field => {
      newTouched[field] = true;
    });
    setTouched(newTouched);

    if (!validateForm()) {
      return;
    }

    const submissionData = {
      ...formData,
      logTime: formData.logTime || DEFAULT_LOG_TIME,
      inTime: formData.inTime || DEFAULT_TIME,
      outTime: formData.outTime || DEFAULT_TIME,
      employeeId: formData.employeeId?.trim(),
      comment: formData.comment?.trim(),
      destination: formData.destination?.trim(),
      category: formData.category?.trim()
    };

    dispatch(submitMovementRequest(submissionData));
  };

  const handleCloseError = () => {
    dispatch(clearError());
  };

  const handleCloseSuccess = () => {
    dispatch(clearSuccessMessage());
  };

  const shouldShowError = (fieldName) => {
    return touched[fieldName] && errors[fieldName];
  };

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
                <FormControl
                    fullWidth
                    margin="normal"
                    error={shouldShowError('movementType')}
                    required
                >
                  <InputLabel id="movement-type-label">Movement Type*</InputLabel>
                  <Select
                      labelId="movement-type-label"
                      id="movementType"
                      name="movementType"
                      value={formData.movementType || ''}
                      label="Movement Type*"
                      onChange={handleChange}
                      onBlur={handleBlur}
                  >
                    {Object.entries(MOVEMENT_TYPES).map(([key, value]) => (
                        <MenuItem key={key} value={key}>
                          {value}
                        </MenuItem>
                    ))}
                  </Select>
                  {shouldShowError('movementType') && (
                      <FormHelperText>{errors.movementType}</FormHelperText>
                  )}
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl
                    fullWidth
                    margin="normal"
                    error={shouldShowError('componentBehavior')}
                    required
                >
                  <InputLabel id="component-behavior-label">Component Behavior*</InputLabel>
                  <Select
                      labelId="component-behavior-label"
                      id="componentBehavior"
                      name="componentBehavior"
                      value={formData.componentBehavior || ''}
                      label="Component Behavior*"
                      onChange={handleComponentBehaviorChange}
                      onBlur={handleBlur}
                  >
                    {Object.entries(COMPONENT_BEHAVIORS).map(([key, value]) => (
                        <MenuItem key={key} value={key}>
                          {value}
                        </MenuItem>
                    ))}
                  </Select>
                  {shouldShowError('componentBehavior') && (
                      <FormHelperText>{errors.componentBehavior}</FormHelperText>
                  )}
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="happenDate"
                    label="Date"
                    name="happenDate"
                    type="date"
                    value={formData.happenDate || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('happenDate')}
                    helperText={shouldShowError('happenDate') ? errors.happenDate : 'Cannot be future date or more than 30 days ago'}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="category"
                    label="Category"
                    name="category"
                    value={formData.category || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('category')}
                    helperText={shouldShowError('category') ? errors.category : 'Optional (max 50 characters)'}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="destination"
                    label="Destination"
                    name="destination"
                    value={formData.destination || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('destination')}
                    helperText={shouldShowError('destination') ? errors.destination : 'Required (min 2 characters, max 100 characters)'}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="comment"
                    label="Reason/Comment"
                    name="comment"
                    multiline
                    rows={4}
                    value={formData.comment || ''}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('comment')}
                    helperText={shouldShowError('comment') ? errors.comment : `${(formData.comment || '').length}/500 characters (min 10 required)`}
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }}>
                  <Typography variant="body2" color="textSecondary">
                    Time Details (Optional)
                  </Typography>
                </Divider>
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="logTime"
                    label="Log Time"
                    name="logTime"
                    type="datetime-local"
                    value={formData.logTime || DEFAULT_LOG_TIME}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('logTime')}
                    helperText={shouldShowError('logTime') ? errors.logTime : "Optional - defaults to 1990-01-01 00:00"}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="inTime"
                    label="In Time"
                    name="inTime"
                    type="time"
                    value={formData.inTime || DEFAULT_TIME}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('inTime')}
                    helperText={shouldShowError('inTime') ? errors.inTime : "Optional - work start time"}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="outTime"
                    label="Out Time"
                    name="outTime"
                    type="time"
                    value={formData.outTime || DEFAULT_TIME}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={shouldShowError('outTime')}
                    helperText={shouldShowError('outTime') ? errors.outTime : "Optional - work end time"}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>
            </Grid>

            <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" color="textSecondary">
                <strong>Selected Behavior:</strong> {COMPONENT_BEHAVIORS[formData.componentBehavior] || 'None'}
              </Typography>
              {formData.componentBehavior === 'ABSENT' && (
                  <Typography variant="caption" color="textSecondary" display="block" sx={{ mt: 1 }}>
                    For absent requests - specify the reason and destination.
                  </Typography>
              )}
              {formData.componentBehavior === 'UNSUCCESSFUL' && (
                  <Typography variant="caption" color="textSecondary" display="block" sx={{ mt: 1 }}>
                    For unsuccessful attendance requests.
                  </Typography>
              )}
              {formData.componentBehavior === 'UNAUTHORIZED' && (
                  <Typography variant="caption" color="textSecondary" display="block" sx={{ mt: 1 }}>
                    For unauthorized absence requests.
                  </Typography>
              )}
            </Box>

            <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Submit Movement Request'}
            </Button>

            {Object.keys(errors).length > 0 && Object.values(touched).some(t => t) && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  Please fix the following errors:
                  <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
                    {Object.entries(errors).map(([field, error]) => (
                        error && touched[field] && (
                            <li key={field}>{error}</li>
                        )
                    ))}
                  </ul>
                </Alert>
            )}
          </Box>
        </Box>

        <Snackbar
            open={showError}
            autoHideDuration={6000}
            onClose={handleCloseError}
        >
          <Alert onClose={handleCloseError} severity="error">
            {error}
          </Alert>
        </Snackbar>

        <Snackbar
            open={showSuccess}
            autoHideDuration={6000}
            onClose={handleCloseSuccess}
        >
          <Alert onClose={handleCloseSuccess} severity="success">
            {successMessage}
          </Alert>
        </Snackbar>
      </Container>
  );
};

export default RequestMovement;
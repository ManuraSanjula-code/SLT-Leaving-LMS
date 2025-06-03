import React, { useEffect } from 'react';
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
  FormControlLabel,
  Checkbox,
  FormControl,
  InputLabel,
  Select,
  Grid,
  CircularProgress
} from '@mui/material';

const RequestMovement = () => {
  const dispatch = useDispatch();

  // Get state from Redux store
  const formData = useSelector(selectMovementRequestForm);
  const status = useSelector(selectMovementRequestStatus);
  const error = useSelector(selectMovementRequestError);
  const successMessage = useSelector(selectMovementRequestSuccess);

  // Derived states for UI
  const isLoading = status === 'loading';
  const showError = Boolean(error);
  const showSuccess = Boolean(successMessage);

  useEffect(() => {
    // Get userId from sessionStorage
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {
      dispatch(setUserId(storedUserId));
    }
  }, [dispatch]);

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;
    dispatch(updateFormField({
      name,
      value: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(submitMovementRequest());
  };

  const handleCloseError = () => {
    dispatch(clearError());
  };

  const handleCloseSuccess = () => {
    dispatch(clearSuccessMessage());
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
                    value={formData.employeeId}
                    onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="normal">
                  <InputLabel id="movement-type-label">Movement Type*</InputLabel>
                  <Select
                      labelId="movement-type-label"
                      id="movementType"
                      name="movementType"
                      value={formData.movementType}
                      label="Movement Type*"
                      onChange={handleChange}
                      required
                  >
                    <MenuItem value="ABSENT">Absent</MenuItem>
                    <MenuItem value="LATEWORK">Late Work</MenuItem>
                    <MenuItem value="UNSUCCESSFUL">Unsuccessful</MenuItem>
                    <MenuItem value="UNAUTHORIZED">Unauthorized</MenuItem>
                    <MenuItem value="REMOTEWORK">Remote Work</MenuItem>
                  </Select>
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
                    value={formData.happenDate}
                    onChange={handleChange}
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
                    value={formData.category}
                    onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="destination"
                    label="Destination"
                    name="destination"
                    value={formData.destination}
                    onChange={handleChange}
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
                    value={formData.comment}
                    onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="logTime"
                    label="Log Time"
                    name="logTime"
                    type="datetime-local"
                    value={formData.logTime}
                    onChange={handleChange}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="intime"
                    label="In Time"
                    name="intime"
                    type="time"
                    value={formData.intime}
                    onChange={handleChange}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                    margin="normal"
                    fullWidth
                    id="outtime"
                    label="Out Time"
                    name="outtime"
                    type="time"
                    value={formData.outtime}
                    onChange={handleChange}
                    InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="isAbsent"
                          checked={formData.isAbsent}
                          onChange={handleChange}
                      />
                    }
                    label="Absent"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="isUnSuccessfulAttdate"
                          checked={formData.isUnSuccessfulAttdate}
                          onChange={handleChange}
                      />
                    }
                    label="Unsuccessful Attendance"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="isHalfDay"
                          checked={formData.isHalfDay}
                          onChange={handleChange}
                      />
                    }
                    label="Half Day"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="unAuthorized"
                          checked={formData.unAuthorized}
                          onChange={handleChange}
                      />
                    }
                    label="Unauthorized"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="isLate"
                          checked={formData.isLate}
                          onChange={handleChange}
                      />
                    }
                    label="Late"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControlLabel
                    control={
                      <Checkbox
                          name="isLateCover"
                          checked={formData.isLateCover}
                          onChange={handleChange}
                      />
                    }
                    label="Late Cover"
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
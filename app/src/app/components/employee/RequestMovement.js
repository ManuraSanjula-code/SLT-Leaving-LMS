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
  FormControl,
  InputLabel,
  Select,
  Grid,
} from '@mui/material';

const RequestMovement = () => {
  const [userId, setUserId] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [showError, setShowError] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  const [formData, setFormData] = useState({
    employeeId: '',
    movementType: '',
    comment: '',
    destination: '',
    category: '',
    happenDate: '',
    isAbsent: false,
    isUnSuccessfulAttdate: false,
    isHalfDay: false,
    unAuthorized: false,
    isLate: false,        // Added new field
    isLateCover: false    // Added new field
  });

  useEffect(() => {
    // Get userId from sessionStorage
    const storedUserId = sessionStorage.getItem('userId');
    if (storedUserId) {
      setUserId(storedUserId);
      setFormData(prev => ({ ...prev, userId: storedUserId }));
    } else {
      setErrorMessage('User ID not found in local storage. Please login again.');
      setShowError(true);
    }
  }, []);

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const validate = () => {
    if (!formData.movementType) {
      setErrorMessage('Movement Type is required');
      return false;
    }
    if (!formData.happenDate) {
      setErrorMessage('Date is required');
      return false;
    }
    if (!formData.comment) {
      setErrorMessage('Comment/Reason is required');
      return false;
    }
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!userId) {
      setErrorMessage('User ID not found. Please login again.');
      setShowError(true);
      return;
    }

    if (!validate()) {
      setShowError(true);
      return;
    }

    // Convert date string to Date object for API
    const requestData = {
      ...formData,
      userId: userId,
      happenDate: formData.happenDate ? new Date(formData.happenDate) : null
    };

    try {
      const response = await fetch('http://localhost:8080/lms/management/movement/create', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      });

      if (response.ok) {
        setSuccessMessage('Movement request submitted successfully!');
        setShowSuccess(true);
        // Reset form after successful submission
        setFormData({
          employeeId: '',
          movementType: '',
          comment: '',
          destination: '',
          category: '',
          happenDate: '',
          isAbsent: false,
          isUnSuccessfulAttdate: false,
          isHalfDay: false,
          unAuthorized: false,
          isLate: false,        // Reset new field
          isLateCover: false    // Reset new field
        });
      } else {
        const errorData = await response.json();
        setErrorMessage(errorData.message || 'Failed to submit movement request');
        setShowError(true);
      }
    } catch (error) {
      setErrorMessage('Error submitting request: ' + error.message);
      setShowError(true);
    }
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
              {/* Added new checkboxes for isLate and isLateCover */}
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
            >
              Submit Movement Request
            </Button>
          </Box>
        </Box>

        <Snackbar
            open={showError}
            autoHideDuration={6000}
            onClose={() => setShowError(false)}
        >
          <Alert onClose={() => setShowError(false)} severity="error">
            {errorMessage}
          </Alert>
        </Snackbar>

        <Snackbar
            open={showSuccess}
            autoHideDuration={6000}
            onClose={() => setShowSuccess(false)}
        >
          <Alert onClose={() => setShowSuccess(false)} severity="success">
            {successMessage}
          </Alert>
        </Snackbar>
      </Container>
  );
};

export default RequestMovement;
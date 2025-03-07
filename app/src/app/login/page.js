"use client";

import React, { useState } from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  CssBaseline,
  Box,
  Avatar,
  Typography,
  TextField,
  Button,
  Grid,
  Link,
  Snackbar,
  Alert,
} from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import axios from "axios";
import { useRouter } from 'next/navigation'; 
import { setCredentials } from '../redux/authSlice'; // Replace with the correct path
const LoginPage = () => {
  const [openSnackbar, setOpenSnackbar] = useState(false); // State for Snackbar visibility
  const [snackbarMessage, setSnackbarMessage] = useState(""); // Message to display
  const [severity, setSeverity] = useState("success"); // Severity: "success" or "error"
  const router = useRouter();
  const dispatch = useDispatch();

  const handleSubmit = async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);

    // Extract email and password from the form
    const email = data.get("email");
    const password = data.get("password");

    try {
      // Make a POST request to the login API
      const response = await axios.post("http://localhost:8080/users/login", {
        email,
        password,
      }, { withCredentials: true });

      const token = response.headers['authentication']?.replace('Bearer ', '');
      const userId = response.headers['userid'];

      // Dispatch to Redux
      dispatch(setCredentials({
        jwt: token,
        userId: userId
      }));

      // Handle successful login
      console.log("Login successful:", response.data);
      setSnackbarMessage("Login successful!"); // Set success message
      setSeverity("success"); // Set severity to success
      setOpenSnackbar(true); // Show Snackbar

      const userDetailsRes = await axios.get(`http://localhost:8080/users/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
    
      // Dispatch user details with roles
      dispatch(setUserDetails(userDetailsRes.data)); 

      return router.push('/dashboard');
      
    } catch (error) {
      // Handle login error
      console.error("Login failed:", error.response ? error.response.data : error.message);
      setSnackbarMessage(
        error.response?.data?.message || "An error occurred. Please try again."
      ); // Set error message
      setSeverity("error"); // Set severity to error
      setOpenSnackbar(true); // Show Snackbar
    }
  };

  const handleCloseSnackbar = (event, reason) => {
    if (reason === "clickaway") {
      return;
    }
    setOpenSnackbar(false); // Close Snackbar
  };

  return (
    <Container component="main" maxWidth="xs">
      <CssBaseline />
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: "secondary.main" }}>
          <LockOutlinedIcon />
        </Avatar>
        <Typography component="h1" variant="h5">
          Sign in
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Email Address"
            name="email"
            autoComplete="email"
            autoFocus
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
          >
            Sign In
          </Button>
          <Grid container>
            <Grid item xs>
              <Link href="#" variant="body2">
                Forgot password?
              </Link>
            </Grid>
            <Grid item>
              <Link href="#" variant="body2">
                {"Don't have an account? Sign Up"}
              </Link>
            </Grid>
          </Grid>
        </Box>
      </Box>

      {/* Snackbar for displaying success/error messages */}
      <Snackbar
        open={openSnackbar}
        autoHideDuration={6000} // Auto-hide after 6 seconds
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "top", horizontal: "right" }} // Position of the Snackbar
      >
        <Alert onClose={handleCloseSnackbar} severity={severity} sx={{ width: "100%" }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default LoginPage;
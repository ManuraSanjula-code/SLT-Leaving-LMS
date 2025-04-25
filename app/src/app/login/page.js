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
import { useRouter, useSearchParams } from 'next/navigation';
import { setCredentials, setUserDetails } from '../redux-user/authSlice'; // Replace with the correct path

const LoginPage = () => {
  const [openSnackbar, setOpenSnackbar] = useState(false); // State for Snackbar visibility
  const [snackbarMessage, setSnackbarMessage] = useState(""); // Message to display
  const [severity, setSeverity] = useState("success"); // Severity: "success" or "error"
  const router = useRouter();
  const dispatch = useDispatch();

  const searchParams = useSearchParams();
  const isTempLogin = searchParams.get('temp') === 'true'; // Check if temp=true

  const handleSubmit = async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);    
    // Extract email and password from the form
    const email = data.get("email");
    const password = data.get("password");

    const loginUrl = isTempLogin 
        ? "http://localhost:8080/users/login/temp" // Temp endpoint
        : "http://localhost:8080/users/login"; // Regular endpoint

    try {
      // Make a POST request to the login API
      const response = await axios.post(loginUrl, {
        email,
        password,
      }, { withCredentials: true });

      // After successful login
      const authorizationHeader = response.headers['authorization']?.replace('Bearer ', '');
      const userId = response.headers['userid'];
      // First set local storage
      sessionStorage.setItem('userId', userId);
      sessionStorage.setItem('jwt', authorizationHeader);

      // Then dispatch credentials
      dispatch(setCredentials({
        jwt: authorizationHeader,
        userId: userId
      }));

      // Fetch user details
      try {
        const userDetailsRes = await axios.get(`http://localhost:8080/users/${userId}`, {
          headers: {
            Authorization: `Bearer ${authorizationHeader}`
          }
        });

        // Dispatch user details
        dispatch(setUserDetails(userDetailsRes.data));

        // Wait for state updates
        await new Promise(resolve => setTimeout(resolve, 50));

        // Redirect only after all state is updated
        router.push('/dashboard');
      } catch (error) {
        console.error("Failed to fetch user details:", error);
        handleLogout();
      }

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
            <Grid item>
              <Link href="/login?temp=true" variant="body2">
                {"Temp login ??"}
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
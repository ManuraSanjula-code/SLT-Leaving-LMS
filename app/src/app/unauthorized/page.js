"use client";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { useEffect } from "react";
import { Box, Typography, Button, Container, Paper, Grid } from "@mui/material";
import { LockOutlined } from "@mui/icons-material";
import NavBar from "@/app/components/navbar/NavBar";

export default function UnauthorizedPage() {
  const router = useRouter();
  const reduxUser = useSelector((state) => state.user); // Assuming user slice exists

  // Function to extract USER_ID from cookies
  const getUserIdFromCookie = () => {
    return document.cookie
      .split("; ")
      .find((row) => row.startsWith("USER_ID="))
      ?.split("=")[1];
  };

  useEffect(() => {
    // Check if user is logged in (Redux user exists)
    if (!reduxUser?.userId) {
      router.replace("/login");
      return;
    }

    // Check if cookie exists and matches Redux userId
    const userIdCookie = getUserIdFromCookie();
    if (!userIdCookie || userIdCookie !== reduxUser.userId) {
      router.replace("/login");
      return;
    }
  }, [reduxUser, router]);

  return (
    <>
      <NavBar />
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
          backgroundColor: "#f5f5f5",
        }}
      >
        <Container maxWidth="sm">
          <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
            <Grid container spacing={2} alignItems="center" direction="column">
              {/* Lock Icon */}
              <Grid item>
                <LockOutlined
                  sx={{
                    fontSize: 80,
                    color: "#ff5722",
                  }}
                />
              </Grid>

              {/* Title */}
              <Grid item>
                <Typography variant="h4" fontWeight="bold" align="center" gutterBottom>
                  Access Denied
                </Typography>
              </Grid>

              {/* Description */}
              <Grid item>
                <Typography variant="body1" align="center" color="textSecondary" gutterBottom>
                  You don&apos;t have permission to view this page. Please contact support if you believe this is an error.
                </Typography>
              </Grid>

              {/* Dashboard Button */}
              <Grid item>
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  onClick={() => {
                    window.location.href = "/login";
                    router.push("/login")
                  }} // Updated to navigate to login page
                  sx={{
                    mt: 2,
                    px: 4,
                    py: 1.5,
                    borderRadius: 50,
                    textTransform: "none",
                    fontSize: 16,
                  }}
                >
                  Return to Login
                </Button>
              </Grid>
            </Grid>
          </Paper>
        </Container>
      </Box>
    </>
  );
}
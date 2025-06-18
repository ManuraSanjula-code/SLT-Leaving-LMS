"use client";

import { Box, Typography, Button } from "@mui/material";

export default function NetworkErrorPage() {
  const handleRetry = () => {
    window.location.reload();
  };

  return (
      <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          minHeight="100vh"
          textAlign="center"
          p={3}
      >
        <Typography variant="h4" component="h1" gutterBottom>
          Network Error
        </Typography>
        <Typography variant="body1" color="text.secondary" mb={3}>
          Please check your internet connection and try again.
        </Typography>
        <Button
            variant="contained"
            color="primary"
            onClick={handleRetry}
            size="large"
        >
          Retry
        </Button>
      </Box>
  );
}
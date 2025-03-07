// src/pages/NetworkErrorPage.js
import { Box, Typography, Button } from "@mui/material";

export default function NetworkErrorPage() {
  return (
    <Box
      display="flex"
      flexDirection="column"
      justifyContent="center"
      alignItems="center"
      height="100vh"
      textAlign="center"
    >
      <Typography variant="h4" gutterBottom>
        Network Error
      </Typography>
      <Typography variant="body1" gutterBottom>
        Please check your internet connection and try again.
      </Typography>
      <Button
        variant="contained"
        color="primary"
        onClick={() => window.location.reload()} // Reload the page
      >
        Retry
      </Button>
    </Box>
  );
}
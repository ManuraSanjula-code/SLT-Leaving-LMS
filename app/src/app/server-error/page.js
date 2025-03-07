import { Box, Typography, Button } from "@mui/material";

export default function ServerErrorPage() {
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
        Server Unavailable
      </Typography>
      <Typography variant="body1" gutterBottom>
        We're sorry, but the server is currently unavailable. Please check your internet connection or try again later.
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
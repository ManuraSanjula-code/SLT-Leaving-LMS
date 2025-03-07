// src/pages/NotFoundPage.js
import { Box, Typography, Button } from "@mui/material";
import { useRouter } from "next/navigation";
import NavBar from "@/app/components/navbar/NavBar";

export default function NotFoundPage() {
  const router = useRouter();

  return (
    <>
      <NavBar />
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        height="100vh"
        textAlign="center"
      >
        <Typography variant="h4" gutterBottom>
          404 - Page Not Found
        </Typography>
        <Typography variant="body1" gutterBottom>
          The page you're looking for doesn't exist or has been moved.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => router.push("/")} // Redirect to home page
        >
          Go to Home
        </Button>
      </Box>
    </>
  );
}
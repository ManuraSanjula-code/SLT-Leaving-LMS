// app/not-found.js
"use client"; // Ensure this is a client component
import { Card, CardContent, Typography, Button, Box } from "@mui/material";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import { useRouter } from "next/navigation";

export default function NotFound() {
  const router = useRouter();

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
        backgroundColor: "#f4f6f8", // Light gray background
        padding: 2,
      }}
    >
      <Card
        sx={{
          maxWidth: 450,
          textAlign: "center",
          padding: 3,
          boxShadow: 3,
          backgroundColor: "#fff", // White background
          borderRadius: "12px",
        }}
      >
        <CardContent>
          <ErrorOutlineIcon color="error" sx={{ fontSize: 70, marginBottom: 2 }} />
          <Typography variant="h4" fontWeight="bold" color="error">
            404 - Page Not Found
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ marginY: 1 }}>
            The page you're looking for doesn't exist.
          </Typography>
          <Button
            variant="contained"
            color="primary"
            sx={{ marginTop: 2, borderRadius: "8px" }}
            onClick={() => router.push("/")}
          >
            Go to Home
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
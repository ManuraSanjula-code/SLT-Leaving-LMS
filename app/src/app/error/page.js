"use client";

import { Container, Typography, Button, Box } from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import { useRouter } from "next/navigation";
import NavBar from "@/app/components/navbar/NavBar";

export default function ErrorPage() {
  const router = useRouter();

  return (
    <>
      <NavBar />

      <Box sx={{
        minHeight: '100vh',
        backgroundColor: 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 3
      }}>
        <Container maxWidth="md" sx={{
          textAlign: 'center',
          backgroundColor: 'white'
        }}>
          {/* Centered Image Container */}
          <Box sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            mb: 4,
            width: '100%'
          }}>
            <img
              src="https://media.giphy.com/media/3o7TKsrf0g3VCDnkZq/giphy.gif"
              alt="Error animation"
              style={{
                display: 'block',
                width: '100%',
                maxWidth: '500px',
                height: 'auto',
                borderRadius: '16px',
                margin: '0 auto'
              }}
            />
          </Box>

          {/* Centered Text Content */}
          <Box sx={{
            maxWidth: 600,
            margin: '0 auto'
          }}>
            <Typography variant="h3" component="h1" gutterBottom sx={{
              fontWeight: 700,
              color: 'text.primary',
              mb: 2
            }}>
              Application Error
            </Typography>

            <Typography variant="h5" component="h2" gutterBottom sx={{
              fontWeight: 500,
              color: 'text.secondary',
              mb: 3
            }}>
              Something Went Wrong
            </Typography>

            <Typography variant="body1" sx={{
              color: 'text.secondary',
              mb: 4
            }}>
              We encountered an unexpected issue. Our team has been notified and is working to resolve it.
            </Typography>

            {/* Centered Button */}
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Button
                variant="contained"
                color="primary"
                size="large"
                startIcon={<DashboardIcon />}
                onClick={() => router.push('/dashboard')}
                sx={{
                  px: 5,
                  py: 1.5,
                  borderRadius: '8px',
                  textTransform: 'none',
                  fontSize: '1.1rem',
                  boxShadow: 'none',
                  '&:hover': {
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                  }
                }}
              >
                Return to Dashboard
              </Button>
            </Box>
          </Box>
        </Container>
      </Box>
    </>
  );
}
"use client";
import React from "react";
import { Box, Typography, Container } from "@mui/material";

const Footer = () => {
    return (
        <Box
            component="footer"
            sx={{
                backgroundColor: "#f5f5f5",
                padding: "20px 0",
                textAlign: "center",
                width: "100%",
                mt: "auto", // Pushes the footer to the bottom
            }}
        >
            <Container>
                <Typography variant="body2" color="textSecondary">
                    © {new Date().getFullYear()} Your Company Name. All rights reserved.
                </Typography>
                <Typography variant="body2" color="textSecondary">
                    Made with ❤️ by Your Team
                </Typography>
            </Container>
        </Box>
    );
};

export default Footer;
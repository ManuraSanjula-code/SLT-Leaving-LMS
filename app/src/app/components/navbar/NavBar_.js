"use client";

import React, { useState } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Tabs,
  Tab,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemText,
  useMediaQuery,
  useTheme,
  Box,
  Collapse,
  ListItemIcon,
} from "@mui/material";
import { useRouter, usePathname } from "next/navigation"; // Use Next.js's useRouter and usePathname
import MenuIcon from "@mui/icons-material/Menu";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"; // Icon for expand/collapse
import ExpandLessIcon from "@mui/icons-material/ExpandLess"; // Icon for expand/collapse
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings"; // Icon for admin items

const NavBar = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm")); // Check if the screen is small
  const router = useRouter(); // Use Next.js's useRouter
  const pathname = usePathname(); // Use Next.js's usePathname to get the current route
  const [sidebarOpen, setSidebarOpen] = useState(false); // State for the sidebar
  const [employeeCollapseOpen, setEmployeeCollapseOpen] = useState(false); // State for the employee collapsible section
  const [adminCollapseOpen, setAdminCollapseOpen] = useState(false); // State for the admin collapsible section

  // Navigation items
  const navItems = [
    { label: "Dashboard", path: "/dashboard" },
    { label: "Apply Leave", path: "/apply-leave" },
    { label: "Request Movement", path: "/request-movement" },
    { label: "Profile", path: "/profile" },
  ];

  // Employee-related items
  const employeeItems = [
    { label: "All Leaves", path: "/all-leaves" },
    { label: "All Movements Requests", path: "/all-movements" },
    { label: "Unsuccessful Leaves", path: "/unsuccessful-leaves" },
    { label: "Unauthorized Leaves", path: "/unauthorized-leaves" },
    { label: "Absent Employees", path: "/absent-all-employees" },
    { label: "No-Pay Leaves", path: "/no-pay-leaves" },
    { label: "Single Employee Activities", path: "/single-employee-activities" },
  ];

  // Admin items
  const adminItems = [
    { label: "Manage Employees", path: "/manage-employees" },
    { label: "Manage Leave Requests", path: "/manage-leave-requests" },
    { label: "Manage Movement Requests", path: "/manage-movement-requests" },
    { label: 'All Employee Activities', path: '/employee-activities' },

    { label: "All Leaves", path: "/all-leaves" },
    { label: "Unsuccessful Leaves", path: "/unsuccessful-leaves" },
    { label: "Unauthorized Leaves", path: "/unauthorized-leaves" },
    { label: "Absent Employees", path: "/absent-employees" },
    { label: "No-Pay Leaves", path: "/no-pay-leaves" },
  ];

  // Handle opening the sidebar
  const handleSidebarOpen = () => {
    setSidebarOpen(true);
  };

  // Handle closing the sidebar
  const handleSidebarClose = () => {
    setSidebarOpen(false);
  };

  // Handle toggling the employee collapsible section
  const handleEmployeeCollapseToggle = () => {
    setEmployeeCollapseOpen(!employeeCollapseOpen);
  };

  // Handle toggling the admin collapsible section
  const handleAdminCollapseToggle = () => {
    setAdminCollapseOpen(!adminCollapseOpen);
  };

  // Handle tab navigation
  const handleTabChange = (path) => {
    router.push(path); // Use Next.js's router.push for client-side navigation
  };

  return (
      <>
        {/* App Bar */}
        <AppBar position="static">
          <Toolbar>
            {/* App Title */}
            <Typography variant="h6" sx={{ flexGrow: 1 }}>
              {/* Leave Management System */}
            </Typography>

            {/* Navigation Tabs */}
            <Tabs
                value={pathname} // Highlight the current tab based on the route
                textColor="inherit"
                variant={isMobile ? "scrollable" : "standard"} // Make tabs scrollable on small screens
                scrollButtons="auto" // Show scroll buttons if tabs overflow
            >
              {navItems.map((item) => (
                  <Tab
                      key={item.path}
                      label={item.label}
                      value={item.path}
                      onClick={() => handleTabChange(item.path)} // Use handleTabChange for navigation
                  />
              ))}
            </Tabs>

            {/* Sidebar Toggle Button */}
            <IconButton
                color="inherit"
                onClick={handleSidebarOpen}
                sx={{ ml: 2 }} // Add margin to separate from other tabs
            >
              <MenuIcon /> {/* Icon for the sidebar */}
            </IconButton>
          </Toolbar>
        </AppBar>

        {/* Sidebar */}
        <Drawer
            anchor="right" // Open the sidebar from the right
            open={sidebarOpen}
            onClose={handleSidebarClose}
        >
          <Box sx={{ width: 350 }} role="presentation">
            <List>
              {/* Employee Items in Sidebar */}
              <ListItem button onClick={handleEmployeeCollapseToggle}>
                <ListItemText primary="Employee" />
                {employeeCollapseOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </ListItem>
              <Collapse in={employeeCollapseOpen}>
                <List>
                  {employeeItems.map((item) => (
                      <ListItem
                          button
                          key={item.path}
                          onClick={() => {
                            handleTabChange(item.path); // Use handleTabChange for navigation
                            handleSidebarClose();
                          }}
                          sx={{ pl: 4 }} // Add padding for indentation
                      >
                        <ListItemText primary={item.label} />
                      </ListItem>
                  ))}
                </List>
              </Collapse>

              {/* Admin Items in Sidebar */}
              <ListItem button onClick={handleAdminCollapseToggle}>
                <ListItemText primary="Admin" />
                {adminCollapseOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </ListItem>
              <Collapse in={adminCollapseOpen}>
                <List>
                  {adminItems.map((item) => (
                      <ListItem
                          button
                          key={item.path}
                          onClick={() => {
                            handleTabChange(item.path); // Use handleTabChange for navigation
                            handleSidebarClose();
                          }}
                          sx={{ pl: 4 }} // Add padding for indentation
                      >
                        <ListItemIcon>
                          <AdminPanelSettingsIcon />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                      </ListItem>
                  ))}
                </List>
              </Collapse>
            </List>
          </Box>
        </Drawer>
      </>
  );
};

export default NavBar;
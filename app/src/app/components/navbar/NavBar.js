"use client";

import React, { useState, useMemo } from "react";
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
import { useRouter, usePathname } from "next/navigation";
import { useSelector } from "react-redux";
import MenuIcon from "@mui/icons-material/Menu";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
// import { roleAccessRules } from "@/path/to/your/access-rules"; // Update import path

const roleAccessRules = (() => {
  const ROLE_EMPLOYEE = [
    "/dashboard",
    "/apply-leave",
    "/request-movement",
    "/profile",
    "/all-leaves",
    "/all-movements",
    "/single-employee-activities",
    "/"
  ];

  return {
    ROLE_EMPLOYEE,
    ROLE_HOD: [...ROLE_EMPLOYEE, "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    ROLE_SUPERVISOR: [...ROLE_EMPLOYEE, "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    ROLE_ADMIN: [...ROLE_EMPLOYEE, "/manage-employees"],
    ROLE_CEO: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
    ROLE_CHAIRMAN: [...ROLE_EMPLOYEE, "/employee-activities", "/absent-employees", "/no-pay-leaves", "/manage-leave-requests", "/manage-movement-requests", "/unsuccessful-leaves", "/unauthorized-leaves"],
  };
})();

const NavBar = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const router = useRouter();
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [employeeCollapseOpen, setEmployeeCollapseOpen] = useState(false);
  const [adminCollapseOpen, setAdminCollapseOpen] = useState(false);

  // Get user from Redux
  const reduxUser = useSelector((state) => state.auth);
  const roles = reduxUser.userDetails?.roles || [];

  // Calculate allowed routes based on user roles
  const allowedRoutes = useMemo(() => {
    const routes = new Set();
    roles.forEach(role => {
      const roleRoutes = roleAccessRules[role];
      if (roleRoutes) roleRoutes.forEach(path => routes.add(path));
    });
    return routes;
  }, [roles]);

  // Navigation items with access check
  const navItems = useMemo(() => [
    { label: "Dashboard", path: "/dashboard" },
    { label: "Apply Leave", path: "/apply-leave" },
    { label: "Request Movement", path: "/request-movement" },
    { label: "Profile", path: "/profile" },
  ].filter(item => allowedRoutes.has(item.path)), [allowedRoutes]);

  // Employee-related items with access check
  const employeeItems = useMemo(() => [
    { label: "All Leaves", path: "/all-leaves" },
    { label: "All Movements Requests", path: "/all-movements" },
    { label: "Unsuccessful Leaves", path: "/unsuccessful-leaves" },
    { label: "Unauthorized Leaves", path: "/unauthorized-leaves" },
    { label: "Absent Employees", path: "/absent-all-employees" },
    { label: "No-Pay Leaves", path: "/no-pay-leaves" },
    { label: "Single Employee Activities", path: "/single-employee-activities" },
  ].filter(item => allowedRoutes.has(item.path)), [allowedRoutes]);

  // Admin items with access check
  const adminItems = useMemo(() => [
    { label: "Manage Employees", path: "/manage-employees" },
    { label: "Manage Leave Requests", path: "/manage-leave-requests" },
    { label: "Manage Movement Requests", path: "/manage-movement-requests" },
    { label: 'All Employee Activities', path: '/employee-activities' },
    { label: "All Leaves", path: "/all-leaves" },
    { label: "Unsuccessful Leaves", path: "/unsuccessful-leaves" },
    { label: "Unauthorized Leaves", path: "/unauthorized-leaves" },
    { label: "Absent Employees", path: "/absent-employees" },
    { label: "No-Pay Leaves", path: "/no-pay-leaves" },
  ].filter(item => allowedRoutes.has(item.path)), [allowedRoutes]);

  // Handlers remain the same
  const handleSidebarOpen = () => setSidebarOpen(true);
  const handleSidebarClose = () => setSidebarOpen(false);
  const handleEmployeeCollapseToggle = () => setEmployeeCollapseOpen(!employeeCollapseOpen);
  const handleAdminCollapseToggle = () => setAdminCollapseOpen(!adminCollapseOpen);
  const handleTabChange = (path) => router.push(path);

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }} />
          <Tabs
            value={pathname}
            textColor="inherit"
            variant={isMobile ? "scrollable" : "standard"}
            scrollButtons="auto"
          >
            {navItems.map((item) => (
              <Tab
                key={item.path}
                label={item.label}
                value={item.path}
                onClick={() => handleTabChange(item.path)}
              />
            ))}
          </Tabs>
          <IconButton color="inherit" onClick={handleSidebarOpen} sx={{ ml: 2 }}>
            <MenuIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Drawer anchor="right" open={sidebarOpen} onClose={handleSidebarClose}>
        <Box sx={{ width: 350 }}>
          <List>
            {employeeItems.length > 0 && (
              <>
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
                          handleTabChange(item.path);
                          handleSidebarClose();
                        }}
                        sx={{ pl: 4 }}
                      >
                        <ListItemText primary={item.label} />
                      </ListItem>
                    ))}
                  </List>
                </Collapse>
              </>
            )}

            {adminItems.length > 0 && (
              <>
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
                          handleTabChange(item.path);
                          handleSidebarClose();
                        }}
                        sx={{ pl: 4 }}
                      >
                        <ListItemIcon>
                          <AdminPanelSettingsIcon />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                      </ListItem>
                    ))}
                  </List>
                </Collapse>
              </>
            )}
          </List>
        </Box>
      </Drawer>
    </>
  );
};

export default NavBar;
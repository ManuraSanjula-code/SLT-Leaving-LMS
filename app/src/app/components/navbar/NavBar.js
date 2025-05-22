import React, { useState, useMemo, useCallback } from "react";
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
import { getAccessibleRoutes } from "../../priorityRoutes";

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
  const userPriority = reduxUser.userDetails?.highestRolePriority || 200;

  // Memoize all route data
  const { allNavItems, employeeItems, adminItems } = useMemo(() => {
    const allowedRoutes = getAccessibleRoutes(userPriority);

    const allItems = [
      { label: "Dashboard", path: "/dashboard" },
      { label: "Apply Leave", path: "/apply-leave" },
      { label: "Request Movement", path: "/request-movement" },
      { label: "Profile", path: "/profile" },
    ].filter(item => allowedRoutes.has(item.path));

    const empItems = [
      { label: "Your Leaves", path: "/all-leaves" },
      { label: "Your Movements", path: "/all-movements" },
      { label: "Your Activities", path: "/single-employee-activities" },
      { label: "Your Absents", path: "/absent-employees" },
      { label: "Your No-Pay's", path: "/no-pay-leaves" },
      { label: "Your Unsuccessful Leaves", path: "/unsuccessful-leaves" },
      { label: "Your Unauthorized Leaves", path: "/unauthorized-leaves" },
      { label: "In-Outs", path: "/in-outs" },

    ].filter(item => allowedRoutes.has(item.path));

    const admItems = [
      { label: "Manage Employees-Admin", path: "/manage-employees" },
      { label: "Employee Activities-Admin", path: "/employee-activities" },
      { label: "Manage Leave Requests-Admin", path: "/manage-leave-requests" },
      { label: "Manage Movement Requests-Admin", path: "/manage-movement-requests" },
      { label: "Unsuccessful Leaves-Admin", path: "/unsuccessful-leaves-admin" },
      { label: "Unauthorized Leaves-Admin", path: "/unauthorized-leaves-admin" },
      { label: "No Pay-Admin", path: "/no-pay-leaves-admin" },
      { label: "Absent Employees-Admin", path: "/absent-employees-admin" },
      { label: "Roster All Attendance ", path: "/roster/all-attendance" },
      { label: "Get All Roster ", path: "/roster/all" },
      { label: "Roster Detail", path: "/roster/detail" },
      { label: "Other", path: "/other" },

    ].filter(item => allowedRoutes.has(item.path));

    return { allNavItems: allItems, employeeItems: empItems, adminItems: admItems };
  }, [userPriority]);

  // Memoize handlers
  const handleSidebarOpen = useCallback(() => setSidebarOpen(true), []);
  const handleSidebarClose = useCallback(() => setSidebarOpen(false), []);
  const handleEmployeeCollapseToggle = useCallback(() => setEmployeeCollapseOpen(prev => !prev), []);
  const handleAdminCollapseToggle = useCallback(() => setAdminCollapseOpen(prev => !prev), []);

  // Optimized navigation handler
  const handleNavigation = useCallback((path) => {
    // Close drawer immediately for better perceived performance
    setSidebarOpen(false);

    // Check if we're already on this page
    if (pathname !== path) {
      router.push(path);
    }
  }, [pathname, router]);

  return (
      <>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" sx={{ flexGrow: 1 }} />
            {!isMobile && (
                <Tabs
                    value={pathname}
                    textColor="inherit"
                    indicatorColor="secondary"
                >
                  {allNavItems.map((item) => (
                      <Tab
                          key={item.path}
                          label={item.label}
                          value={item.path}
                          onClick={() => handleNavigation(item.path)}
                          sx={{
                            minWidth: 'unset',
                            padding: '6px 12px',
                            '&.Mui-selected': {
                              color: 'inherit',
                            },
                          }}
                      />
                  ))}
                </Tabs>
            )}
            <IconButton
                color="inherit"
                onClick={handleSidebarOpen}
                sx={{ ml: 2 }}
                aria-label="Open menu"
            >
              <MenuIcon />
            </IconButton>
          </Toolbar>
        </AppBar>

        <Drawer
            anchor="right"
            open={sidebarOpen}
            onClose={handleSidebarClose}
            ModalProps={{
              keepMounted: true,
            }}
        >
          <Box sx={{ width: 400, paddingTop: 2 }}>
            <List>
              {/* Only show main navigation items in drawer on mobile */}
              {isMobile && allNavItems.map((item) => (
                  <ListItem
                      button
                      key={item.path}
                      onClick={() => handleNavigation(item.path)}
                      selected={pathname === item.path}
                      sx={{
                        '&.Mui-selected': {
                          backgroundColor: theme.palette.action.selected,
                        },
                      }}
                  >
                    <ListItemText primary={item.label} />
                  </ListItem>
              ))}

              {/* Employee section - always in drawer regardless of screen size */}
              {employeeItems.length > 0 && (
                  <>
                    <ListItem
                        button
                        onClick={handleEmployeeCollapseToggle}
                        sx={{
                          '&:hover': {
                            backgroundColor: 'transparent',
                          },
                        }}
                    >
                      <ListItemText primary="Employee" />
                      {employeeCollapseOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                    </ListItem>
                    <Collapse in={employeeCollapseOpen} timeout="auto" unmountOnExit>
                      <List disablePadding>
                        {employeeItems.map((item) => (
                            <ListItem
                                button
                                key={item.path}
                                onClick={() => handleNavigation(item.path)}
                                selected={pathname === item.path}
                                sx={{ pl: 4 }}
                            >
                              <ListItemText primary={item.label} />
                            </ListItem>
                        ))}
                      </List>
                    </Collapse>
                  </>
              )}

              {/* Admin section - always in drawer regardless of screen size */}
              {adminItems.length > 0 && (
                  <>
                    <ListItem
                        button
                        onClick={handleAdminCollapseToggle}
                        sx={{
                          '&:hover': {
                            backgroundColor: 'transparent',
                          },
                        }}
                    >
                      <ListItemText primary="System" />
                      {adminCollapseOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                    </ListItem>
                    <Collapse in={adminCollapseOpen} timeout="auto" unmountOnExit>
                      <List disablePadding>
                        {adminItems.map((item) => (
                            <ListItem
                                button
                                key={item.path}
                                onClick={() => handleNavigation(item.path)}
                                selected={pathname === item.path}
                                sx={{ pl: 4 }}
                            >
                              <ListItemIcon sx={{ minWidth: '36px' }}>
                                <AdminPanelSettingsIcon fontSize="small" />
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
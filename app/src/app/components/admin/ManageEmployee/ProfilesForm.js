import React, { useState, useEffect, useRef } from "react";
import {
  TextField,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Checkbox,
  ListItemIcon,
  Typography,
  Pagination,
  Box,
  InputAdornment,
} from "@mui/material";
import { Delete, Edit, Search } from "@mui/icons-material";
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const ProfilesForm = () => {
  const [profiles, setProfiles] = useState([]);
  const [users, setUsers] = useState([]); // All users from the server
  const [openDialog, setOpenDialog] = useState(false);
  const [currentProfile, setCurrentProfile] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    publicId: "",
    workStart: "",
    workEnds: "",
    ignoreSl: "",
    gracePeriodeStart: "",
    hdStart: "",
    slStartMorning: "",
    slStartEvening: "",
    possibleFpLocations: "",
    defaultHrs: "",
    hdHrs: "",
    minHrsForSl: "",
    shortLeaveCount: "",
    hdEndsMorning: "",
    flexiDays: "",
    flexiHrsStart: "",
    addedUsers: [],
    deletedUsers: [],
  });
  const [selectedUsers, setSelectedUsers] = useState([]); // Users selected for the profile
  const [deletedUsers, setDeletedUsers] = useState([]); // Users to be removed from the profile
  const [newUserId, setNewUserId] = useState(""); // Input for adding a new user by ID
  const [userPage, setUserPage] = useState(1); // Pagination for assigned users
  const [searchQuery, setSearchQuery] = useState(""); // Search query for filtering users
  const usersPerPage = 20; // Number of users to display per page
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const resolveRef = useRef(null); // Declare the ref

  // Fetch profiles from the server on component mount
  useEffect(() => {
    const fetchProfiles = async () => {
      try {
        const response = await fetch("http://localhost:8080/users/profile", {
          method: "GET",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        setProfiles(data); // Set profiles fetched from the server
      } catch (err) {
        console.error("Error fetching profiles:", err);
      }
    };

    fetchProfiles();
  }, []);

  // Open dialog for adding or editing a profile
  const handleOpenDialog = (profile = null) => {
    setCurrentProfile(profile);
    setFormData(
      profile || {
        name: "",
        publicId: "",
        workStart: "",
        workEnds: "",
        ignoreSl: "",
        gracePeriodeStart: "",
        hdStart: "",
        slStartMorning: "",
        slStartEvening: "",
        possibleFpLocations: "",
        defaultHrs: "",
        hdHrs: "",
        minHrsForSl: "",
        shortLeaveCount: "",
        hdEndsMorning: "",
        flexiDays: "",
        flexiHrsStart: "",
        addedUsers: [],
        deletedUsers: [],
      }
    );

    // Initialize selectedUsers and deletedUsers based on the current profile's users
    if (profile) {
      const profileUserIds = profile.users.map((user) => user.id);
      setSelectedUsers(profileUserIds);
      setDeletedUsers([]); // Reset deletedUsers when opening the dialog
    } else {
      setSelectedUsers([]);
      setDeletedUsers([]);
    }

    setOpenDialog(true);
  };

  // Close dialog
  const handleCloseDialog = () => {
    setOpenDialog(false);
    setCurrentProfile(null);
    setFormData({
      name: "",
      publicId: "",
      workStart: "",
      workEnds: "",
      ignoreSl: "",
      gracePeriodeStart: "",
      hdStart: "",
      slStartMorning: "",
      slStartEvening: "",
      possibleFpLocations: "",
      defaultHrs: "",
      hdHrs: "",
      minHrsForSl: "",
      shortLeaveCount: "",
      hdEndsMorning: "",
      flexiDays: "",
      flexiHrsStart: "",
      addedUsers: [],
      deletedUsers: [],
    });
    setSelectedUsers([]);
    setDeletedUsers([]);
    setNewUserId("");
    setUserPage(1); // Reset pagination
    setSearchQuery(""); // Reset search query
  };

  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // Handle user selection for the profile
  const handleUserSelection = (userId) => {
    if (selectedUsers.includes(userId)) {
      // User is being unchecked, add to deletedUsers
      setSelectedUsers((prev) => prev.filter((id) => id !== userId));
      setDeletedUsers((prev) => [...prev, userId]);
    } else {
      // User is being checked, remove from deletedUsers if present
      setSelectedUsers((prev) => [...prev, userId]);
      setDeletedUsers((prev) => prev.filter((id) => id !== userId));
    }
  };

  const handleSuccessClose = () => {
    setSuccessOpen(false);
    if (resolveRef.current) {
      resolveRef.current(); // Resolve the Promise
      resolveRef.current = null; // Clear the ref
    }
  };

  const handleErrorOpen = () => {
    setErrorOpen(true);
  };

  const handleErrorClose = () => {
    setErrorOpen(false);
    if (resolveRef.current) {
      resolveRef.current(); // Resolve the Promise
      resolveRef.current = null; // Clear the ref
    }
  };

  const showSuccessDialog = () => {
    return new Promise((resolve) => {
      resolveRef.current = resolve; // Store the resolve function
      handleSuccessOpen(); // Open the dialog
    });
  };

  const showErrorDialog = () => {
    return new Promise((resolve) => {
      resolveRef.current = resolve; // Store the resolve function
      handleErrorOpen(); // Open the dialog
    });
  };

  const handleSuccessOpen = () => {
    setSuccessOpen(true);
  };

  // Save or update profile
  const saveProfile = async (profileData) => {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      console.error("User ID not found in localStorage");
      return;
    }

    const url = `http://localhost:8080/users/profile/${userId}`;
    const method = profileData.id ? "PUT" : "POST"; // Use PUT for update, POST for create

    try {
      const response = await fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(profileData),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data; // Return the saved/updated profile
    } catch (err) {
      console.error("Error saving profile:", err);
    }
  };

  // Handle form submission (add or update profile)
  const handleSubmit = async () => {
    const updatedProfile = {
      ...formData,
      users: selectedUsers.map((id) => users.find((user) => user.id === id)),
      addedUsers: selectedUsers
        .filter((id) => !currentProfile?.users.some((user) => user.id === id))
        .map((id) => users.find((user) => user.id === id).userId),
      deletedUsers: deletedUsers.map((id) => users.find((user) => user.id === id).userId),
    };

    try {
      const savedProfile = await saveProfile(updatedProfile);

      if (currentProfile) {
        await showSuccessDialog();
        // Update existing profile in the state
        setProfiles((prevProfiles) =>
          prevProfiles.map((profile) =>
            profile.id === currentProfile.id ? { ...profile, ...savedProfile } : profile
          )
        );
      } else {
        // Add new profile to the state
        setProfiles((prevProfiles) => [...prevProfiles, savedProfile]);
      }

      handleCloseDialog();
    } catch (err) {
      await showErrorDialog()
      console.error("Error handling profile submission:", err);
    }
  };

  // Handle deleting a profile
  const handleDelete = async (id) => {
    try {
      const userId = localStorage.getItem("userId");
      if (!userId) {
        console.error("User ID not found in localStorage");
        return;
      }

      const response = await fetch(`http://localhost:8080/users/profile/${id}/${userId}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // If the delete request is successful, update the state
      setProfiles((prevProfiles) => prevProfiles.filter((profile) => profile.id !== id));
    } catch (err) {
      console.error("Error deleting profile:", err);
    }
  };

  // Filter users based on search query
  const filteredUsers = users.filter(
    (user) =>
      user.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Pagination logic for assigned users
  const handleUserPageChange = (event, value) => {
    setUserPage(value);
  };

  // Reset pagination to page 1 when search query changes
  useEffect(() => {
    setUserPage(1);
  }, [searchQuery]);

  const paginatedUsers = filteredUsers.slice(
    (userPage - 1) * usersPerPage,
    userPage * usersPerPage
  );

  return (
    <>
      <SuccessDialog
        open={successOpen}
        onClose={handleSuccessClose}
        title="Success!"
        message="Your action was completed successfully."
      />

      {/* Error Dialog */}
      <ErrorDialog
        open={errorOpen}
        onClose={handleErrorClose}
        title="Oops! Something Went Wrong"
        message="There was an error processing your request. Please try again."
      />
      <div>
        <Button variant="contained" onClick={() => handleOpenDialog()}>
          Add Profile
        </Button>

        <List>
          {profiles.map((profile) => (
            <ListItem key={profile.id}>
              <ListItemText primary={profile.name} secondary={`Public ID: ${profile.publicId}`} />
              <IconButton onClick={() => handleOpenDialog(profile)}>
                <Edit />
              </IconButton>
              <IconButton onClick={() => handleDelete(profile.id)}>
                <Delete />
              </IconButton>
            </ListItem>
          ))}
        </List>

        <Dialog open={openDialog} onClose={handleCloseDialog} fullWidth maxWidth="md">
          <DialogTitle>{currentProfile ? "Edit Profile" : "Add Profile"}</DialogTitle>
          <DialogContent>
            {/* Profile Details Section */}
            <Typography variant="h6" gutterBottom>
              Profile Details
            </Typography>
            <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2 }}>
              <TextField
                label="Name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Public ID"
                name="public_id"
                value={formData.publicId}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Work Start"
                name="workStart"
                value={formData.workStart}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Work Ends"
                name="workEnds"
                value={formData.workEnds}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Ignore SL"
                name="ignoreSl"
                value={formData.ignoreSl}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Grace Period Start"
                name="gracePeriodeStart"
                value={formData.gracePeriodeStart}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="HD Start"
                name="hdStart"
                value={formData.hdStart}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="SL Start Morning"
                name="slStartMorning"
                value={formData.slStartMorning}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="SL Start Evening"
                name="slStartEvening"
                value={formData.slStartEvening}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Possible FP Locations"
                name="possibleFpLocations"
                value={formData.possibleFpLocations}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Default Hours"
                name="defaultHrs"
                value={formData.defaultHrs}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="HD Hours"
                name="hdHrs"
                value={formData.hdHrs}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Min Hours for SL"
                name="minHrsForSl"
                value={formData.minHrsForSl}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Short Leave Count"
                name="shortLeaveCount"
                value={formData.shortLeaveCount}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="HD Ends Morning"
                name="hdEndsMorning"
                value={formData.hdEndsMorning}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Flexi Days"
                name="flexiDays"
                value={formData.flexiDays}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Flexi Hours Start"
                name="flexiHrsStart"
                value={formData.flexiHrsStart}
                onChange={handleChange}
                fullWidth
                margin="normal"
              />
            </Box>

            {/* Assigned Users Section */}
            <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
              Assigned Users
            </Typography>
            <TextField
              label="Search Users"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              fullWidth
              margin="normal"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
            />
            <Box sx={{ maxHeight: "300px", overflowY: "auto" }}>
              <List>
                {paginatedUsers.map((user) => (
                  <ListItem key={user.id}>
                    <ListItemIcon>
                      <Checkbox
                        checked={selectedUsers.includes(user.id)}
                        onChange={() => handleUserSelection(user.id)}
                      />
                    </ListItemIcon>
                    <ListItemText
                      primary={`${user.firstName} ${user.lastName} (${user.email})`}
                    />
                  </ListItem>
                ))}
              </List>
              <Pagination
                count={Math.ceil(filteredUsers.length / usersPerPage)}
                page={userPage}
                onChange={handleUserPageChange}
                sx={{ mt: 2, display: "flex", justifyContent: "center" }}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button onClick={handleSubmit} color="primary">
              {currentProfile ? "Update" : "Add"}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    </>
  );
};

export default ProfilesForm;
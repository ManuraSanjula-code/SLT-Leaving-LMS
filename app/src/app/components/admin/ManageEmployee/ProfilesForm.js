// components/management/ProfilesForm.js
'use client';

import React, { useState, useEffect, useCallback, useMemo } from "react";
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
import { useDispatch, useSelector } from 'react-redux';
import { fetchManagementData, saveProfile, deleteProfile } from '../../../store/managementSlice';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const ProfilesForm = ({ onSubmit }) => {
  const dispatch = useDispatch();
  const { data, loading, error, saveLoading, saveError, saveSuccess } = useSelector(state => state.management);

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
    users: []
  });
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [deletedUsers, setDeletedUsers] = useState([]);
  const [userPage, setUserPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const [formErrors, setFormErrors] = useState({});
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);

  const usersPerPage = 10;

  useEffect(() => {
    dispatch(fetchManagementData());
  }, [dispatch]);

  useEffect(() => {
    if (saveSuccess) {
      setSuccessOpen(true);
      handleCloseDialog();
      dispatch(fetchManagementData());
    }
    if (saveError) {
      setErrorOpen(true);
    }
  }, [saveSuccess, saveError, dispatch]);

  const handleOpenDialog = useCallback((profile = null) => {
    setCurrentProfile(profile);
    setFormData(profile || {
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
      users: []
    });

    if (profile) {
      const profileUserIds = profile.users.map(user => user.userId);
      setSelectedUsers(profileUserIds);
      setDeletedUsers([]);
    } else {
      setSelectedUsers([]);
      setDeletedUsers([]);
    }

    setOpenDialog(true);
  }, []);

  const handleCloseDialog = useCallback(() => {
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
      users: []
    });
    setSelectedUsers([]);
    setDeletedUsers([]);
    setUserPage(1);
    setSearchQuery("");
  }, []);

  const handleChange = useCallback((e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  }, []);

  const handleUserSelection = useCallback((userId) => {
    setSelectedUsers(prev =>
        prev.includes(userId)
            ? prev.filter(id => id !== userId)
            : [...prev, userId]
    );
  }, []);

  const validateForm = useCallback(() => {
    const errors = {};
    let isValid = true;

    const requiredFields = [
      'name', 'workStart', 'workEnds', 'ignoreSl', 'gracePeriodeStart',
      'hdStart', 'slStartMorning', 'slStartEvening', 'possibleFpLocations',
      'defaultHrs', 'hdHrs', 'minHrsForSl', 'shortLeaveCount', 'hdEndsMorning',
      'flexiDays', 'flexiHrsStart'
    ];

    requiredFields.forEach(field => {
      if (!formData[field]) {
        errors[field] = `${field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())} is required`;
        isValid = false;
      }
    });

    setFormErrors(errors);
    return isValid;
  }, [formData]);

  const handleSubmit = useCallback(async () => {
    if (!validateForm()) return;

    const profileData = {
      ...formData,
      users: selectedUsers,
      deletedUsers,
    };

    dispatch(saveProfile({
      profileData: profileData,
      isUpdate:!!currentProfile
    }));
  }, [formData, selectedUsers, deletedUsers, currentProfile, validateForm, dispatch]);

  const handleDelete = useCallback((id) => {
    dispatch(deleteProfile(id));
  }, [dispatch]);

  const filteredUsers = useMemo(() => {
    if (!data?.users) return [];
    return data.users.filter(user =>
        user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [data?.users, searchQuery]);

  const paginatedUsers = useMemo(() =>
          filteredUsers.slice((userPage - 1) * usersPerPage, userPage * usersPerPage),
      [filteredUsers, userPage]);

  const isFormDirty = useMemo(() => {
    if (!currentProfile) return true;

    const formFields = Object.keys(formData);
    const isAnyFieldChanged = formFields.some(field => {
      if (field === 'users') return false; // Handle users separately
      return formData[field] !== currentProfile[field];
    });

    const areUsersChanged =
        selectedUsers.length !== currentProfile.users.length ||
        !selectedUsers.every(userId => currentProfile.users.some(user => user.userId === userId));

    return isAnyFieldChanged || areUsersChanged;
  }, [currentProfile, formData, selectedUsers]);

  return (
      <>
        <SuccessDialog
            open={successOpen}
            onClose={() => setSuccessOpen(false)}
            title="Success!"
            message="Profile saved successfully."
        />

        <ErrorDialog
            open={errorOpen}
            onClose={() => setErrorOpen(false)}
            title="Error"
            message={saveError || "Failed to save profile"}
        />

        <div>
          <Button variant="contained" onClick={() => handleOpenDialog()}>
            Add Profile
          </Button>

          <List>
            {data?.profiles?.map(profile => (
                <ListItem key={profile.id}>
                  <ListItemText
                      primary={profile.name}
                      secondary={`Public ID: ${profile.publicId}`}
                  />
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
              <Typography variant="h6" gutterBottom>
                Profile Details
              </Typography>
              <Box sx={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 2 }}>
                <TextField
                    label="Name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.name}
                    helperText={formErrors.name}
                />
                <TextField
                    disabled
                    label="Public ID"
                    name="publicId"
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
                    error={!!formErrors.workStart}
                    helperText={formErrors.workStart}
                />
                <TextField
                    label="Work Ends"
                    name="workEnds"
                    value={formData.workEnds}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.workEnds}
                    helperText={formErrors.workEnds}
                />
                <TextField
                    label="Ignore SL"
                    name="ignoreSl"
                    value={formData.ignoreSl}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.ignoreSl}
                    helperText={formErrors.ignoreSl}
                />
                <TextField
                    label="Grace Period Start"
                    name="gracePeriodeStart"
                    value={formData.gracePeriodeStart}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.gracePeriodeStart}
                    helperText={formErrors.gracePeriodeStart}
                />
                <TextField
                    label="HD Start"
                    name="hdStart"
                    value={formData.hdStart}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.hdStart}
                    helperText={formErrors.hdStart}
                />
                <TextField
                    label="SL Start Morning"
                    name="slStartMorning"
                    value={formData.slStartMorning}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.slStartMorning}
                    helperText={formErrors.slStartMorning}
                />
                <TextField
                    label="SL Start Evening"
                    name="slStartEvening"
                    value={formData.slStartEvening}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.slStartEvening}
                    helperText={formErrors.slStartEvening}
                />
                <TextField
                    label="Possible FP Locations"
                    name="possibleFpLocations"
                    value={formData.possibleFpLocations}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.possibleFpLocations}
                    helperText={formErrors.possibleFpLocations}
                />
                <TextField
                    label="Default Hours"
                    name="defaultHrs"
                    value={formData.defaultHrs}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.defaultHrs}
                    helperText={formErrors.defaultHrs}
                />
                <TextField
                    label="HD Hours"
                    name="hdHrs"
                    value={formData.hdHrs}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.hdHrs}
                    helperText={formErrors.hdHrs}
                />
                <TextField
                    label="Minimum Hours for SL"
                    name="minHrsForSl"
                    value={formData.minHrsForSl}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.minHrsForSl}
                    helperText={formErrors.minHrsForSl}
                />
                <TextField
                    label="Short Leave Count"
                    name="shortLeaveCount"
                    value={formData.shortLeaveCount}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.shortLeaveCount}
                    helperText={formErrors.shortLeaveCount}
                />
                <TextField
                    label="HD Ends Morning"
                    name="hdEndsMorning"
                    value={formData.hdEndsMorning}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.hdEndsMorning}
                    helperText={formErrors.hdEndsMorning}
                />
                <TextField
                    label="Flexi Days"
                    name="flexiDays"
                    value={formData.flexiDays}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.flexiDays}
                    helperText={formErrors.flexiDays}
                />
                <TextField
                    label="Flexi Hours Start"
                    name="flexiHrsStart"
                    value={formData.flexiHrsStart}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.flexiHrsStart}
                    helperText={formErrors.flexiHrsStart}
                />
              </Box>

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
                  {paginatedUsers.map(user => (
                      <ListItem key={user.userId}>
                        <ListItemIcon>
                          <Checkbox
                              checked={selectedUsers.includes(user.userId)}
                              onChange={() => handleUserSelection(user.userId)}
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
                    onChange={(e, value) => setUserPage(value)}
                    sx={{ mt: 2, display: "flex", justifyContent: "center" }}
                />
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDialog}>Cancel</Button>
              <Button
                  onClick={handleSubmit}
                  color="primary"
                  disabled={!isFormDirty || saveLoading}
              >
                {saveLoading ? 'Saving...' : currentProfile ? "Update" : "Add"}
              </Button>
            </DialogActions>
          </Dialog>
        </div>
      </>
  );
};

export default React.memo(ProfilesForm);
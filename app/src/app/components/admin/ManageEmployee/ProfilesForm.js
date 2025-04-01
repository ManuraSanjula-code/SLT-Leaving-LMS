// components/management/ProfileForm.js
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
  FormControlLabel,
  Switch,
} from "@mui/material";
import { Delete, Edit, Search } from "@mui/icons-material";
import { useDispatch, useSelector } from 'react-redux';
import { fetchManagementData, saveProfile, deleteProfile } from '../../../store/managementSlice';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

// Simple TimeInput component since TimePicker wasn't available
const TimeInput = ({ label, value, onChange, error, helperText }) => {
  return (
      <TextField
          label={label}
          type="time"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          fullWidth
          margin="normal"
          InputLabelProps={{
            shrink: true,
          }}
          inputProps={{
            step: 300, // 5 min intervals
          }}
          error={error}
          helperText={helperText}
      />
  );
};

const ProfileForm = ({ onSubmit }) => {
  const dispatch = useDispatch();
  const {
    data,
    loading,
    error,
    saveLoading,
    saveError,
    saveSuccess,
    currentPage,
    pageSize
  } = useSelector(state => state.management);

  const [openDialog, setOpenDialog] = useState(false);
  const [currentProfile, setCurrentProfile] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    publicId: "",
    workStart: "08:30",
    workEnds: "17:00",
    ignoreSl: false,
    gracePeriodStart: "08:45",
    hdStart: "12:00",
    slStartMorning: "08:30",
    slStartEvening: "13:30",
    possibleFpLocations: "Office,Remote",
    defaultHrs: "8",
    hdHrs: "4",
    minHrsForSl: "4",
    shortLeaveCount: "2",
    hdEndsMorning: "12:30",
    flexiDays: "5",
    flexiHrsStart: "07:00",
    users: []
  });
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [deletedUsers, setDeletedUsers] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [formErrors, setFormErrors] = useState({});
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);

  useEffect(() => {
    dispatch(fetchManagementData({ usersPage: currentPage, usersSize: pageSize }));
  }, [dispatch, currentPage, pageSize]);

  useEffect(() => {
    if (saveSuccess) {
      setSuccessOpen(true);
      handleCloseDialog();
      dispatch(fetchManagementData({ usersPage: currentPage, usersSize: pageSize }));
    }
    if (saveError) {
      setErrorOpen(true);
    }
  }, [saveSuccess, saveError, dispatch, currentPage, pageSize]);

  const handlePageChange = (event, newPage) => {
    dispatch(fetchManagementData({
      usersPage: newPage - 1,
      usersSize: pageSize
    }));
  };

  const handleOpenDialog = useCallback((profile = null) => {
    setCurrentProfile(profile);
    setFormData(profile || {
      name: "",
      publicId: "",
      workStart: "08:30",
      workEnds: "17:00",
      ignoreSl: false,
      gracePeriodStart: "08:45",
      hdStart: "12:00",
      slStartMorning: "08:30",
      slStartEvening: "13:30",
      possibleFpLocations: "Office,Remote",
      defaultHrs: "8",
      hdHrs: "4",
      minHrsForSl: "4",
      shortLeaveCount: "2",
      hdEndsMorning: "12:30",
      flexiDays: "5",
      flexiHrsStart: "07:00",
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
      workStart: "08:30",
      workEnds: "17:00",
      ignoreSl: false,
      gracePeriodStart: "08:45",
      hdStart: "12:00",
      slStartMorning: "08:30",
      slStartEvening: "13:30",
      possibleFpLocations: "Office,Remote",
      defaultHrs: "8",
      hdHrs: "4",
      minHrsForSl: "4",
      shortLeaveCount: "2",
      hdEndsMorning: "12:30",
      flexiDays: "5",
      flexiHrsStart: "07:00",
      users: []
    });
    setSelectedUsers([]);
    setDeletedUsers([]);
    setSearchQuery("");
  }, []);

  const handleChange = useCallback((e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  }, []);

  const handleTimeChange = useCallback((name, value) => {
    setFormData(prev => ({ ...prev, [name]: value }));
  }, []);

  const handleUserSelection = useCallback((userId) => {
    setSelectedUsers(prev => {
      const newSelected = prev.includes(userId)
          ? prev.filter(id => id !== userId)
          : [...prev, userId];

      if (currentProfile) {
        setDeletedUsers(prevDeleted => {
          const wasInOriginal = currentProfile.users.some(u => u.userId === userId);
          if (wasInOriginal && !newSelected.includes(userId)) {
            return [...prevDeleted, userId];
          }
          if (prevDeleted.includes(userId) && newSelected.includes(userId)) {
            return prevDeleted.filter(id => id !== userId);
          }
          return prevDeleted;
        });
      }
      return newSelected;
    });
  }, [currentProfile]);

  const validateForm = useCallback(() => {
    const errors = {};
    let isValid = true;

    const requiredFields = [
      'name', 'workStart', 'workEnds', 'gracePeriodStart',
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

    // Validate time format (HH:MM)
    const timeRegex = /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/;
    const timeFields = ['workStart', 'workEnds', 'gracePeriodStart', 'hdStart',
      'slStartMorning', 'slStartEvening', 'flexiHrsStart'];
    timeFields.forEach(field => {
      if (formData[field] && !timeRegex.test(formData[field])) {
        errors[field] = 'Invalid time format (HH:MM)';
        isValid = false;
      }
    });

    setFormErrors(errors);
    return isValid;
  }, [formData]);

  const handleSubmit = useCallback(async () => {
    if (!validateForm()) return;

    const addedUsers = selectedUsers
        .filter(userId => !currentProfile?.users.some(user => user.userId === userId))
        .map(userId => data.users.content.find(user => user.userId === userId));

    const profileData = {
      ...formData,
      addedUsers: addedUsers.map(user => user.userId),
      deletedUsers,
      id: currentProfile?.id || null
    };

    dispatch(saveProfile({
      profileData,
      isUpdate: !!currentProfile,
      profileId: currentProfile?.id
    }));
  }, [formData, selectedUsers, deletedUsers, currentProfile, validateForm, dispatch, data?.users?.content]);

  const handleDelete = useCallback((id) => {
    dispatch(deleteProfile(id));
  }, [dispatch]);

  const filteredUsers = useMemo(() => {
    if (!data?.users?.content) return [];
    return data.users.content.filter(user =>
        user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [data?.users?.content, searchQuery]);

  const isFormDirty = useMemo(() => {
    if (!currentProfile) return true;

    // Check all fields except users
    const fieldsToCheck = [
      'name', 'workStart', 'workEnds', 'ignoreSl', 'gracePeriodStart',
      'hdStart', 'slStartMorning', 'slStartEvening', 'possibleFpLocations',
      'defaultHrs', 'hdHrs', 'minHrsForSl', 'shortLeaveCount', 'hdEndsMorning',
      'flexiDays', 'flexiHrsStart'
    ];

    const isAnyFieldChanged = fieldsToCheck.some(field => {
      return formData[field] !== currentProfile[field];
    });

    // Check users separately
    const areUsersChanged =
        selectedUsers.length !== currentProfile.users.length ||
        !selectedUsers.every(userId =>
            currentProfile.users.some(user => user.userId === userId)
        );

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
                      secondary={`Work Hours: ${profile.workStart} - ${profile.workEnds}`}
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

                <TimeInput
                    label="Work Start"
                    value={formData.workStart}
                    onChange={(value) => handleTimeChange('workStart', value)}
                    error={!!formErrors.workStart}
                    helperText={formErrors.workStart}
                />

                <TimeInput
                    label="Work Ends"
                    value={formData.workEnds}
                    onChange={(value) => handleTimeChange('workEnds', value)}
                    error={!!formErrors.workEnds}
                    helperText={formErrors.workEnds}
                />

                <FormControlLabel
                    control={
                      <Switch
                          checked={formData.ignoreSl}
                          onChange={handleChange}
                          name="ignoreSl"
                          color="primary"
                      />
                    }
                    label="Ignore Short Leave"
                />

                <TimeInput
                    label="Grace Period Start"
                    value={formData.gracePeriodStart}
                    onChange={(value) => handleTimeChange('gracePeriodStart', value)}
                    error={!!formErrors.gracePeriodStart}
                    helperText={formErrors.gracePeriodStart}
                />

                <TimeInput
                    label="HD Start"
                    value={formData.hdStart}
                    onChange={(value) => handleTimeChange('hdStart', value)}
                    error={!!formErrors.hdStart}
                    helperText={formErrors.hdStart}
                />

                <TimeInput
                    label="SL Start Morning"
                    value={formData.slStartMorning}
                    onChange={(value) => handleTimeChange('slStartMorning', value)}
                    error={!!formErrors.slStartMorning}
                    helperText={formErrors.slStartMorning}
                />

                <TimeInput
                    label="SL Start Evening"
                    value={formData.slStartEvening}
                    onChange={(value) => handleTimeChange('slStartEvening', value)}
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
                    type="number"
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
                    type="number"
                    value={formData.hdHrs}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.hdHrs}
                    helperText={formErrors.hdHrs}
                />

                <TextField
                    label="Min Hours for SL"
                    name="minHrsForSl"
                    type="number"
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
                    type="number"
                    value={formData.shortLeaveCount}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.shortLeaveCount}
                    helperText={formErrors.shortLeaveCount}
                />

                <TimeInput
                    label="HD Ends Morning"
                    value={formData.hdEndsMorning}
                    onChange={(value) => handleTimeChange('hdEndsMorning', value)}
                    error={!!formErrors.hdEndsMorning}
                    helperText={formErrors.hdEndsMorning}
                />

                <TextField
                    label="Flexi Days"
                    name="flexiDays"
                    type="number"
                    value={formData.flexiDays}
                    onChange={handleChange}
                    fullWidth
                    margin="normal"
                    error={!!formErrors.flexiDays}
                    helperText={formErrors.flexiDays}
                />

                <TimeInput
                    label="Flexi Hours Start"
                    value={formData.flexiHrsStart}
                    onChange={(value) => handleTimeChange('flexiHrsStart', value)}
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
                  {filteredUsers.map(user => (
                      <ListItem key={user.userId}>
                        <ListItemIcon>
                          <Checkbox
                              checked={selectedUsers.includes(user.userId)}
                              onChange={() => handleUserSelection(user.userId)}
                          />
                        </ListItemIcon>
                        <ListItemText
                            primary={`${user.firstName} ${user.lastName}`}
                            secondary={user.email}
                        />
                      </ListItem>
                  ))}
                </List>
                <Pagination
                    count={data?.users?.totalPages || 1}
                    page={(data?.users?.pageable?.pageNumber || 0) + 1}
                    onChange={handlePageChange}
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

export default React.memo(ProfileForm);
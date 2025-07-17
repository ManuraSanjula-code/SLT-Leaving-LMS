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
import { fetchManagementData, saveSection, deleteSection } from '../../../../../lib/redux/redux-lms/user/managementSlice';
import SuccessDialog from '../../SuccessDialog';
import ErrorDialog from '../../ErrorDialog';

const SectionForm = ({ onSubmit }) => {
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
  const [currentSection, setCurrentSection] = useState(null);
  const [formData, setFormData] = useState({
    section: "",
    publicId: "",
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
  }, [saveSuccess, saveError, dispatch, currentPage, pageSize, handleCloseDialog]);

  const handlePageChange = (event, newPage) => {
    dispatch(fetchManagementData({
      usersPage: newPage - 1,
      usersSize: pageSize
    }));
  };

  const handleOpenDialog = useCallback((section = null) => {
    setCurrentSection(section);
    setFormData(section || {
      section: "",
      publicId: "",
      users: []
    });

    if (section) {
      const sectionUserIds = section.users.map(user => user.userId);
      setSelectedUsers(sectionUserIds);
      setDeletedUsers([]);
    } else {
      setSelectedUsers([]);
      setDeletedUsers([]);
    }

    setOpenDialog(true);
  }, []);

  const handleCloseDialog = useCallback(() => {
    setOpenDialog(false);
    setCurrentSection(null);
    setFormData({
      section: "",
      publicId: "",
      users: []
    });
    setSelectedUsers([]);
    setDeletedUsers([]);
    setSearchQuery("");
  }, []);

  const handleChange = useCallback((e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  }, []);

  const handleUserSelection = useCallback((userId) => {
    setSelectedUsers(prev => {
      const newSelected = prev.includes(userId)
        ? prev.filter(id => id !== userId)
        : [...prev, userId];

      // If this is an edit of an existing section
      if (currentSection) {
        setDeletedUsers(prevDeleted => {
          // If user was originally in the section and is now being removed
          const wasInOriginal = currentSection.users.some(u => u.userId === userId);
          if (wasInOriginal && !newSelected.includes(userId)) {
            return [...prevDeleted, userId];
          }
          // If user was previously marked for deletion but is now being re-added
          if (prevDeleted.includes(userId) && newSelected.includes(userId)) {
            return prevDeleted.filter(id => id !== userId);
          }
          return prevDeleted;
        });
      }
      return newSelected;
    });
  }, [currentSection]);

  const validateForm = useCallback(() => {
    const errors = { section: '' };
    let isValid = true;

    if (!formData.section.trim()) {
      errors.section = 'Section name is required';
      isValid = false;
    } else if (formData.section.length < 2) {
      errors.section = 'Section name must be at least 2 characters';
      isValid = false;
    }

    setFormErrors(errors);
    return isValid;
  }, [formData.section]);

  const handleSubmit = useCallback(async () => {
    if (!validateForm()) return;

    const addedUsers = selectedUsers
      .filter(userId => !currentSection?.users.some(user => user.userId === userId))
      .map(userId => data.users.content.find(user => user.userId === userId));

    const sectionData = {
      section: formData.section,
      publicId: formData.publicId,
      addedUsers: addedUsers.map(user => user.userId),
      deletedUsers
    };

    dispatch(saveSection({
      sectionData,
      isUpdate: !!currentSection,
      publicId: formData.publicId
    }));
  }, [
    formData,
    selectedUsers,
    deletedUsers,
    currentSection,
    validateForm,
    dispatch,
    data?.users?.content
  ]);

  const handleDelete = useCallback((id) => {
    dispatch(deleteSection(id));
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
    if (!currentSection) return true;

    const isSectionChanged = formData.section !== currentSection.section;
    const areUsersChanged =
      selectedUsers.length !== currentSection.users.length ||
      !selectedUsers.every(userId => currentSection.users.some(user => user.userId === userId));

    return isSectionChanged || areUsersChanged;
  }, [currentSection, formData.section, selectedUsers]);

  return (
    <>
      <SuccessDialog
        open={successOpen}
        onClose={() => setSuccessOpen(false)}
        title="Success!"
        message="Section saved successfully."
      />

      <ErrorDialog
        open={errorOpen}
        onClose={() => setErrorOpen(false)}
        title="Error"
        message={saveError || "Failed to save section"}
      />

      <div>
        <Button variant="contained" onClick={() => handleOpenDialog()}>
          Add Section
        </Button>

        <List>
          {data?.sections?.map(section => (
            <ListItem key={section.id}>
              <ListItemText
                primary={section.section}
                secondary={`Public ID: ${section.publicId}`}
              />
              <IconButton onClick={() => handleOpenDialog(section)}>
                <Edit />
              </IconButton>
              <IconButton onClick={() => handleDelete(section.id)}>
                <Delete />
              </IconButton>
            </ListItem>
          ))}
        </List>

        <Dialog open={openDialog} onClose={handleCloseDialog} fullWidth maxWidth="md">
          <DialogTitle>{currentSection ? "Edit Section" : "Add Section"}</DialogTitle>
          <DialogContent>
            <Typography variant="h6" gutterBottom>
              Section Details
            </Typography>
            <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2 }}>
              <TextField
                label="Section Name"
                name="section"
                value={formData.section}
                onChange={handleChange}
                fullWidth
                margin="normal"
                error={!!formErrors.section}
                helperText={formErrors.section}
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
              {saveLoading ? 'Saving...' : currentSection ? "Update" : "Add"}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    </>
  );
};

export default React.memo(SectionForm);
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

const SectionForm = () => {
  const [sections, setSections] = useState([]);
  const [users, setUsers] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentSection, setCurrentSection] = useState(null);
  const [formData, setFormData] = useState({ section: "", publicId: "", users: [] });
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [deletedUsers, setDeletedUsers] = useState([]);
  const [newUserId, setNewUserId] = useState("");
  const [userPage, setUserPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const usersPerPage = 10;
  const resolveRef = useRef(null); // Declare the ref
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [errors, setErrors] = useState({});

  // Fetch sections from the server
  useEffect(() => {
    const fetchSections = async () => {
      try {
        const response = await fetch("http://localhost:8080/users/sections", {
          method: "GET",
          headers: { "Content-Type": "application/json" },
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        setSections(data);
      } catch (err) {
        console.error("Error fetching sections:", err);
      }
    };

    fetchSections();
    fetch("http://localhost:8080/users")
      .then((response) => response.json())
      .then((data) => setUsers(data || []))
      .catch((err) => console.error("Error fetching users:", err));
  }, []);

  // Open dialog for adding or editing a section
  const handleOpenDialog = (section = null) => {
    setCurrentSection(section);
    setFormData(section || { section: "", publicId: "", users: [] });

    if (section) {
      const sectionUserIds = section.users.map((user) => user.id);
      setSelectedUsers(sectionUserIds);
      setDeletedUsers([]);
    } else {
      setSelectedUsers([]);
      setDeletedUsers([]);
    }

    setOpenDialog(true);
  };

  // Close dialog
  const handleCloseDialog = () => {
    setOpenDialog(false);
    setCurrentSection(null);
    setFormData({ section: "", publicId: "", users: [] });
    setSelectedUsers([]);
    setDeletedUsers([]);
    setNewUserId("");
    setUserPage(1);
    setSearchQuery("");
  };

  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // Handle user selection for the section
  const handleUserSelection = (userId) => {
    if (selectedUsers.includes(userId)) {
      setSelectedUsers((prev) => prev.filter((id) => id !== userId));
      setDeletedUsers((prev) => [...prev, userId]);
    } else {
      setSelectedUsers((prev) => [...prev, userId]);
      setDeletedUsers((prev) => prev.filter((id) => id !== userId));
    }
  };

  // Save section to the server
  const saveSection = async (sectionData, addedUsers, deletedUsers) => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      console.error("User ID not found in localStorage");
      return;
    }

    const url = `http://localhost:8080/users/section/${userId}`;
    const method = sectionData.id ? "PUT" : "POST";

    try {
      const response = await fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        credentials: 'include',
        body: JSON.stringify({
          section: sectionData.section, // Use `section` instead of `name`
          addedUsers: addedUsers.map((user) => user.id),
          deletedUsers: deletedUsers,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (err) {
      console.error("Error saving section:", err);
    }
  };

  // Handle form submission
  const handleSubmit = async () => {
    const newErrors = {};
    if (!formData.section) {
      newErrors.section = "Section Name is required.";
    }


    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    if (!formData.section) {
      console.error("Section name is required");
      return;
    }
    setErrors({});
    const updatedSection = {
      section: formData.section, // Use `section` instead of `name`
      publicId: formData.publicId,
      users: selectedUsers.map((id) => users.find((user) => user.id === id)),
    };

    const addedUsers = currentSection
      ? selectedUsers
        .filter((id) => !currentSection.users.some((user) => user.id === id))
        .map((id) => users.find((user) => user.id === id))
      : updatedSection.users;

    const deletedUsers = currentSection
      ? currentSection.users
        .filter((user) => !selectedUsers.includes(user.id))
        .map((user) => user.id)
      : [];

    try {
      const savedSection = await saveSection(updatedSection, addedUsers, deletedUsers);
      await showSuccessDialog();
      if (currentSection) {
        setSections((prevSections) =>
          prevSections.map((section) =>
            section.id === currentSection.id ? { ...section, ...savedSection } : section
          )
        );
      } else {
        setSections((prevSections) => [...prevSections, savedSection]);
      }

      handleCloseDialog();
    } catch (err) {
      await showErrorDialog()
      console.error("Error handling section submission:", err);
    }
  };

  const handleUserPageChange = (event, value) => {
    setUserPage(value);
  };
  // Handle deleting a section
  const handleDelete = async (id) => {
    try {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        console.error("User ID not found in localStorage");
        return;
      }
      const response = await fetch(`http://localhost:8080/users/section/${id}/${userId}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      setSections((prevSections) => prevSections.filter((section) => section.id !== id));
    } catch (err) {
      console.error("Error deleting section:", err);
    }
  };

  // Filter users based on search query
  const filteredUsers = users.filter(
    (user) =>
      user.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Pagination logic
  const paginatedUsers = filteredUsers.slice(
    (userPage - 1) * usersPerPage,
    userPage * usersPerPage
  );

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
          Add Section
        </Button>

        <List>
          {sections.map((section) => (
            <ListItem key={section.id}>
              <ListItemText primary={section.section} secondary={`Public ID: ${section.publicId}`} />
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
                error={!!errors.section} // Highlight the field if there's an error
                helperText={errors.section}
              />
              <TextField
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
              {currentSection ? "Update" : "Add"}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    </>
  );
};

export default SectionForm;
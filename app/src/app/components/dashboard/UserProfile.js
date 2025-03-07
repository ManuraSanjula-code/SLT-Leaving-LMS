'use client';

import React, { useState, useEffect } from 'react';
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  TextField,
  Button,
  Avatar,
  IconButton,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Checkbox,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Card,
  CardContent,
  Chip,
  Divider,
} from '@mui/material';
import { CameraAlt as CameraIcon } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { putUserData, putUserProfile } from '../../api';
import { setUserDetails, setLoading } from '../../redux/authSlice';
import { getCookie } from 'cookies-next';

const UserProfile = () => {
  const { userDetails, loading } = useSelector((state) => state.auth);
  const dispatch = useDispatch();

  const [open, setOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);

  // Local state for editing
  const [profile, setProfile] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    gender: '',
    profilePic: '',
    addresses: [],
  });

  // State for managing addresses dialog
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState(null);

  // Sync local state with Redux state
  useEffect(() => {
    if (userDetails) {
      setProfile({
        firstName: userDetails.firstName || '',
        lastName: userDetails.lastName || '',
        email: userDetails.email || '',
        phone: userDetails.phone || '',
        gender: userDetails.gender || '',
        profilePic: userDetails.profilePic || '',
        addresses: userDetails.addresses || [],
      });
    }
  }, [userDetails]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => ({ ...prev, [name]: value }));
  };

  const handleProfilePictureChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfile((prev) => ({ ...prev, profilePic: reader.result }));
      };
      reader.readAsDataURL(file);
    }
  };

  const handleAddAddress = () => {
    setSelectedAddress(null); // Clear selected address for adding a new one
    setOpenDialog(true);
  };

  const handleEditAddress = (address) => {
    setSelectedAddress(address);
    setOpenDialog(true);
  };

  const handleDeleteAddress = (addressId) => {
    setProfile((prev) => ({
      ...prev,
      addresses: prev.addresses.filter((addr) => addr.addressId !== addressId),
    }));
  };

  const handleSaveAddress = () => {
    // Validate the selectedAddress
    if (!selectedAddress || !selectedAddress.streetName || !selectedAddress.city || !selectedAddress.country || !selectedAddress.postalCode) {
      alert('Please fill in all required fields (Street Name, City, Country, Postal Code).');
      return;
    }

    if (selectedAddress.addressId) {
      // Edit existing address
      setProfile((prev) => ({
        ...prev,
        addresses: prev.addresses.map((addr) =>
          addr.addressId === selectedAddress.addressId ? selectedAddress : addr
        ),
      }));
    } else {
      // Add new address
      setProfile((prev) => ({
        ...prev,
        addresses: [...prev.addresses, { ...selectedAddress, addressId: Date.now().toString() }],
      }));
    }

    setOpenDialog(false); // Close the dialog
  };

  const handleSetDefaultAddress = (addressId) => {
    setProfile((prev) => ({
      ...prev,
      addresses: prev.addresses.map((addr) =>
        addr.addressId === addressId ? { ...addr, isDefault: true } : { ...addr, isDefault: false }
      ),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(setLoading(true));

    try {
      // Prepare payload for PUT request
      const payload = {
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        phone: profile.phone,
        gender: profile.gender,
        profilePic: profile.profilePic,
        addresses: profile.addresses,
      };

      const jwtFromCookie = getCookie('jwt')?.toString();
      const userIdFromCookie = getCookie('userId')?.toString();

      if (!jwtFromCookie || !userIdFromCookie) {
        alert('Failed to update profile. Please try again. 1');
        return;
      }
      if (userIdFromCookie !== userDetails.userId) {
        alert('Failed to update profile. Please try again. 2');
        return;
      }
      console.log(" ****************************** ", payload)
      const data = await putUserData(`/users/${userDetails.userId}`, jwtFromCookie, JSON.stringify(payload));
      console.log(data, "============================== ")
      // Update Redux state
      dispatch(setUserDetails({ ...userDetails, ...payload }));

      alert('Profile updated successfully!');
    } catch (error) {
      console.error('Failed to update profile:', error);
      alert('Failed to update profile. Please try again.');
    } finally {
      dispatch(setLoading(false));
    }
  };

  useEffect(() => {
    return () => {
      if (previewImage) {
        URL.revokeObjectURL(previewImage);
      }
    };
  }, [previewImage]);

  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      setPreviewImage(imageUrl);
      setSelectedFile(file);
    }
  };

  const handleSave = async () => {
    if (!selectedFile) return;
    try {
      const jwtFromCookie = getCookie('jwt')?.toString();
      const userIdFromCookie = getCookie('userId')?.toString();

      if (!jwtFromCookie || !userIdFromCookie) {
        alert('Failed to update profile. Please try again. 1');
        return;
      }
      if (userIdFromCookie !== userDetails.userId) {
        alert('Failed to update profile. Please try again. 2');
        return;
      }

      await putUserProfile(
        `http://localhost:8080/users/upload-pic/${userDetails.userId}`,
        jwtFromCookie,
        selectedFile
      );

      // Update parent state or context here if needed
      handleClose();
    } catch (error) {
      console.error('Upload failed:', error);
      setPreviewImage(null);
      setSelectedFile(null);
    }
  };

  return (
    <Container component="main" maxWidth="md">
      <CssBaseline />
      <Box sx={{ mt: 4 }}>
        {/* Profile Picture */}
        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
          <IconButton
            color="primary"
            aria-label="upload picture"
            onClick={handleOpen}
            sx={{ position: 'relative' }}
          >
            <Avatar
              // src={`http://localhost:8080/users/image/${previewImage || profile.profilePic}`}
              src={`http://localhost:8080/users/image/${userDetails.userId}`}
              sx={{ width: 150, height: 150, border: '2px solid #ccc' }}
            />
            <CameraIcon
              sx={{
                position: 'absolute',
                bottom: 0,
                right: 0,
                backgroundColor: 'white',
                borderRadius: '50%',
                padding: 1,
                boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.2)',
              }}
            />
          </IconButton>

          <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Change Profile Picture</DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                <input
                  accept="image/*"
                  id="profile-picture-input"
                  type="file"
                  onChange={handleFileChange}
                  style={{ display: 'none' }}
                />
                <label htmlFor="profile-picture-input">
                  <Button
                    variant="contained"
                    component="span"
                    fullWidth
                  >
                    Select Image
                  </Button>
                </label>

                {previewImage && (
                  <Avatar
                    src={previewImage}
                    sx={{
                      width: 200,
                      height: 200,
                      border: '2px solid #ccc',
                      margin: '0 auto'
                    }}
                  />
                )}
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleClose}>Cancel</Button>
              <Button
                onClick={handleSave}
                variant="contained"
                disabled={!selectedFile}
              >
                Save
              </Button>
            </DialogActions>
          </Dialog>
        </Box>

        {/* Personal Information */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              Personal Information
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <TextField
              margin="normal"
              required
              fullWidth
              id="firstName"
              label="First Name"
              name="firstName"
              value={profile.firstName}
              onChange={handleChange}
              sx={{ mb: 2 }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="lastName"
              label="Last Name"
              name="lastName"
              value={profile.lastName}
              onChange={handleChange}
              sx={{ mb: 2 }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              name="email"
              type="email"
              value={profile.email}
              onChange={handleChange}
              sx={{ mb: 2 }}
            />
            <TextField
              margin="normal"
              fullWidth
              id="phone"
              label="Phone"
              name="phone"
              value={profile.phone}
              onChange={handleChange}
              sx={{ mb: 2 }}
            />
            <TextField
              margin="normal"
              fullWidth
              id="gender"
              label="Gender"
              name="gender"
              value={profile.gender}
              onChange={handleChange}
            />
          </CardContent>
        </Card>

        {/* Roles */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              Roles
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {userDetails?.roles?.map((role, index) => (
                <Chip key={index} label={role} color="primary" variant="outlined" />
              ))}
            </Box>
          </CardContent>
        </Card>

        {/* Sections and Profiles */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              Sections & Profiles
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="subtitle1" gutterBottom>
              Sections
            </Typography>
            {userDetails?.sections?.length > 0 ? (
              <List>
                {userDetails.sections.map((section, index) => (
                  <ListItem key={index}>
                    <ListItemText primary={section} />
                  </ListItem>
                ))}
              </List>
            ) : (
              <Typography>No sections assigned.</Typography>
            )}
            <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
              Profiles
            </Typography>
            {userDetails?.profiles?.length > 0 ? (
              <List>
                {userDetails.profiles.map((profile, index) => (
                  <ListItem key={index}>
                    <ListItemText primary={profile} />
                  </ListItem>
                ))}
              </List>
            ) : (
              <Typography>No profiles assigned.</Typography>
            )}
          </CardContent>
        </Card>

        {/* Employment Status */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              Employment Status
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="body1">
              SLT Employee: {userDetails?.isSltEmp ? 'Yes' : 'No'}
            </Typography>
            <Typography variant="body1">
              SLT Intern: {userDetails?.isSltIntern ? 'Yes' : 'No'}
            </Typography>
            <Typography variant="body1">
              Account Status: {userDetails?.active ? 'Active' : 'Inactive'}
            </Typography>
          </CardContent>
        </Card>

        {/* Addresses */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              Addresses
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <List>
              {profile.addresses.length > 0 ? (
                profile.addresses.map((address) => (
                  <ListItem key={address.addressId}>
                    <ListItemText
                      primary={`${address.streetName}, ${address.city}, ${address.country}`}
                      secondary={`Postal Code: ${address.postalCode}`}
                    />
                    <ListItemSecondaryAction>
                      <Checkbox
                        edge="end"
                        checked={address.isDefault || false}
                        onChange={() => handleSetDefaultAddress(address.addressId)}
                      />
                      <IconButton onClick={() => handleEditAddress(address)}>Edit</IconButton>
                      <IconButton onClick={() => handleDeleteAddress(address.addressId)}>Delete</IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))
              ) : (
                <Typography>No addresses added yet.</Typography>
              )}
            </List>
            <Button
              variant="contained"
              color="primary"
              onClick={handleAddAddress}
              sx={{ mt: 2 }}
            >
              Add Address
            </Button>
          </CardContent>
        </Card>

        {/* Submit Button */}
        <Button
          type="submit"
          fullWidth
          variant="contained"
          color="secondary"
          onClick={handleSubmit}
          sx={{ py: 2, mt: 4 }}
        >
          Update Profile
        </Button>

        {/* Address Dialog */}
        <Dialog open={openDialog} onClose={() => setOpenDialog(false)}>
          <DialogTitle>{selectedAddress ? 'Edit Address' : 'Add Address'}</DialogTitle>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              id="streetName"
              label="Street Name"
              fullWidth
              value={selectedAddress?.streetName || ''}
              onChange={(e) =>
                setSelectedAddress((prev) => ({ ...prev, streetName: e.target.value }))
              }
            />
            <TextField
              margin="dense"
              id="city"
              label="City"
              fullWidth
              value={selectedAddress?.city || ''}
              onChange={(e) =>
                setSelectedAddress((prev) => ({ ...prev, city: e.target.value }))
              }
            />
            <TextField
              margin="dense"
              id="country"
              label="Country"
              fullWidth
              value={selectedAddress?.country || ''}
              onChange={(e) =>
                setSelectedAddress((prev) => ({ ...prev, country: e.target.value }))
              }
            />
            <TextField
              margin="dense"
              id="postalCode"
              label="Postal Code"
              fullWidth
              value={selectedAddress?.postalCode || ''}
              onChange={(e) =>
                setSelectedAddress((prev) => ({ ...prev, postalCode: e.target.value }))
              }
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)} color="primary">
              Cancel
            </Button>
            <Button onClick={() => handleSaveAddress(selectedAddress)} color="primary">
              Save
            </Button>
          </DialogActions>
        </Dialog>

        {/* Loading Spinner */}
        {loading && (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100vh',
            }}
          >
            <CircularProgress size={60} thickness={4} />
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default UserProfile;
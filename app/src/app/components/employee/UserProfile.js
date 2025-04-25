'use client';

import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
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
import { putUserProfile } from '../../api';
import { setUserDetails} from '../../redux-user/authSlice';
import isEqual from 'lodash/isEqual';
import debounce from 'lodash/debounce';
import throttle from 'lodash/throttle';
import SuccessDialog from '../SuccessDialog';
import ErrorDialog from '../ErrorDialog';

// Constants for better maintainability
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
const DEBOUNCE_DELAY = 300;
const THROTTLE_DELAY = 1000;

// Memoized components to prevent unnecessary re-renders
const MemoizedAvatar = React.memo(Avatar);
const MemoizedTextField = React.memo(TextField);
const MemoizedButton = React.memo(Button);
const MemoizedListItem = React.memo(ListItem);
const MemoizedDialog = React.memo(Dialog);
const MemoizedCard = React.memo(Card);

const UserProfile = () => {
  const { userDetails, loading } = useSelector((state) => state.auth);
  const dispatch = useDispatch();

  // Initialize profile state with deep comparison
  const initialProfileState = useMemo(() => ({
    firstName: userDetails?.firstName || '',
    lastName: userDetails?.lastName || '',
    email: userDetails?.email || '',
    phone: userDetails?.phone || '',
    gender: userDetails?.gender || '',
    profilePic: userDetails?.profilePic || '',
    addresses: userDetails?.addresses ? [...userDetails.addresses] : [],
    // Add the new fields
    employeeId: userDetails?.employeeId || '',
    sltId: userDetails?.sltId || '',
  }), [userDetails]);

  const [profile, setProfile] = useState(initialProfileState);
  const [originalProfile, setOriginalProfile] = useState(initialProfileState);
  const [open, setOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resolveRef = useRef(null);
  const fileInputRef = useRef(null);
  const formRef = useRef(null);
  const userId = useMemo(() => userDetails?.id || sessionStorage.getItem('userId'), [userDetails]);

  // Sync local state when Redux data changes
  useEffect(() => {
    if (userDetails && !isEqual(initialProfileState, profile)) {
      setProfile(initialProfileState);
      setOriginalProfile(initialProfileState);
    }
  }, [userDetails, initialProfileState, profile]);

  // Track if form is dirty (changed from original data)
  useEffect(() => {
  }, [profile, originalProfile]);

  // Clean up object URLs on unmount
  useEffect(() => {
    const preview = previewImage;
    return () => {
      if (preview) {
        URL.revokeObjectURL(preview);
      }
    };
  }, [previewImage]);

  // Dialog handlers
  const handleSuccessOpen = useCallback(() => setSuccessOpen(true), []);
  const handleSuccessClose = useCallback(() => setSuccessOpen(false), []);
  const handleErrorOpen = useCallback(() => setErrorOpen(true), []);
  const handleErrorClose = useCallback(() => {
    setErrorOpen(false);
    setErrors({});
  }, []);

  // Optimized field change handlers
  const debouncedHandleChange = useMemo(() =>
          debounce((e) => {
            const { name, value } = e.target;
            setProfile((prev) => ({ ...prev, [name]: value }));
          }, DEBOUNCE_DELAY),
      []
  );

  const handleChange = useCallback((e) => {
    e.persist();
    debouncedHandleChange(e);
  }, [debouncedHandleChange]);


  // Address management with throttling for rapid clicks
  const throttledAddAddress = useMemo(() =>
          throttle(() => {
            setSelectedAddress({
              streetName: '',
              city: '',
              country: '',
              postalCode: '',
              isDefault: false
            });
            setOpenDialog(true);
          }, THROTTLE_DELAY),
      []
  );

  const handleAddAddress = useCallback(() => {
    throttledAddAddress();
  }, [throttledAddAddress]);

  const throttledEditAddress = useMemo(() =>
          throttle((address) => {
            setSelectedAddress({ ...address });
            setOpenDialog(true);
          }, THROTTLE_DELAY),
      []
  );

  const handleEditAddress = useCallback((address) => {
    throttledEditAddress(address);
  }, [throttledEditAddress]);

  const throttledDeleteAddress = useMemo(() =>
          throttle((addressId) => {
            setProfile((prev) => ({
              ...prev,
              addresses: prev.addresses.filter((addr) => addr.addressId !== addressId),
              deleteAddresses: [...prev.deleteAddresses, addressId],
            }));
          }, THROTTLE_DELAY),
      []
  );

  const handleDeleteAddress = useCallback((addressId) => {
    throttledDeleteAddress(addressId);
  }, [throttledDeleteAddress]);

  const throttledSetDefaultAddress = useMemo(() =>
          throttle((addressId) => {
            setProfile((prev) => ({
              ...prev,
              addresses: prev.addresses.map((addr) => ({
                ...addr,
                isDefault: addr.addressId === addressId
              })),
            }));
          }, THROTTLE_DELAY),
      []
  );

  const handleSetDefaultAddress = useCallback((addressId) => {
    throttledSetDefaultAddress(addressId);
  }, [throttledSetDefaultAddress]);

  const handleSaveAddress = useCallback(() => {
    if (!selectedAddress) return;

    // Validate address fields
    const addressErrors = {};
    if (!selectedAddress.streetName?.trim()) addressErrors.streetName = 'Street name is required';
    if (!selectedAddress.city?.trim()) addressErrors.city = 'City is required';
    if (!selectedAddress.country?.trim()) addressErrors.country = 'Country is required';
    if (!selectedAddress.postalCode?.trim()) addressErrors.postalCode = 'Postal code is required';

    if (Object.keys(addressErrors).length > 0) {
      setErrors(addressErrors);
      return;
    }

    setProfile((prev) => {
      if (selectedAddress.addressId) {
        // Update existing address
        return {
          ...prev,
          addresses: prev.addresses.map((addr) =>
              addr.addressId === selectedAddress.addressId ? selectedAddress : addr
          ),
        };
      } else {
        // Add new address with unique ID
        const newAddress = {
          ...selectedAddress,
          addressId: Date.now().toString(),
          isDefault: prev.addresses.length === 0 ? true : selectedAddress.isDefault
        };
        return {
          ...prev,
          addresses: [...prev.addresses, newAddress],
        };
      }
    });

    setOpenDialog(false);
    setErrors({});
  }, [selectedAddress]);

  // Dialog promise helpers
  const showSuccessDialog = useCallback(() => {
    return new Promise((resolve) => {
      resolveRef.current = resolve;
      handleSuccessOpen();
    });
  }, [handleSuccessOpen]);

  const showErrorDialog = useCallback(() => {
    return new Promise((resolve) => {
      resolveRef.current = resolve;
      handleErrorOpen();
    });
  }, [handleErrorOpen]);

  const handleOpen = useCallback(() => {
    setOpen(true);
    // Reset file input when dialog opens
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, []);

  const handleClose = useCallback(() => {
    setOpen(false);
    setPreviewImage(null);
    setSelectedFile(null);
    setErrors({});
  }, []);

  const handleFileChange = useCallback((event) => {
    const file = event.target.files[0];
    if (file) {
      // Validate file type and size
      if (!file.type.match('image.*')) {
        setErrors(prev => ({ ...prev, profilePic: 'Please select an image file' }));
        return;
      }
      if (file.size > MAX_FILE_SIZE) {
        setErrors(prev => ({ ...prev, profilePic: 'File size should be less than 5MB' }));
        return;
      }

      const imageUrl = URL.createObjectURL(file);
      setPreviewImage(imageUrl);
      setSelectedFile(file);
      setErrors(prev => ({ ...prev, profilePic: undefined }));
    }
  }, []);

  const handleSaveProfilePic = useCallback(async () => {
    if (!selectedFile || !userId) return;

    setIsSubmitting(true);
    try {
      await putUserProfile(
          `http://localhost:8080/users/upload-pic/${userId}`,
          selectedFile
      );

      // Update Redux with new profile picture
      const newProfilePic = URL.createObjectURL(selectedFile);
      dispatch(setUserDetails({ ...userDetails, profilePic: newProfilePic }));

      // Update local state
      setProfile(prev => ({ ...prev, profilePic: newProfilePic }));
      setOriginalProfile(prev => ({ ...prev, profilePic: newProfilePic }));

      handleClose();
      await showSuccessDialog();
    } catch (error) {
      console.error('Upload failed:', error);
      setPreviewImage(null);
      setSelectedFile(null);
      await showErrorDialog();
    } finally {
      setIsSubmitting(false);
    }
  }, [selectedFile, userId, userDetails, dispatch, handleClose, showSuccessDialog, showErrorDialog]);

  // Show loading spinner during initialization
  if (loading && !userDetails) {
    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
          <CircularProgress size={60} thickness={4} />
        </Box>
    );
  }

  // Memoized address list to prevent unnecessary re-renders
  const addressList = useMemo(() => (
      profile.addresses.length > 0 ? (
          profile.addresses.map((address) => (
              <MemoizedListItem key={address.addressId}>
                <ListItemText
                    primary={`${address.streetName}, ${address.city}, ${address.country}`}
                    secondary={`Postal Code: ${address.postalCode}`}
                />
                <ListItemSecondaryAction>
                  <Checkbox
                      edge="end"
                      checked={address.isDefault || false}
                      onChange={() => handleSetDefaultAddress(address.addressId)}
                      inputProps={{ 'aria-label': 'Set as default address' }}
                  />
                  <IconButton
                      onClick={() => handleEditAddress(address)}
                      aria-label="View address"
                      disabled={isSubmitting}
                  >
                    View
                  </IconButton>
                </ListItemSecondaryAction>
              </MemoizedListItem>
          ))
      ) : (
          <Typography>No addresses added yet.</Typography>
      )
  ), [profile.addresses, handleSetDefaultAddress, handleEditAddress, handleDeleteAddress, isSubmitting]);

  // Memoized form sections to optimize rendering
  const personalInfoSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Personal Information
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <MemoizedTextField
              margin="normal"
              required
              fullWidth
              id="firstName"
              label="First Name"
              name="firstName"
              value={profile.firstName}
              onChange={handleChange}
              error={!!errors.firstName}
              helperText={errors.firstName}
              sx={{ mb: 2 }}
              disabled={isSubmitting}
          />
          <MemoizedTextField
              margin="normal"
              required
              fullWidth
              id="lastName"
              label="Last Name"
              name="lastName"
              value={profile.lastName}
              onChange={handleChange}
              error={!!errors.lastName}
              helperText={errors.lastName}
              sx={{ mb: 2 }}
              disabled={isSubmitting}
          />
          <MemoizedTextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              name="email"
              type="email"
              value={profile.email}
              onChange={handleChange}
              error={!!errors.email}
              helperText={errors.email}
              sx={{ mb: 2 }}
              disabled={isSubmitting}
          />
          <MemoizedTextField
              margin="normal"
              required
              fullWidth
              id="phone"
              label="Phone"
              name="phone"
              value={profile.phone}
              onChange={handleChange}
              error={!!errors.phone}
              helperText={errors.phone}
              sx={{ mb: 2 }}
              disabled={isSubmitting}
          />
          <MemoizedTextField
              margin="normal"
              required
              fullWidth
              id="gender"
              label="Gender"
              name="gender"
              value={profile.gender}
              onChange={handleChange}
              error={!!errors.gender}
              helperText={errors.gender}
              disabled={isSubmitting}
          />
        </CardContent>
      </MemoizedCard>
  ), [profile, errors, handleChange, isSubmitting]);

  // New section for Employee Identifiers
  const employeeIdentifiersSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Employee Identifiers
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <Typography variant="body1">
            Employee ID: {profile.employeeId || 'Not assigned'}
          </Typography>
          <Typography variant="body1" sx={{ mt: 1 }}>
            SLT ID: {profile.sltId || 'Not assigned'}
          </Typography>
        </CardContent>
      </MemoizedCard>
  ), [profile.employeeId, profile.sltId]);

  const rolesSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
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
      </MemoizedCard>
  ), [userDetails?.roles]);

  const sectionsProfilesSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
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
      </MemoizedCard>
  ), [userDetails?.sections, userDetails?.profiles]);

  const employmentStatusSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
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
      </MemoizedCard>
  ), [userDetails?.isSltEmp, userDetails?.isSltIntern, userDetails?.active]);

  const addressesSection = useMemo(() => (
      <MemoizedCard sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Addresses
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <List>
            {addressList}
          </List>
        </CardContent>
      </MemoizedCard>
  ), [addressList, handleAddAddress, isSubmitting]);

  return (
      <>
        <SuccessDialog
            open={successOpen}
            onClose={handleSuccessClose}
            title="Success!"
            message="Your action was completed successfully."
        />

        <ErrorDialog
            open={errorOpen}
            onClose={handleErrorClose}
            title="Oops! Something Went Wrong"
            message="There was an error processing your request. Please try again."
        />

        <Container component="main" maxWidth="md" ref={formRef}>
          <CssBaseline />
          <Box sx={{ mt: 4 }}>
            {/* Profile Picture */}
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
              <IconButton
                  color="primary"
                  aria-label="upload picture"
                  onClick={handleOpen}
                  sx={{ position: 'relative' }}
                  disabled={isSubmitting}
              >
                <MemoizedAvatar
                    src={`http://localhost:8080/users/image/${userId}` || ''}
                    sx={{ width: 150, height: 150, border: '2px solid #ccc' }}
                    alt="Profile picture"
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

              <MemoizedDialog open={open} onClose={handleClose}>
                <DialogTitle>Change Profile Picture</DialogTitle>
                <DialogContent>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                    <input
                        accept="image/*"
                        id="profile-picture-input"
                        type="file"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                        ref={fileInputRef}
                        disabled={isSubmitting}
                    />
                    <label htmlFor="profile-picture-input">
                      <MemoizedButton
                          variant="contained"
                          component="span"
                          fullWidth
                          disabled={isSubmitting}
                      >
                        Select Image
                      </MemoizedButton>
                    </label>

                    {previewImage && (
                        <MemoizedAvatar
                            src={previewImage}
                            sx={{
                              width: 200,
                              height: 200,
                              border: '2px solid #ccc',
                              margin: '0 auto'
                            }}
                            alt="Preview"
                        />
                    )}
                    {errors.profilePic && (
                        <Typography color="error" variant="body2">
                          {errors.profilePic}
                        </Typography>
                    )}
                  </Box>
                </DialogContent>
                <DialogActions>
                  <MemoizedButton onClick={handleClose} disabled={isSubmitting}>
                    Cancel
                  </MemoizedButton>
                  <MemoizedButton
                      onClick={handleSaveProfilePic}
                      variant="contained"
                      disabled={!selectedFile || isSubmitting}
                  >
                    {isSubmitting ? <CircularProgress size={24} /> : 'Save'}
                  </MemoizedButton>
                </DialogActions>
              </MemoizedDialog>
            </Box>

            {/* Form Sections */}
            {personalInfoSection}
            {employeeIdentifiersSection} {/* Add the new section here */}
            {rolesSection}
            {sectionsProfilesSection}
            {employmentStatusSection}
            {addressesSection}


            {/* Address Dialog */}
            <MemoizedDialog open={openDialog} onClose={() => setOpenDialog(false)}>
              <DialogTitle>{selectedAddress?.addressId ? 'Edit Address' : 'Add Address'}</DialogTitle>
              <DialogContent>
                <MemoizedTextField
                    autoFocus
                    margin="dense"
                    id="streetName"
                    label="Street Name"
                    fullWidth
                    required
                    value={selectedAddress?.streetName || ''}
                    onChange={(e) =>
                        setSelectedAddress((prev) => ({ ...prev, streetName: e.target.value }))
                    }
                    error={!!errors.streetName}
                    helperText={errors.streetName}
                    disabled={isSubmitting}
                />
                <MemoizedTextField
                    margin="dense"
                    id="city"
                    label="City"
                    fullWidth
                    required
                    value={selectedAddress?.city || ''}
                    onChange={(e) =>
                        setSelectedAddress((prev) => ({ ...prev, city: e.target.value }))
                    }
                    error={!!errors.city}
                    helperText={errors.city}
                    disabled={isSubmitting}
                />
                <MemoizedTextField
                    margin="dense"
                    id="country"
                    label="Country"
                    fullWidth
                    required
                    value={selectedAddress?.country || ''}
                    onChange={(e) =>
                        setSelectedAddress((prev) => ({ ...prev, country: e.target.value }))
                    }
                    error={!!errors.country}
                    helperText={errors.country}
                    disabled={isSubmitting}
                />
                <MemoizedTextField
                    margin="dense"
                    id="postalCode"
                    label="Postal Code"
                    fullWidth
                    required
                    value={selectedAddress?.postalCode || ''}
                    onChange={(e) =>
                        setSelectedAddress((prev) => ({ ...prev, postalCode: e.target.value }))
                    }
                    error={!!errors.postalCode}
                    helperText={errors.postalCode}
                    disabled={isSubmitting}
                />
              </DialogContent>
              <DialogActions>
                <MemoizedButton
                    onClick={() => setOpenDialog(false)}
                    color="primary"
                    disabled={isSubmitting}
                >
                  Cancel
                </MemoizedButton>
                <MemoizedButton
                    onClick={handleSaveAddress}
                    color="primary"
                    disabled={isSubmitting}
                >
                  Save
                </MemoizedButton>
              </DialogActions>
            </MemoizedDialog>

            {/* Global Loading Indicator */}
            {loading && (
                <Box
                    sx={{
                      position: 'fixed',
                      top: 0,
                      left: 0,
                      right: 0,
                      bottom: 0,
                      backgroundColor: 'rgba(255,255,255,0.7)',
                      display: 'flex',
                      justifyContent: 'center',
                      alignItems: 'center',
                      zIndex: 9999,
                    }}
                >
                  <CircularProgress size={60} thickness={4} />
                </Box>
            )}
          </Box>
        </Container>
      </>
  );
};

export default React.memo(UserProfile);
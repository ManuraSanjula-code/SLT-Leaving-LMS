"use client";

import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  CircularProgress,
  Modal,
  Alert,
  FormControl,
  InputLabel,
  Divider
} from "@mui/material";
import { Close, Check } from "@mui/icons-material";

const leaveTypes = [
  { id: '1', name: 'Annual Leave', uuid: 'cd80ada8-7844-4788-9b1d-d59a85517a69' },
  { id: '2', name: 'Medical Leave', uuid: '0bba8dd6-9116-4010-b7ea-4cda306dad6e' },
  { id: '3', name: 'Casual Leave', uuid: '113475bf-b35f-47a6-89fa-62727c9c050a' },
  { id: '4', name: 'Maternity Leave', uuid: '65ce0e73-b2c3-423b-aee9-211808891ee7' },
  { id: '5', name: 'Short Leave', uuid: '280223f8-7389-43cd-892c-f5066e2e9719' },
  { id: '6', name: 'Duty Leave', uuid: 'c5c2a038-5268-4a84-92e1-4880395bf993' },
  { id: '7', name: 'Special Leave', uuid: 'a24a487e-a788-4aea-bd5a-a5807326262d' }
];

const ApplyLeaveModal = ({
  open,
  onClose,
  employee,
  onSuccess
}) => {
  const [formData, setFormData] = useState({
    fromDate: '',
    toDate: '',
    leaveType: leaveTypes[0].uuid,
    description: '',
    userId: '',
    numOfDays: 1,
    happenDate: '',
    componentBehavior: 'ABSENT',
    publicId: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (employee && open) {
      const date = new Date(employee.date);
      date.setDate(date.getDate() + 1);
      const formattedDate = date.toISOString().split('T')[0];

      setFormData({
        fromDate: formattedDate,
        toDate: formattedDate,
        leaveType: leaveTypes[0].uuid,
        description: employee.reason || 'Leave application for absent day',
        userId: employee.publicId || '',
        numOfDays: 2,
        happenDate: formattedDate,
        componentBehavior: 'ABSENT',
        publicId: employee.publicId || ''
      });
      setError(null);
      setSuccess(false);
    }
  }, [employee, open]);

  const handleDateChange = (newDate) => {
    setFormData(prev => ({
      ...prev,
      fromDate: newDate,
      toDate: newDate,
      happenDate: newDate,
      numOfDays: 2
    }));
  };

  const handleInputChange = (field) => (event) => {
    const value = event.target.value;
    if (field === 'fromDate' || field === 'toDate' || field === 'happenDate') {
      handleDateChange(value);
    } else {
      setFormData(prev => ({
        ...prev,
        [field]: value
      }));
    }
  };

  const validateForm = () => {
    if (!formData.fromDate) {
      setError('From date is required');
      return false;
    }
    if (!formData.toDate) {
      setError('To date is required');
      return false;
    }
    if (!formData.leaveType) {
      setError('Leave type is required');
      return false;
    }
    if (!formData.userId) {
      setError('User ID is required');
      return false;
    }
    if (!formData.happenDate) {
      setError('Happen date is required');
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    setLoading(true);
    setError(null);

    try {
      const userId = sessionStorage.getItem('userId');
      const empId = sessionStorage.getItem('userId');

      if (!userId || !empId) {
        throw new Error('User authentication information not found');
      }

      const selectedLeaveType = leaveTypes.find(lt => lt.uuid === formData.leaveType);

      const payload = {
        publicId: formData.publicId,
        fromDate: new Date(formData.fromDate).toISOString(),
        toDate: new Date(formData.toDate).toISOString(),
        leaveType: selectedLeaveType.name,
        leaveTypeId: selectedLeaveType.id,
        leaveTypeUUID: selectedLeaveType.uuid,
        description: formData.description,
        userId: formData.userId,
        numOfDays: 1,
        happenDate: new Date(formData.happenDate).toISOString(),
        componentBehavior: formData.componentBehavior,
        requestStatus: 'DRAFT',
        notUsed: false,
        isManualRequest: true,
        isEdited: false
      };

      const response = await fetch(
        `http://192.168.3.20:8080/lms/management/leave/create/${userId}/${empId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
          credentials: 'include'
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(`${errorData.message}`);
      }

      setSuccess(true);

      setTimeout(() => {
        onClose();
        setSuccess(false);
      }, 2000);

    } catch (error) {
      console.error('Error submitting leave application:', error);
      setError(error.message || 'Failed to submit leave application');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      onClose();
      setError(null);
      setSuccess(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Box
        sx={{
          width: '90%',
          maxWidth: '800px',
          maxHeight: '90vh',
          bgcolor: 'background.paper',
          borderRadius: 2,
          boxShadow: 24,
          overflow: 'auto',
          p: 3,
          border: '1px solid rgba(0, 0, 0, 0.12)'
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h5" fontWeight="bold">
            Apply Leave for Absence
          </Typography>
          <Button onClick={handleClose} sx={{ minWidth: 0 }}>
            <Close />
          </Button>
        </Box>

        {employee && (
          <Typography variant="subtitle1" color="text.secondary" mb={3}>
            Employee: {employee.employeeName} ({employee.publicId})
          </Typography>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 3 }} icon={<Check />}>
            Leave application submitted successfully!
          </Alert>
        )}

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Employee Information</Typography>
          <Box sx={{ display: 'grid', gridTemplateColumns: { sm: '1fr 1fr' }, gap: 2 }}>
            <TextField
              label="User ID *"
              value={formData.userId}
              onChange={handleInputChange('userId')}
              required
              fullWidth
            />
            <TextField
              label="Public ID"
              value={formData.publicId}
              disabled
              fullWidth
            />
          </Box>
        </Box>

        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Leave Details</Typography>
          <Alert severity="info" sx={{ mb: 3 }}>
            This application is for a single day absence. All dates will be synchronized.
          </Alert>

          <Box sx={{ display: 'grid', gridTemplateColumns: { sm: '1fr 1fr' }, gap: 2, mb: 2 }}>
            <TextField
              label="Leave Date *"
              type="date"
              value={formData.fromDate}
              onChange={handleInputChange('fromDate')}
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            <FormControl fullWidth>
              <InputLabel>Leave Type *</InputLabel>
              <Select
                value={formData.leaveType}
                onChange={handleInputChange('leaveType')}
                label="Leave Type *"
                required
              >
                {leaveTypes.map((type) => (
                  <MenuItem key={type.uuid} value={type.uuid}>
                    {type.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>

          <TextField
            label="Description"
            value={formData.description}
            onChange={handleInputChange('description')}
            multiline
            rows={3}
            fullWidth
            sx={{ mb: 2 }}
          />
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 3 }}>
          <Button
            onClick={handleClose}
            disabled={loading}
            variant="outlined"
            color="inherit"
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading || success}
            variant="contained"
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            {loading ? 'Submitting...' : 'Submit Application'}
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default ApplyLeaveModal;
'use client'; // This is a client component

import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { clearAuth } from './redux/authSlice';
import { Snackbar, Alert } from '@mui/material';

export default function Notification() {
  const dispatch = useDispatch();
  const { successMessage, errorMessage } = useSelector((state) => state.auth);

  // State to control Snackbar visibility
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState('');
  const [severity, setSeverity] = useState('success'); // 'success' or 'error'

  useEffect(() => {
    if (successMessage) {
      setMessage(successMessage);
      setSeverity('success');
      setOpen(true);
    } else if (errorMessage) {
      setMessage(errorMessage);
      setSeverity('error');
      setOpen(true);
    }
  }, [successMessage, errorMessage]);

  const handleClose = () => {
    setOpen(false);
    
  };

  return (
    <Snackbar
      open={open}
      autoHideDuration={6000} // Auto-close after 6 seconds
      onClose={handleClose}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }} // Position: top-right
    >
      <Alert onClose={handleClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
}
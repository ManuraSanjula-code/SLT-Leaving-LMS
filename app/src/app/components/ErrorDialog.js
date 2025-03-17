import React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import ErrorIcon from '@mui/icons-material/Error';
import { red } from '@mui/material/colors';

const ErrorDialog = ({ open, onClose, title, message }) => {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', color: red[500] }}>
        <ErrorIcon sx={{ marginRight: 1 }} />
        {title || 'Error'}
      </DialogTitle>
      <DialogContent>
        <DialogContentText>{message || 'An error occurred. Please try again.'}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ErrorDialog;
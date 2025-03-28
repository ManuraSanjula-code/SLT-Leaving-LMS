import React from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    Button
} from '@mui/material';

const ConfirmationDialog = ({
                                open,
                                onClose,
                                onConfirm,
                                title = "Confirm Action",
                                message = "Are you sure you want to perform this action?",
                                confirmText = "Confirm",
                                cancelText = "Cancel"
                            }) => {
    return (
        <Dialog
            open={open}
            onClose={onClose}
            aria-labelledby="alert-dialog-title"
            aria-describedby="alert-dialog-description"
        >
            <DialogTitle id="alert-dialog-title">{title}</DialogTitle>
            <DialogContent>
                <DialogContentText id="alert-dialog-description">
                    {message}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} color="primary">
                    {cancelText}
                </Button>
                <Button onClick={onConfirm} color="primary" autoFocus>
                    {confirmText}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfirmationDialog;
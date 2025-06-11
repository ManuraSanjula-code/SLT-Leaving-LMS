import React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';

const DynamicDialog = ({ open, onClose, title, items }) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDialog-paper': {
          width: '80%',
          maxWidth: 'none',
        },
      }}
    >
      <DialogTitle>{title || 'Choose your option'}</DialogTitle>
      <DialogContent>
        <List>
          {items.map((item, index) => (
            <ListItem key={index} disablePadding>
              <ListItemButton onClick={() => item.onClick()}>
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </DialogContent>
    </Dialog>
  );
};

export default DynamicDialog;
'use client'
import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';

const EnhancedDatePicker = ({ selectedDate, onChange, disabled }) => {
    const formatDateForInput = (date) => {
        if (!date) return '';
        const d = new Date(date);
        if (isNaN(d.getTime())) return '';
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };

    const handleChange = (e) => {
        if (e.target.value) {
            const newDate = new Date(e.target.value);
            if (!isNaN(newDate.getTime())) {
                onChange(newDate);
            }
        }
    };

    return (
        <TextField
            label="Select Date"
            type="date"
            value={formatDateForInput(selectedDate)}
            onChange={handleChange}
            fullWidth
            disabled={disabled}
            InputLabelProps={{ shrink: true }}
            InputProps={{
                startAdornment: (
                    <InputAdornment position="start">
                        <CalendarTodayIcon color="primary" />
                    </InputAdornment>
                ),
                sx: { borderRadius: 1, height: 56 }
            }}
            sx={{
                '& .MuiOutlinedInput-root': {
                    '&.Mui-focused fieldset': {
                        borderColor: 'primary.main',
                        borderWidth: 2,
                    },
                },
                '& .MuiInputLabel-root': {
                    transform: 'translate(14px, -9px) scale(0.75)',
                    backgroundColor: 'white',
                    px: 1,
                }
            }}
        />
    );
};

export default EnhancedDatePicker;
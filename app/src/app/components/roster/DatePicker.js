"use client";

import React, { useState, useEffect } from 'react';
import { Box } from '@mui/material';

const DatePicker = ({ selectedDate, onChange }) => {
    const [date, setDate] = useState(selectedDate);

    useEffect(() => {
        setDate(selectedDate);
    }, [selectedDate]);

    const handleDateChange = (e) => {
        const newDate = e.target.value;
        setDate(newDate);
        onChange(newDate);
    };

    return (
        <Box mb={2}>
            <input
                type="date"
                value={date}
                onChange={handleDateChange}
                style={{
                    padding: '10px',
                    borderRadius: '4px',
                    border: '1px solid #ccc',
                    fontSize: '16px',
                    width: '200px'
                }}
            />
        </Box>
    );
};

export default DatePicker;
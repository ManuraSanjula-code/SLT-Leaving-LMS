import React, { useEffect } from 'react';
import {
    Button,
    Box,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Typography,
    Grid,
    Paper,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
    Alert,
    CircularProgress
} from '@mui/material';
import { useSelector, useDispatch } from 'react-redux';
import EventIcon from "@mui/icons-material/Event";
import HolidayManagement from './HolidayManagement';
import {
    openDialog,
    closeDialog,
    updateFormField,
    setFile,
    clearMessages,
    uploadRoster,
    uploadRosterShift,
    uploadDutyRoster,
    deleteRoster,
    deleteRosterShift,
    deleteDutyRoster,
    downloadAttendance,
    downloadAttendanceByDate,
    downloadAttendanceByMonth
} from '../../../../lib/redux/redux-lms/other/otherSlice.js';

const Other = () => {
    const dispatch = useDispatch();
    const { userDetails } = useSelector((state) => state.auth);
    const { 
        dialogs, 
        form, 
        files, 
        loading, 
        error, 
        successMessage 
    } = useSelector((state) => state.other);

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 11 }, (_, i) => currentYear - 5 + i);

    const months = [
        { value: 1, label: 'January' },
        { value: 2, label: 'February' },
        { value: 3, label: 'March' },
        { value: 4, label: 'April' },
        { value: 5, label: 'May' },
        { value: 6, label: 'June' },
        { value: 7, label: 'July' },
        { value: 8, label: 'August' },
        { value: 9, label: 'September' },
        { value: 10, label: 'October' },
        { value: 11, label: 'November' },
        { value: 12, label: 'December' }
    ];

    useEffect(() => {
        if (error || successMessage) {
            const timer = setTimeout(() => {
                dispatch(clearMessages());
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [error, successMessage, dispatch]);

    const handleRosterFileChange = (event) => {
        dispatch(setFile({ fileType: 'roster', file: event.target.files[0] }));
    };

    const handleRosterShiftFileChange = (event) => {
        dispatch(setFile({ fileType: 'rosterShift', file: event.target.files[0] }));
    };

    const handleDutyRosterFileChange = (event) => {
        dispatch(setFile({ fileType: 'dutyRoster', file: event.target.files[0] }));
    };

    const handleFormFieldChange = (field, value) => {
        dispatch(updateFormField({ field, value }));
    };

    const handleOpenDialog = (dialogName) => {
        dispatch(openDialog(dialogName));
    };

    const handleCloseDialog = (dialogName) => {
        dispatch(closeDialog(dialogName));
    };

    const handleUploadRoster = () => {
        if (!files.roster) {
            alert('Please select a file first');
            return;
        }
        dispatch(uploadRoster(files.roster));
    };

    const handleUploadRosterShift = () => {
        if (!files.rosterShift) {
            alert('Please select a file first');
            return;
        }
        dispatch(uploadRosterShift(files.rosterShift));
    };

    const handleUploadDutyRoster = () => {
        if (!files.dutyRoster || !form.weekStartingDate) {
            alert('Please select a file and week starting date');
            return;
        }
        
        dispatch(uploadDutyRoster({
            file: files.dutyRoster,
            rosterName: form.rosterName,
            weekStartingDate: form.weekStartingDate
        }));
    };

    const handleDeleteRoster = () => {
        if (!form.rosterDate) {
            alert('Please select a date');
            return;
        }
        dispatch(deleteRoster(form.rosterDate));
    };

    const handleDeleteRosterShift = () => {
        if (!form.rosterShiftDate) {
            alert('Please select a date');
            return;
        }
        dispatch(deleteRosterShift(form.rosterShiftDate));
    };

    const handleDeleteDutyRoster = () => {
        if (!form.weekStartingDate) {
            alert('Please select a week starting date');
            return;
        }
        dispatch(deleteDutyRoster({
            rosterName: form.rosterName,
            weekStartingDate: form.weekStartingDate
        }));
    };

    const handleGetAttendance = () => {
        if (!form.userId) {
            alert('Please enter a user ID');
            return;
        }
        dispatch(downloadAttendance(form.userId));
    };

    const handleGetAttendanceByDate = () => {
        if (!form.userId || !form.startDate) {
            alert('Please enter user ID and select a date');
            return;
        }
        dispatch(downloadAttendanceByDate({
            userId: form.userId,
            date: form.startDate
        }));
    };

    const handleGetAttendanceByMonth = () => {
        if (!form.userId || !form.selectedYear || !form.selectedMonth) {
            alert('Please enter user ID and select year and month');
            return;
        }
        dispatch(downloadAttendanceByMonth({
            userId: form.userId,
            year: form.selectedYear,
            month: form.selectedMonth
        }));
    };

    return (
        <Paper elevation={3} sx={{ p: 3, m: 2 }}>
            <Typography variant="h5" gutterBottom>
                Others
            </Typography>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}
            
            {successMessage && (
                <Alert severity="success" sx={{ mb: 2 }}>
                    {successMessage}
                </Alert>
            )}

            <Grid container spacing={2} sx={{ mt: 2 }}>
                {(userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50) && (
                    <>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                fullWidth
                                onClick={() => handleOpenDialog('uploadRosterShift')}
                                disabled={loading.uploadRosterShift}
                            >
                                {loading.uploadRosterShift && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Upload Roster Shift
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                fullWidth
                                onClick={() => handleOpenDialog('uploadRoster')}
                                disabled={loading.uploadRoster}
                            >
                                {loading.uploadRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Upload Roster
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="info"
                                fullWidth
                                onClick={() => handleOpenDialog('uploadDutyRoster')}
                                disabled={loading.uploadDutyRoster}
                            >
                                {loading.uploadDutyRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                For Charana Tv
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="error"
                                fullWidth
                                onClick={() => handleOpenDialog('deleteRosterShift')}
                                disabled={loading.deleteRosterShift}
                            >
                                {loading.deleteRosterShift && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Delete Roster Shift
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="error"
                                fullWidth
                                onClick={() => handleOpenDialog('deleteRoster')}
                                disabled={loading.deleteRoster}
                            >
                                {loading.deleteRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Delete Roster
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="warning"
                                fullWidth
                                onClick={() => handleOpenDialog('deleteDutyRoster')}
                                disabled={loading.deleteDutyRoster}
                            >
                                {loading.deleteDutyRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Delete Roster (For Charana Tv)
                            </Button>
                        </Grid>
                    </>
                )}

                {(userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50) && (
                    <>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="secondary"
                                fullWidth
                                onClick={() => handleOpenDialog('getAttendance')}
                                disabled={loading.downloadAttendance}
                            >
                                {loading.downloadAttendance && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Get All Attendance by UserId
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="secondary"
                                fullWidth
                                onClick={() => handleOpenDialog('getAttendanceByDate')}
                                disabled={loading.downloadAttendanceByDate}
                            >
                                {loading.downloadAttendanceByDate && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Get All Attendance by UserId and Date
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="secondary"
                                fullWidth
                                onClick={() => handleOpenDialog('getAttendanceByMonth')}
                                disabled={loading.downloadAttendanceByMonth}
                            >
                                {loading.downloadAttendanceByMonth && <CircularProgress size={20} sx={{ mr: 1 }} />}
                                Get All Attendance by Month
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                onClick={() => handleOpenDialog('holiday')}
                                startIcon={<EventIcon />}
                            >
                                Holidays
                            </Button>

                            <HolidayManagement
                                open={dialogs.holiday}
                                onClose={() => handleCloseDialog('holiday')}
                            />
                        </Grid>
                    </>
                )}
            </Grid>

            <Dialog open={dialogs.uploadRoster} onClose={() => handleCloseDialog('uploadRoster')}>
                <DialogTitle>Upload Roster</DialogTitle>
                <DialogContent>
                    <Box sx={{ mt: 2 }}>
                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{ display: 'none' }}
                            id="roster-file-upload"
                            type="file"
                            onChange={handleRosterFileChange}
                        />
                        <label htmlFor="roster-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {files.roster && (
                            <Typography variant="body2" sx={{ mt: 1 }}>
                                Selected file: {files.roster.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('uploadRoster')}>Cancel</Button>
                    <Button
                        onClick={handleUploadRoster}
                        color="primary"
                        disabled={!files.roster || loading.uploadRoster}
                    >
                        {loading.uploadRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.uploadDutyRoster} onClose={() => handleCloseDialog('uploadDutyRoster')}>
                <DialogTitle>Upload Roster For Charana Tv</DialogTitle>
                <DialogContent>
                    <Box sx={{ mt: 2 }}>
                        <TextField
                            label="Roster Name"
                            fullWidth
                            value={form.rosterName}
                            onChange={(e) => handleFormFieldChange('rosterName', e.target.value)}
                            sx={{ mb: 2 }}
                        />

                        <Typography variant="subtitle2" gutterBottom>
                            Week Starting Date
                        </Typography>
                        <TextField
                            type="date"
                            fullWidth
                            InputLabelProps={{
                                shrink: true,
                            }}
                            value={form.weekStartingDate}
                            onChange={(e) => handleFormFieldChange('weekStartingDate', e.target.value)}
                            sx={{ mb: 2 }}
                        />

                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{ display: 'none' }}
                            id="duty-roster-file-upload"
                            type="file"
                            onChange={handleDutyRosterFileChange}
                        />
                        <label htmlFor="duty-roster-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {files.dutyRoster && (
                            <Typography variant="body2" sx={{ mt: 1 }}>
                                Selected file: {files.dutyRoster.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('uploadDutyRoster')}>Cancel</Button>
                    <Button
                        onClick={handleUploadDutyRoster}
                        color="primary"
                        disabled={!files.dutyRoster || !form.weekStartingDate || loading.uploadDutyRoster}
                    >
                        {loading.uploadDutyRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.uploadRosterShift} onClose={() => handleCloseDialog('uploadRosterShift')}>
                <DialogTitle>Upload Roster Shift</DialogTitle>
                <DialogContent>
                    <Box sx={{ mt: 2 }}>
                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{ display: 'none' }}
                            id="roster-shift-file-upload"
                            type="file"
                            onChange={handleRosterShiftFileChange}
                        />
                        <label htmlFor="roster-shift-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {files.rosterShift && (
                            <Typography variant="body2" sx={{ mt: 1 }}>
                                Selected file: {files.rosterShift.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('uploadRosterShift')}>Cancel</Button>
                    <Button
                        onClick={handleUploadRosterShift}
                        color="primary"
                        disabled={!files.rosterShift || loading.uploadRosterShift}
                    >
                        {loading.uploadRosterShift && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.deleteRoster} onClose={() => handleCloseDialog('deleteRoster')}>
                <DialogTitle>Delete Roster</DialogTitle>
                <DialogContent>
                    <Typography variant="subtitle2" gutterBottom>
                        Roster Date (date only)
                    </Typography>
                    <TextField
                        type="date"
                        fullWidth
                        InputLabelProps={{
                            shrink: true,
                        }}
                        value={form.rosterDate}
                        onChange={(e) => handleFormFieldChange('rosterDate', e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('deleteRoster')}>Cancel</Button>
                    <Button
                        onClick={handleDeleteRoster}
                        color="error"
                        disabled={!form.rosterDate || loading.deleteRoster}
                    >
                        {loading.deleteRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.deleteDutyRoster} onClose={() => handleCloseDialog('deleteDutyRoster')}>
                <DialogTitle>Delete Roster (Charan Tv)</DialogTitle>
                <DialogContent>
                    <Typography variant="subtitle2" gutterBottom>
                        Week Starting Date
                    </Typography>
                    <TextField
                        type="date"
                        fullWidth
                        InputLabelProps={{
                            shrink: true,
                        }}
                        value={form.weekStartingDate}
                        onChange={(e) => handleFormFieldChange('weekStartingDate', e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('deleteDutyRoster')}>Cancel</Button>
                    <Button
                        onClick={handleDeleteDutyRoster}
                        color="error"
                        disabled={!form.weekStartingDate || loading.deleteDutyRoster}
                    >
                        {loading.deleteDutyRoster && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.deleteRosterShift} onClose={() => handleCloseDialog('deleteRosterShift')}>
                <DialogTitle>Delete Roster Shift</DialogTitle>
                <DialogContent>
                    <Typography variant="subtitle2" gutterBottom>
                        Shift Date (date only)
                    </Typography>
                    <TextField
                        type="date"
                        fullWidth
                        InputLabelProps={{
                            shrink: true,
                        }}
                        value={form.rosterShiftDate}
                        onChange={(e) => handleFormFieldChange('rosterShiftDate', e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('deleteRosterShift')}>Cancel</Button>
                    <Button
                        onClick={handleDeleteRosterShift}
                        color="error"
                        disabled={!form.rosterShiftDate || loading.deleteRosterShift}
                    >
                        {loading.deleteRosterShift && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.getAttendance} onClose={() => handleCloseDialog('getAttendance')}>
                <DialogTitle>Get All Attendance by User ID</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="User ID"
                        type="text"
                        fullWidth
                        value={form.userId}
                        onChange={(e) => handleFormFieldChange('userId', e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('getAttendance')}>Cancel</Button>
                    <Button
                        onClick={handleGetAttendance}
                        color="primary"
                        disabled={!form.userId || loading.downloadAttendance}
                    >
                        {loading.downloadAttendance && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Get Attendance
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.getAttendanceByDate} onClose={() => handleCloseDialog('getAttendanceByDate')}>
                <DialogTitle>Get All Attendance by User ID and Date</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="User ID"
                        type="text"
                        fullWidth
                        value={form.userId}
                        onChange={(e) => handleFormFieldChange('userId', e.target.value)}
                        sx={{ mb: 2 }}
                    />

                    <Typography variant="subtitle2" gutterBottom>
                        Date
                    </Typography>
                    <TextField
                        type="date"
                        fullWidth
                        InputLabelProps={{
                            shrink: true,
                        }}
                        value={form.startDate}
                        onChange={(e) => handleFormFieldChange('startDate', e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('getAttendanceByDate')}>Cancel</Button>
                    <Button
                        onClick={handleGetAttendanceByDate}
                        color="primary"
                        disabled={!form.userId || !form.startDate || loading.downloadAttendanceByDate}
                    >
                        {loading.downloadAttendanceByDate && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Get Attendance
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={dialogs.getAttendanceByMonth} onClose={() => handleCloseDialog('getAttendanceByMonth')}>
                <DialogTitle>Get All Attendance by User ID and Month</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="User ID"
                        type="text"
                        fullWidth
                        value={form.userId}
                        onChange={(e) => handleFormFieldChange('userId', e.target.value)}
                        sx={{ mb: 2 }}
                    />

                    <FormControl fullWidth sx={{ mb: 2 }}>
                        <InputLabel>Year</InputLabel>
                        <Select
                            value={form.selectedYear}
                            onChange={(e) => handleFormFieldChange('selectedYear', e.target.value)}
                            label="Year"
                        >
                            {years.map((year) => (
                                <MenuItem key={year} value={year}>
                                    {year}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth>
                        <InputLabel>Month</InputLabel>
                        <Select
                            value={form.selectedMonth}
                            onChange={(e) => handleFormFieldChange('selectedMonth', e.target.value)}
                            label="Month"
                        >
                            {months.map((month) => (
                                <MenuItem key={month.value} value={month.value}>
                                    {month.label}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => handleCloseDialog('getAttendanceByMonth')}>Cancel</Button>
                    <Button
                        onClick={handleGetAttendanceByMonth}
                        color="primary"
                        disabled={!form.userId || !form.selectedYear || !form.selectedMonth || loading.downloadAttendanceByMonth}
                    >
                        {loading.downloadAttendanceByMonth && <CircularProgress size={20} sx={{ mr: 1 }} />}
                        Get Attendance
                    </Button>
                </DialogActions>
            </Dialog>
        </Paper>
    );
};

export default Other;
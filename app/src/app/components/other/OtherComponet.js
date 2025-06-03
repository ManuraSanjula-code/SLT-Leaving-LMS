import React, {use, useState} from 'react';
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
    Paper
} from '@mui/material';
import {useSelector, useDispatch} from 'react-redux';

const Other = () => {
    const {userDetails, loading} = useSelector((state) => state.auth);

    // State for file uploads
    const [rosterFile, setRosterFile] = useState(null);
    const [rosterShiftFile, setRosterShiftFile] = useState(null);

    // State for dialogs
    const [openUploadRoster, setOpenUploadRoster] = useState(false);
    const [openUploadRosterShift, setOpenUploadRosterShift] = useState(false);
    const [openDeleteRoster, setOpenDeleteRoster] = useState(false);
    const [openDeleteRosterShift, setOpenDeleteRosterShift] = useState(false);
    const [openGetAttendance, setOpenGetAttendance] = useState(false);
    const [openGetAttendanceByDate, setOpenGetAttendanceByDate] = useState(false);
    // New dialogs for duty roster
    const [openUploadDutyRoster, setOpenUploadDutyRoster] = useState(false);
    const [openDeleteDutyRoster, setOpenDeleteDutyRoster] = useState(false);

    // State for user inputs
    const [userId, setUserId] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [rosterDate, setRosterDate] = useState('');
    const [rosterShiftDate, setRosterShiftDate] = useState('');

    // New state for duty roster upload fields
    const [dutyRosterFile, setDutyRosterFile] = useState(null);
    const [rosterName, setRosterName] = useState('CharanaTV_MCR');
    const [weekStartingDate, setWeekStartingDate] = useState('');

    // Function to handle roster file upload
    const handleRosterFileChange = (event) => {
        setRosterFile(event.target.files[0]);
    };

    // Function to handle roster shift file upload
    const handleRosterShiftFileChange = (event) => {
        setRosterShiftFile(event.target.files[0]);
    };

    // Function to handle duty roster file upload
    const handleDutyRosterFileChange = (event) => {
        setDutyRosterFile(event.target.files[0]);
    };

    // Function handlers for each action
    const handleUploadRoster = () => {
        if (!rosterFile) {
            alert('Please select a file first');
            return;
        }

        // Create FormData object to send the file
        const formData = new FormData();
        formData.append('file', rosterFile);

        // Make the API call to upload roster (original endpoint)
        fetch('http://localhost:8080/api/roster/upload/employee', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    alert('Roster uploaded successfully!');
                    setOpenUploadRoster(false);
                    setRosterFile(null);
                } else {
                    throw new Error('Failed to upload roster');
                }
            })
            .catch(error => {
                console.error('Error uploading roster:', error);
                alert(`Error uploading roster: ${error.message}`);
            });
    };

    const handleUploadDutyRoster = () => {
        if (!dutyRosterFile) {
            alert('Please select a file first');
            return;
        }

        if (!weekStartingDate) {
            alert('Please select a week starting date');
            return;
        }

        const formData = new FormData();
        formData.append('file', dutyRosterFile, dutyRosterFile.name);
        formData.append('rosterName', rosterName);
        formData.append('weekStartingDate', weekStartingDate);

        fetch('http://localhost:8080/api/duty-roster/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    alert('Duty Roster uploaded successfully!');
                    setOpenUploadDutyRoster(false);
                    setDutyRosterFile(null);
                    setWeekStartingDate('');
                } else {
                    throw new Error('Failed to upload duty roster');
                }
            })
            .catch(error => {
                console.error('Error uploading duty roster:', error);
                alert(`Error uploading duty roster: ${error.message}`);
            });
    };

    const handleUploadRosterShift = () => {
        if (!rosterShiftFile) {
            alert('Please select a file first');
            return;
        }

        // Create FormData object to send the file
        const formData = new FormData();
        formData.append('file', rosterShiftFile);

        // Make the API call to upload roster shift
        fetch('http://localhost:8080/api/roster/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    alert('Roster shift uploaded successfully!');
                    setOpenUploadRosterShift(false);
                    setRosterShiftFile(null);
                } else {
                    throw new Error('Failed to upload roster shift');
                }
            })
            .catch(error => {
                console.error('Error uploading roster shift:', error);
                alert(`Error uploading roster shift: ${error.message}`);
            });
    };

    const handleDeleteRoster = () => {
        // Format date to only include the date part (YYYY-MM-DD)
        const dateOnly = rosterDate.split('T')[0];

        // Make the DELETE request to the API (original endpoint)
        fetch(`http://localhost:8080/api/attendance/${dateOnly}/roster`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(response => {
                if (response.ok) {
                    alert(`Roster for date ${dateOnly} deleted successfully!`);
                } else {
                    throw new Error('Failed to delete roster');
                }
            })
            .catch(error => {
                console.error('Error deleting roster:', error);
                alert(`Error deleting roster: ${error.message}`);
            })
            .finally(() => {
                setOpenDeleteRoster(false);
                setRosterDate('');
            });
    };

    const handleDeleteDutyRoster = () => {
        if (!weekStartingDate) {
            alert('Please select a week starting date');
            return;
        }

        fetch(`http://localhost:8080/api/duty-roster/charana-tv/delete/${weekStartingDate}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(response => {
                if (response.ok) {
                    alert(`Duty Roster ${rosterName} for week starting ${weekStartingDate} deleted successfully!`);
                } else {
                    throw new Error('Failed to delete duty roster');
                }
            })
            .catch(error => {
                console.error('Error deleting duty roster:', error);
                alert(`Error deleting duty roster: ${error.message}`);
            })
            .finally(() => {
                setOpenDeleteDutyRoster(false);
                setWeekStartingDate('');
            });
    };

    const handleDeleteRosterShift = () => {
        const dateOnly = rosterShiftDate.split('T')[0];

        // Make the DELETE request to the API
        fetch(`http://localhost:8080/api/attendance/${dateOnly}/roster-shifts`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(response => {
                if (response.ok) {
                    alert(`Roster shift for date ${dateOnly} deleted successfully!`);
                } else {
                    throw new Error('Failed to delete roster shift');
                }
            })
            .catch(error => {
                console.error('Error deleting roster shift:', error);
                alert(`Error deleting roster shift: ${error.message}`);
            })
            .finally(() => {
                setOpenDeleteRosterShift(false);
                setRosterShiftDate('');
            });
    };

    const handleGetAttendance = () => {
        if (!userId) {
            alert('Please enter a user ID');
            return;
        }

        // Get logged-in user ID from session storage
        const loggedInUserId = sessionStorage.getItem('userId');

        if (!loggedInUserId) {
            alert('User session not found. Please log in again.');
            return;
        }

        // Form the URL with the dynamic parts
        const url = `http://localhost:8080/lms/employee/${userId}/excel/${loggedInUserId}`;

        // Trigger file download
        fetch(url, {
            method: 'GET',
            credentials: 'include', // This includes cookies in the request
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch attendance data');
                }
                return response.blob();
            })
            .then(blob => {
                // Create a URL for the blob
                const downloadUrl = window.URL.createObjectURL(blob);

                // Create a temporary link and trigger download
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = `employee_report_${userId}.xlsx`;
                document.body.appendChild(a);
                a.click();

                // Clean up
                window.URL.revokeObjectURL(downloadUrl);
                document.body.removeChild(a);

                // Close dialog and reset state
                setOpenGetAttendance(false);
                setUserId('');
            })
            .catch(error => {
                console.error('Error downloading attendance report:', error);
                alert(`Error downloading attendance report: ${error.message}`);
            });
    };

    const handleGetAttendanceByDate = () => {
        if (!userId) {
            alert('Please enter a user ID');
            return;
        }

        if (!startDate) {
            alert('Please select a date');
            return;
        }

        // Get logged-in user ID from session storage
        const loggedInUserId = sessionStorage.getItem('userId');

        if (!loggedInUserId) {
            alert('User session not found. Please log in again.');
            return;
        }

        // Extract just the date part (YYYY-MM-DD) from the datetime
        const dateOnly = startDate.split('T')[0];

        // Form the URL with the dynamic parts
        const url = `http://localhost:8080/lms/employee/${userId}/excel/date/${dateOnly}/${loggedInUserId}`;

        // Trigger file download
        fetch(url, {
            method: 'GET',
            credentials: 'include', // This includes cookies in the request
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch attendance data for the selected date');
                }
                return response.blob();
            })
            .then(blob => {
                // Create a URL for the blob
                const downloadUrl = window.URL.createObjectURL(blob);

                // Create a temporary link and trigger download
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = `employee_report_${userId}_${dateOnly}.xlsx`;
                document.body.appendChild(a);
                a.click();

                // Clean up
                window.URL.revokeObjectURL(downloadUrl);
                document.body.removeChild(a);

                // Close dialog and reset state
                setOpenGetAttendanceByDate(false);
                setUserId('');
                setStartDate('');
                setEndDate(''); // Also reset endDate even though we're not using it
            })
            .catch(error => {
                console.error('Error downloading date-specific attendance report:', error);
                alert(`Error downloading attendance report: ${error.message}`);
            });
    };

    return (
        <Paper elevation={3} sx={{p: 3, m: 2}}>
            <Typography variant="h5" gutterBottom>
                Roster Management
            </Typography>

            <Grid container spacing={2} sx={{mt: 2}}>
                {userDetails.roaster && (userDetails.highestRolePriority > 0  && userDetails.highestRolePriority < 10) && (
                    <>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                fullWidth
                                onClick={() => setOpenUploadRosterShift(true)}
                            >
                                Upload Roster Shift
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                fullWidth
                                onClick={() => setOpenUploadRoster(true)}
                            >
                                Upload Roster
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="info"
                                fullWidth
                                onClick={() => setOpenUploadDutyRoster(true)}
                            >
                                For Charana Tv
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="error"
                                fullWidth
                                onClick={() => setOpenDeleteRosterShift(true)}
                            >
                                Delete Roster Shift
                            </Button>
                        </Grid>

                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="error"
                                fullWidth
                                onClick={() => setOpenDeleteRoster(true)}
                            >
                                Delete Roster
                            </Button>
                        </Grid>

                        <Grid item xs={12} sm={6} md={4}>
                            <Button
                                variant="contained"
                                color="warning"
                                fullWidth
                                onClick={() => setOpenDeleteDutyRoster(true)}
                            >
                                Delete Roster (For Charana Tv)
                            </Button>
                        </Grid>
                    </>
                )}

                <Grid item xs={12} sm={6} md={4}>
                    <Button
                        variant="contained"
                        color="secondary"
                        fullWidth
                        onClick={() => setOpenGetAttendance(true)}
                    >
                        Get All Attendance by UserId
                    </Button>
                </Grid>

                <Grid item xs={12} sm={6} md={4}>
                    <Button
                        variant="contained"
                        color="secondary"
                        fullWidth
                        onClick={() => setOpenGetAttendanceByDate(true)}
                    >
                        Get All Attendance by UserId and Date
                    </Button>
                </Grid>
            </Grid>

            {/* Upload Roster Dialog */}
            <Dialog open={openUploadRoster} onClose={() => setOpenUploadRoster(false)}>
                <DialogTitle>Upload Roster</DialogTitle>
                <DialogContent>
                    <Box sx={{mt: 2}}>
                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{display: 'none'}}
                            id="roster-file-upload"
                            type="file"
                            onChange={handleRosterFileChange}
                        />
                        <label htmlFor="roster-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {rosterFile && (
                            <Typography variant="body2" sx={{mt: 1}}>
                                Selected file: {rosterFile.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenUploadRoster(false)}>Cancel</Button>
                    <Button
                        onClick={handleUploadRoster}
                        color="primary"
                        disabled={!rosterFile}
                    >
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={openUploadDutyRoster} onClose={() => setOpenUploadDutyRoster(false)}>
                <DialogTitle>Upload Roster For Charana Tv</DialogTitle>
                <DialogContent>
                    <Box sx={{mt: 2}}>
                        <TextField
                            label="Roster Name"
                            fullWidth
                            value={rosterName}
                            onChange={(e) => setRosterName(e.target.value)}
                            sx={{mb: 2}}
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
                            value={weekStartingDate}
                            onChange={(e) => setWeekStartingDate(e.target.value)}
                            sx={{mb: 2}}
                        />

                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{display: 'none'}}
                            id="duty-roster-file-upload"
                            type="file"
                            onChange={handleDutyRosterFileChange}
                        />
                        <label htmlFor="duty-roster-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {dutyRosterFile && (
                            <Typography variant="body2" sx={{mt: 1}}>
                                Selected file: {dutyRosterFile.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenUploadDutyRoster(false)}>Cancel</Button>
                    <Button
                        onClick={handleUploadDutyRoster}
                        color="primary"
                        disabled={!dutyRosterFile || !weekStartingDate}
                    >
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Upload Roster Shift Dialog */}
            <Dialog open={openUploadRosterShift} onClose={() => setOpenUploadRosterShift(false)}>
                <DialogTitle>Upload Roster Shift</DialogTitle>
                <DialogContent>
                    <Box sx={{mt: 2}}>
                        <input
                            accept=".csv,.xlsx,.xls"
                            style={{display: 'none'}}
                            id="roster-shift-file-upload"
                            type="file"
                            onChange={handleRosterShiftFileChange}
                        />
                        <label htmlFor="roster-shift-file-upload">
                            <Button variant="outlined" component="span">
                                Select File
                            </Button>
                        </label>
                        {rosterShiftFile && (
                            <Typography variant="body2" sx={{mt: 1}}>
                                Selected file: {rosterShiftFile.name}
                            </Typography>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenUploadRosterShift(false)}>Cancel</Button>
                    <Button
                        onClick={handleUploadRosterShift}
                        color="primary"
                        disabled={!rosterShiftFile}
                    >
                        Upload
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Roster Dialog */}
            <Dialog open={openDeleteRoster} onClose={() => setOpenDeleteRoster(false)}>
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
                        value={rosterDate}
                        onChange={(e) => setRosterDate(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteRoster(false)}>Cancel</Button>
                    <Button
                        onClick={handleDeleteRoster}
                        color="error"
                        disabled={!rosterDate}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={openDeleteDutyRoster} onClose={() => setOpenDeleteDutyRoster(false)}>
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
                        value={weekStartingDate}
                        onChange={(e) => setWeekStartingDate(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteDutyRoster(false)}>Cancel</Button>
                    <Button
                        onClick={handleDeleteDutyRoster}
                        color="error"
                        disabled={!weekStartingDate}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Roster Shift Dialog */}
            <Dialog open={openDeleteRosterShift} onClose={() => setOpenDeleteRosterShift(false)}>
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
                        value={rosterShiftDate}
                        onChange={(e) => setRosterShiftDate(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteRosterShift(false)}>Cancel</Button>
                    <Button
                        onClick={handleDeleteRosterShift}
                        color="error"
                        disabled={!rosterShiftDate}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Get Attendance Dialog */}
            <Dialog open={openGetAttendance} onClose={() => setOpenGetAttendance(false)}>
                <DialogTitle>Get All Attendance by User ID</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="User ID"
                        type="text"
                        fullWidth
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenGetAttendance(false)}>Cancel</Button>
                    <Button
                        onClick={handleGetAttendance}
                        color="primary"
                        disabled={!userId}
                    >
                        Get Attendance
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Get Attendance by Date Dialog */}
            <Dialog open={openGetAttendanceByDate} onClose={() => setOpenGetAttendanceByDate(false)}>
                <DialogTitle>Get All Attendance by User ID and Date</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="User ID"
                        type="text"
                        fullWidth
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                        sx={{mb: 2}}
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
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenGetAttendanceByDate(false)}>Cancel</Button>
                    <Button
                        onClick={handleGetAttendanceByDate}
                        color="primary"
                        disabled={!userId || !startDate}
                    >
                        Get Attendance
                    </Button>
                </DialogActions>
            </Dialog>
        </Paper>
    );
};

export default Other;
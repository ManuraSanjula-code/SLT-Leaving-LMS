"use client";

import React, { useState, useEffect } from "react";
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Checkbox,
  Button,
  IconButton,
  Pagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Alert,
  Chip,
  CircularProgress
} from "@mui/material";
import { Check as CheckIcon, Visibility as VisibilityIcon } from "@mui/icons-material";
import { useSelector, useDispatch } from "react-redux";
import {
  fetchUnauthorizedLeaves,
  resolveUnauthorizedLeave,
  approveUnauthorizedLeave,
  bulkResolveUnauthorizedLeaves,
  deleteMultipleUnauthorizedLeaves,
  setPage,
  setPageSize,
  clearError
} from "../../../../lib/redux/redux-lms/unauthorized-leaves/unauthorizedLeavesSlice";

const UnauthorizedLeaves = ({ isAdmin = false }) => {
  const dispatch = useDispatch();
  const {
    leaves,
    loading,
    error,
    page,
    pageSize
  } = useSelector((state) => state.unauthorizedLeaves);

  const [searchQuery, setSearchQuery] = useState("");
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");
  const [attendanceTypeFilter, setAttendanceTypeFilter] = useState("All");
  const [leaveStatusFilter, setLeaveStatusFilter] = useState("All");
  const [payStatusFilter, setPayStatusFilter] = useState("All");
  const [resolutionFilter, setResolutionFilter] = useState("All");
  const [selected, setSelected] = useState([]);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedLeave, setSelectedLeave] = useState(null);

  useEffect(() => {
    const userId = sessionStorage.getItem('userId');
    if (userId || isAdmin) {
      dispatch(fetchUnauthorizedLeaves({ isAdmin, page, pageSize, userId }));
    }
  }, [page, pageSize, dispatch, isAdmin]);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const formatTime = (timeString) => {
    if (!timeString) return "-";
    return timeString;
  };

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const handlePageSizeChange = (event) => {
    dispatch(setPageSize(parseInt(event.target.value)));
  };

  const handleResolveLeave = (id) => {
    dispatch(resolveUnauthorizedLeave(id));
  };

  const handleApproveLeave = (id) => {
    dispatch(approveUnauthorizedLeave(id));
  };

  const handleViewDetails = (leave) => {
    setSelectedLeave(leave);
    setDetailDialogOpen(true);
  };

  const handleCloseDetails = () => {
    setDetailDialogOpen(false);
  };

  const getAttendanceTypeChip = (attendanceType) => {
    switch (attendanceType) {
      case "FULL_DAY":
        return <Chip label="Full Day" color="success" size="small" />;
      case "HALF_DAY":
        return <Chip label="Half Day" color="warning" size="small" />;
      case "ABSENT":
        return <Chip label="Absent" color="error" size="small" />;
      default:
        return null;
    }
  };

  const getLeaveStatusChip = (leaveStatus) => {
    switch (leaveStatus) {
      case "NO_LEAVE":
        return <Chip label="No Leave" color="default" size="small" />;
      case "FULL_LEAVE":
        return <Chip label="Full Leave" color="info" size="small" />;
      case "SHORT_LEAVE":
        return <Chip label="Short Leave" color="warning" size="small" />;
      case "LEAVE_REQUESTED":
        return <Chip label="Leave Requested" color="secondary" size="small" />;
      case "LEAVE_APPROVED":
        return <Chip label="Leave Approved" color="success" size="small" />;
      default:
        return null;
    }
  };

  const getPayStatusChip = (payStatus) => {
    if (payStatus === "NO_PAY") {
      return <Chip label="No Pay" color="error" size="small" />;
    }
    return null;
  };

  const getResolveStatusChip = (resolve) => {
    if (resolve === null) {
      return <Chip label="Unresolved" color="default" size="small" />;
    }
    switch (resolve) {
      case "VIA_MOVEMENT":
        return <Chip label="Via Movement" color="success" size="small" />;
      case "VIA_LEAVE":
        return <Chip label="Via Leave" color="success" size="small" />;
      case "EXPIRED":
        return <Chip label="Expired" color="error" size="small" />;
      default:
        return <Chip label="Resolved" color="success" size="small" />;
    }
  };

  const filteredLeaves = leaves.content ? leaves.content.filter((leave) => {
    const matchesSearchQuery =
        leave.employeeId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        leave.publicId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        leave.issueDescription?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        leave.userId?.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesAttendanceTypeFilter =
        attendanceTypeFilter === "All" ||
        (attendanceTypeFilter === "FULL_DAY" && leave.attendanceType === "FULL_DAY") ||
        (attendanceTypeFilter === "HALF_DAY" && leave.attendanceType === "HALF_DAY") ||
        (attendanceTypeFilter === "ABSENT" && leave.attendanceType === "ABSENT");

    const matchesLeaveStatusFilter =
        leaveStatusFilter === "All" ||
        leave.leaveStatus === leaveStatusFilter;

    const matchesPayStatusFilter =
        payStatusFilter === "All" ||
        (payStatusFilter === "NO_PAY" && leave.payStatus === "NO_PAY") ||
        (payStatusFilter === "PAID" && leave.payStatus !== "NO_PAY");

    const leaveDate = new Date(leave.date);
    const matchesStartDateFilter = !startDateFilter || leaveDate >= new Date(startDateFilter);
    const matchesEndDateFilter = !endDateFilter || leaveDate <= new Date(endDateFilter);

    const matchesResolutionFilter =
        resolutionFilter === "All" ||
        (resolutionFilter === "Resolved" && leave.resolve !== null) ||
        (resolutionFilter === "Unresolved" && leave.resolve === null);

    return matchesSearchQuery && matchesAttendanceTypeFilter && matchesLeaveStatusFilter &&
        matchesPayStatusFilter && matchesStartDateFilter && matchesEndDateFilter && matchesResolutionFilter;
  }) : [];

  const handleSelect = (id) => {
    if (selected.includes(id)) {
      setSelected((prev) => prev.filter((item) => item !== id));
    } else {
      setSelected((prev) => [...prev, id]);
    }
  };

  const handleSelectAll = () => {
    if (selected.length === filteredLeaves.length) {
      setSelected([]);
    } else {
      setSelected(filteredLeaves.map((leave) => leave.id));
    }
  };

  const handleDeleteAllSelected = () => {
    dispatch(deleteMultipleUnauthorizedLeaves(selected));
    setSelected([]);
  };

  const handleBulkResolve = () => {
    dispatch(bulkResolveUnauthorizedLeaves(selected));
    setSelected([]);
  };

  const handlePageChange = (event, value) => {
    dispatch(setPage(value - 1));
  };

  useEffect(() => {
    return () => {
      dispatch(clearError());
    };
  }, [dispatch]);

  return (
      <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            minHeight: "100vh",
          }}
      >
        <CssBaseline />
        <Container maxWidth="lg">
          <Box sx={{ mt: 4, mb: 4 }}>
            <Box sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              mb: 3
            }}>
              <Typography variant="h4">
                Unauthorized Leave
              </Typography>

              <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                <InputLabel id="rows-per-page-label">Rows per page</InputLabel>
                <Select
                    labelId="rows-per-page-label"
                    value={pageSize}
                    onChange={handlePageSizeChange}
                    label="Rows per page"
                    size="small"
                >
                  <MenuItem value={5}>5</MenuItem>
                  <MenuItem value={10}>10</MenuItem>
                  <MenuItem value={25}>25</MenuItem>
                  <MenuItem value={50}>50</MenuItem>
                  <MenuItem value={100}>100</MenuItem>
                </Select>
              </FormControl>
            </Box>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                  {error}
                </Alert>
            )}

            <TextField
                label="Search by Employee ID, User ID or Issue Description"
                variant="outlined"
                fullWidth
                value={searchQuery}
                onChange={handleSearchChange}
                sx={{ mb: 2 }}
            />

            <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Attendance Type</InputLabel>
                <Select
                    value={attendanceTypeFilter}
                    onChange={(e) => setAttendanceTypeFilter(e.target.value)}
                    label="Attendance Type"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="FULL_DAY">Full Day</MenuItem>
                  <MenuItem value="HALF_DAY">Half Day</MenuItem>
                  <MenuItem value="ABSENT">Absent</MenuItem>
                </Select>
              </FormControl>

              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Leave Status</InputLabel>
                <Select
                    value={leaveStatusFilter}
                    onChange={(e) => setLeaveStatusFilter(e.target.value)}
                    label="Leave Status"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="NO_LEAVE">No Leave</MenuItem>
                  <MenuItem value="FULL_LEAVE">Full Leave</MenuItem>
                  <MenuItem value="SHORT_LEAVE">Short Leave</MenuItem>
                  <MenuItem value="LEAVE_REQUESTED">Leave Requested</MenuItem>
                  <MenuItem value="LEAVE_APPROVED">Leave Approved</MenuItem>
                </Select>
              </FormControl>

              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Pay Status</InputLabel>
                <Select
                    value={payStatusFilter}
                    onChange={(e) => setPayStatusFilter(e.target.value)}
                    label="Pay Status"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="NO_PAY">No Pay</MenuItem>
                  <MenuItem value="PAID">Paid</MenuItem>
                </Select>
              </FormControl>

              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Resolution Status</InputLabel>
                <Select
                    value={resolutionFilter}
                    onChange={(e) => setResolutionFilter(e.target.value)}
                    label="Resolution Status"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="Resolved">Resolved</MenuItem>
                  <MenuItem value="Unresolved">Unresolved</MenuItem>
                </Select>
              </FormControl>

              <TextField
                  label="Start Date"
                  type="date"
                  variant="outlined"
                  value={startDateFilter}
                  onChange={(e) => setStartDateFilter(e.target.value)}
                  InputLabelProps={{ shrink: true }}
              />

              <TextField
                  label="End Date"
                  type="date"
                  variant="outlined"
                  value={endDateFilter}
                  onChange={(e) => setEndDateFilter(e.target.value)}
                  InputLabelProps={{ shrink: true }}
              />
            </Box>

            <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
              {!isAdmin && selected.length > 0 && (
                  <>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleBulkResolve}
                        disabled={selected.length === 0}
                    >
                      Resolve Selected ({selected.length})
                    </Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={handleDeleteAllSelected}
                        disabled={selected.length === 0}
                    >
                      Delete Selected
                    </Button>
                  </>
              )}
            </Box>

            {loading ? (
                <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                  <CircularProgress />
                </Box>
            ) : (
                <>
                  <TableContainer component={Paper}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          {!isAdmin && (
                              <TableCell padding="checkbox">
                                <Checkbox
                                    indeterminate={
                                        selected.length > 0 && selected.length < filteredLeaves.length
                                    }
                                    checked={selected.length === filteredLeaves.length && filteredLeaves.length > 0}
                                    onChange={handleSelectAll}
                                />
                              </TableCell>
                          )}
                          <TableCell>Public ID</TableCell>
                          <TableCell>Employee ID</TableCell>
                          <TableCell>User ID</TableCell>
                          <TableCell>Date</TableCell>
                          <TableCell>Arrival Time</TableCell>
                          <TableCell>Left Time</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Due Date</TableCell>
                          <TableCell>Issue Description</TableCell>
                          <TableCell>Action</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {filteredLeaves.length === 0 ? (
                            <TableRow>
                              <TableCell colSpan={!isAdmin ? 11 : 10} align="center">
                                No unauthorized leaves found
                              </TableCell>
                            </TableRow>
                        ) : (
                            filteredLeaves.map((leave) => (
                                <TableRow key={leave.id}>
                                  {!isAdmin && (
                                      <TableCell padding="checkbox">
                                        <Checkbox
                                            checked={selected.includes(leave.id)}
                                            onChange={() => handleSelect(leave.id)}
                                            disabled={leave.resolve !== null}
                                        />
                                      </TableCell>
                                  )}
                                  <TableCell>{leave.publicId}</TableCell>
                                  <TableCell>{leave.employeeId}</TableCell>
                                  <TableCell>{leave.userId}</TableCell>
                                  <TableCell>{formatDate(leave.date)}</TableCell>
                                  <TableCell>{formatTime(leave.arrivalTime)}</TableCell>
                                  <TableCell>{formatTime(leave.leftTime)}</TableCell>
                                  <TableCell>
                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                      {getAttendanceTypeChip(leave.attendanceType)}
                                      {getLeaveStatusChip(leave.leaveStatus)}
                                      {getPayStatusChip(leave.payStatus)}
                                      {getResolveStatusChip(leave.resolve)}
                                    </Box>
                                  </TableCell>
                                  <TableCell>{formatDate(leave.dueDateForUA)}</TableCell>
                                  <TableCell sx={{ maxWidth: 250, overflow: "hidden", textOverflow: "ellipsis" }}>
                                    {leave.issueDescription || 'N/A'}
                                  </TableCell>
                                  <TableCell>
                                    {!isAdmin && leave.resolve === null && (
                                        <IconButton
                                            onClick={() => handleApproveLeave(leave.id)}
                                            color="success"
                                            size="small"
                                            title="Approve"
                                        >
                                          <CheckIcon />
                                        </IconButton>
                                    )}
                                    {leave.resolve === null && !isAdmin && (
                                        <Button
                                            variant="contained"
                                            color="primary"
                                            size="small"
                                            onClick={() => handleResolveLeave(leave.id)}
                                            sx={{ mx: 1 }}
                                        >
                                          Resolve
                                        </Button>
                                    )}
                                    {!isAdmin && (
                                        <IconButton
                                            onClick={() => handleViewDetails(leave)}
                                            color="info"
                                            size="small"
                                            title="View Details"
                                        >
                                          <VisibilityIcon />
                                        </IconButton>
                                    )}
                                  </TableCell>
                                </TableRow>
                            ))
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  <Box sx={{ display: "flex", justifyContent: "flex-end", alignItems: "center", mt: 2 }}>
                    <Typography variant="body2" sx={{ mr: 2 }}>
                      {leaves.totalElements > 0 ?
                          `Showing ${page * pageSize + 1} to 
                          ${Math.min((page + 1) * pageSize, leaves.totalElements)} 
                          of ${leaves.totalElements} entries` :
                          'No entries to display'}
                    </Typography>
                    <Pagination
                        count={leaves.totalPages || 1}
                        page={page + 1}
                        onChange={handlePageChange}
                        color="primary"
                    />
                  </Box>
                </>
            )}
          </Box>
        </Container>

        <Dialog
            open={detailDialogOpen}
            onClose={handleCloseDetails}
            maxWidth="md"
            fullWidth
        >
          <DialogTitle>
            Leave Details - {selectedLeave?.employeeId}
          </DialogTitle>
          <DialogContent dividers>
            {selectedLeave && (
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle1">Basic Information</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>ID:</strong> {selectedLeave.id}</Typography>
                      <Typography><strong>Public ID:</strong> {selectedLeave.publicId}</Typography>
                      <Typography><strong>Employee ID:</strong> {selectedLeave.employeeId}</Typography>
                      <Typography><strong>User ID:</strong> {selectedLeave.userId}</Typography>
                      <Typography><strong>Date:</strong> {formatDate(selectedLeave.date)}</Typography>
                      <Typography><strong>Due Date:</strong> {formatDate(selectedLeave.dueDateForUA)}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle1">Time Information</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>Arrival Time:</strong> {selectedLeave.arrivalTime || 'N/A'}</Typography>
                      <Typography><strong>Left Time:</strong> {selectedLeave.leftTime || 'N/A'}</Typography>
                      <Typography><strong>Arrival Date:</strong> {formatDate(selectedLeave.arrivalDate) || 'N/A'}</Typography>
                      <Typography><strong>Terminal ID:</strong> {selectedLeave.terminalId || 'N/A'}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="subtitle1">Leave Status</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>Status:</strong> {selectedLeave.resolve ? 'Resolved' : 'Unresolved'}</Typography>
                      <Typography><strong>Attendance Type:</strong> {selectedLeave.attendanceType || 'Not Set'}</Typography>
                      <Typography><strong>Leave Status:</strong> {selectedLeave.leaveStatus || 'Not Set'}</Typography>
                      <Typography><strong>Pay Status:</strong> {selectedLeave.payStatus || 'Not Set'}</Typography>
                      <Typography><strong>Is Late:</strong> {selectedLeave.isLate ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Is Unauthorized:</strong> {selectedLeave.isUnauthorized ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Is Resolved:</strong> {selectedLeave.isResolved ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Has Issues:</strong> {selectedLeave.hasIssues ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Is Active:</strong> {selectedLeave.isActive ? 'Yes' : 'No'}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="subtitle1">Issue Information</Typography>
                    <Paper sx={{ p: 2 }}>
                      <Typography><strong>Issue Description:</strong></Typography>
                      <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                        {selectedLeave.issueDescription || 'No description provided'}
                      </Paper>
                    </Paper>
                  </Grid>
                </Grid>
            )}
          </DialogContent>
          <DialogActions>
            {!isAdmin && selectedLeave && selectedLeave.resolve === null && (
                <Button
                    onClick={() => {
                      handleResolveLeave(selectedLeave.id);
                      handleCloseDetails();
                    }}
                    color="primary"
                    variant="contained"
                >
                  Resolve
                </Button>
            )}
            <Button onClick={handleCloseDetails} color="inherit">
              Close
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
  );
};

export default UnauthorizedLeaves;
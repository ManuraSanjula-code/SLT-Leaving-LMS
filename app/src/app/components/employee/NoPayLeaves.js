"use client";

import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchNoPayRecords,
  setSearchQuery,
  setStartDateFilter,
  setEndDateFilter,
  setUserIdFilter,
  setCurrentPage,
  setPageSize,
  deleteNoPayRecord
} from '../../../../lib/redux/redux-lms/no-pay/noPaySlice';
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
  IconButton,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Pagination,
  Tooltip,
  Switch,
  FormControlLabel,
} from "@mui/material";
import {
  Info as InfoIcon,
  FilterAlt as FilterIcon,
} from "@mui/icons-material";
import {DeleteIcon} from "lucide-react";

const ManageNoPay = ({ isAdmin = false, userId = null }) => {
  const dispatch = useDispatch();
  if (userId == null) userId = sessionStorage.getItem('userId');

  const noPayRecords = useSelector(state => state.noPay.records);
  const loading = useSelector(state => state.noPay.loading);
  const error = useSelector(state => state.noPay.error);
  const pagination = useSelector(state => state.noPay.pagination);
  const filters = useSelector(state => state.noPay.filters);

  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [adminMode, setAdminMode] = useState(isAdmin);

   userId = typeof window !== 'undefined' ? sessionStorage.getItem('userId') : null;

  useEffect(() => {
    if (userId) {
      dispatch(fetchNoPayRecords({
        isAdmin: adminMode,
        userId,
        page: pagination.currentPage,
        size: pagination.pageSize,
        userIdFilter: filters.userIdFilter
      }));
    }
  }, [
    dispatch,
    adminMode,
    userId,
    pagination.currentPage,
    pagination.pageSize,
    filters.userIdFilter
  ]);

  const handlePageChange = (event, value) => {
    dispatch(setCurrentPage(value - 1));
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = parseInt(event.target.value);
    dispatch(setPageSize(newSize));
  };

  const handleAdminModeToggle = (event) => {
    setAdminMode(event.target.checked);
    dispatch(setCurrentPage(0));
    dispatch(setUserIdFilter(""));
  };

  const handleSearchChange = (event) => {
    dispatch(setSearchQuery(event.target.value));
  };

  const handleStartDateChange = (event) => {
    dispatch(setStartDateFilter(event.target.value));
  };

  const handleEndDateChange = (event) => {
    dispatch(setEndDateFilter(event.target.value));
  };

  const handleUserIdFilterChange = (event) => {
    dispatch(setUserIdFilter(event.target.value));
  };

  const handleApplyFilters = () => {
    dispatch(setCurrentPage(0));

    dispatch(fetchNoPayRecords({
      isAdmin: adminMode,
      userId,
      page: 0,
      size: pagination.pageSize,
      userIdFilter: filters.userIdFilter
    }));
  };

  const filteredNoPayRecords = noPayRecords.filter((record) => {
    const employeeIdLower = (record.employeeID || "").toLowerCase();
    const commentLower = (record.comment || "").toLowerCase();
    const searchQueryLower = filters.searchQuery.toLowerCase();

    const matchesSearchQuery =
        employeeIdLower.includes(searchQueryLower) ||
        commentLower.includes(searchQueryLower);

    const matchesStartDateFilter =
        !filters.startDateFilter || record.happenDate >= filters.startDateFilter;

    const matchesEndDateFilter =
        !filters.endDateFilter || record.happenDate <= filters.endDateFilter;

    return (
        matchesSearchQuery &&
        matchesStartDateFilter &&
        matchesEndDateFilter
    );
  });

  const handleOpenDetailDialog = (record) => {
    setSelectedRecord(record);
    setDetailDialogOpen(true);
  };

  const getNoPayType = (record) => {
    if (record.absent) return "Absent";
    if (record.halfDay) return "Half Day";
    if (record.late) return "Late";
    if (record.unSuccessful) return "Unsuccessful";
    return "Unknown";
  };

  const checkIsAdminRole = () => {
    const userRole = typeof window !== 'undefined' ? sessionStorage.getItem('userRole') : null;
    return userRole && userRole.includes("ADMIN");
  };

  const handleDeleteRecord = async (recordId) => {
    const empId = sessionStorage.getItem('userId');
    if (window.confirm('Are you sure you want to delete this no pay record?')) {
      try {
        await dispatch(deleteNoPayRecord({ nopayid: recordId, empId })).unwrap();
        // Refresh the records after deletion
        dispatch(fetchNoPayRecords({
          isAdmin: adminMode,
          userId,
          page: pagination.currentPage,
          size: pagination.pageSize,
          userIdFilter: filters.userIdFilter
        }));
      } catch (error) {
        console.error('Failed to delete record:', error);
      }
    }
  }

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            No Pay Records
          </Typography>

          {checkIsAdminRole() && (
              <FormControlLabel
                  control={
                    <Switch
                        checked={adminMode}
                        onChange={handleAdminModeToggle}
                        color="primary"
                    />
                  }
                  label="Admin Mode"
                  sx={{ mb: 2 }}
              />
          )}

          {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                Error: {error}
              </Alert>
          )}

          {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                <CircularProgress />
              </Box>
          ) : (
              <>
                <Box sx={{ mb: 3 }}>
                  <TextField
                      label="Search by Employee ID or Comments"
                      variant="outlined"
                      fullWidth
                      value={filters.searchQuery}
                      onChange={handleSearchChange}
                      sx={{ mb: 2 }}
                  />

                  <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap", mb: 2 }}>
                    {adminMode && checkIsAdminRole() && (
                        <TextField
                            label="Filter by User ID"
                            variant="outlined"
                            value={filters.userIdFilter}
                            onChange={handleUserIdFilterChange}
                            placeholder="Enter employee ID"
                        />
                    )}

                    <TextField
                        label="Start Date"
                        type="date"
                        variant="outlined"
                        value={filters.startDateFilter}
                        onChange={handleStartDateChange}
                        InputLabelProps={{ shrink: true }}
                    />
                    <TextField
                        label="End Date"
                        type="date"
                        variant="outlined"
                        value={filters.endDateFilter}
                        onChange={handleEndDateChange}
                        InputLabelProps={{ shrink: true }}
                    />

                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleApplyFilters}
                        startIcon={<FilterIcon />}
                    >
                      Apply Filters
                    </Button>
                  </Box>
                </Box>

                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
                  <Typography variant="body2">
                    Showing {filteredNoPayRecords.length} of {pagination.totalElements} total no pay records
                  </Typography>

                  <FormControl variant="outlined" size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Records per page</InputLabel>
                    <Select
                        value={pagination.pageSize}
                        onChange={handlePageSizeChange}
                        label="Records per page"
                    >
                      <MenuItem value={5}>5</MenuItem>
                      <MenuItem value={10}>10</MenuItem>
                      <MenuItem value={25}>25</MenuItem>
                      <MenuItem value={50}>50</MenuItem>
                      <MenuItem value={100}>100</MenuItem>
                    </Select>
                  </FormControl>
                </Box>

                {noPayRecords.length === 0 ? (
                    <Alert severity="info">No pay records found.</Alert>
                ) : (
                    <TableContainer component={Paper}>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Employee ID</TableCell>
                            <TableCell>Date</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Submission Date</TableCell>
                            <TableCell>Comment</TableCell>
                            <TableCell>Actions</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {filteredNoPayRecords.map((record) => (
                              <TableRow key={record.id}>
                                <TableCell>{record.employeeID || ""}</TableCell>
                                <TableCell>{record.happenDate || "Not specified"}</TableCell>
                                <TableCell>{getNoPayType(record)}</TableCell>
                                <TableCell>{record.submissionDate || "Not specified"}</TableCell>
                                <TableCell>
                                  {record.comment ? (
                                      record.comment.length > 30
                                          ? `${record.comment.substring(0, 30)}...`
                                          : record.comment
                                  ) : "No comment"}
                                </TableCell>
                                <TableCell>
                                  <Tooltip title="View Details">
                                    <IconButton
                                        onClick={() => handleOpenDetailDialog(record)}
                                        color="primary"
                                    >
                                      <InfoIcon />
                                      {adminMode && (
                                          <Tooltip title="Delete Record">
                                            <IconButton
                                                onClick={() => handleDeleteRecord(record.publicId)}
                                                color="error"
                                            >
                                              <DeleteIcon />
                                            </IconButton>
                                          </Tooltip>
                                      )}
                                    </IconButton>
                                  </Tooltip>
                                </TableCell>
                              </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                )}

                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 3, gap: 2 }}>
                  {pagination.totalPages > 0 && (
                      <Pagination
                          count={pagination.totalPages}
                          page={pagination.currentPage + 1}
                          onChange={handlePageChange}
                          color="primary"
                          showFirstButton
                          showLastButton
                      />
                  )}
                </Box>
              </>
          )}
        </Box>


        <Dialog
            open={detailDialogOpen}
            onClose={() => setDetailDialogOpen(false)}
            maxWidth="md"
            fullWidth
        >
          <DialogTitle>No Pay Record Details</DialogTitle>
          {selectedRecord && (
              <DialogContent dividers>
                <TableContainer>
                  <Table>
                    <TableBody>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Employee ID
                        </TableCell>
                        <TableCell>{selectedRecord.employeeID}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Date
                        </TableCell>
                        <TableCell>{selectedRecord.happenDate}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Type
                        </TableCell>
                        <TableCell>{getNoPayType(selectedRecord)}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Submission Date
                        </TableCell>
                        <TableCell>{selectedRecord.submissionDate}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Comment
                        </TableCell>
                        <TableCell>{selectedRecord.comment || "No comment"}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Attendance ID
                        </TableCell>
                        <TableCell>{selectedRecord.attendance || "N/A"}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Record ID
                        </TableCell>
                        <TableCell>{selectedRecord.publicId}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </TableContainer>
              </DialogContent>
          )}
          <DialogActions>
            <Button onClick={() => setDetailDialogOpen(false)} color="primary">
              Close
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
  );
};

export default ManageNoPay;
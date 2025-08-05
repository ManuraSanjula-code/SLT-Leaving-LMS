import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const API_BASE_URL = 'http://192.168.3.20:8080';

export const uploadRoster = createAsyncThunk(
  'other/uploadRoster',
  async (file, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(`${API_BASE_URL}/api/roster/upload/employee`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Failed to upload roster');
      }

      return { message: 'Roster uploaded successfully!' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const uploadRosterShift = createAsyncThunk(
  'other/uploadRosterShift',
  async (file, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(`${API_BASE_URL}/api/roster/upload`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Failed to upload roster shift');
      }

      return { message: 'Roster shift uploaded successfully!' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const uploadDutyRoster = createAsyncThunk(
  'other/uploadDutyRoster',
  async ({ file, rosterName, weekStartingDate }, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('file', file, file.name);
      formData.append('rosterName', rosterName);
      formData.append('weekStartingDate', weekStartingDate);

      const response = await fetch(`${API_BASE_URL}/api/duty-roster/upload`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Failed to upload duty roster');
      }

      return { message: 'Duty Roster uploaded successfully!' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const deleteRoster = createAsyncThunk(
  'other/deleteRoster',
  async (date, { rejectWithValue }) => {
    try {
      const dateOnly = date.split('T')[0];
      const response = await fetch(`${API_BASE_URL}/api/attendance/${dateOnly}/roster`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to delete roster');
      }

      return { message: `Roster for date ${dateOnly} deleted successfully!`, date: dateOnly };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const deleteRosterShift = createAsyncThunk(
  'other/deleteRosterShift',
  async (date, { rejectWithValue }) => {
    try {
      const dateOnly = date.split('T')[0];
      const response = await fetch(`${API_BASE_URL}/api/attendance/${dateOnly}/roster-shifts`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to delete roster shift');
      }

      return { message: `Roster shift for date ${dateOnly} deleted successfully!`, date: dateOnly };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const deleteDutyRoster = createAsyncThunk(
  'other/deleteDutyRoster',
  async ({ rosterName, weekStartingDate }, { rejectWithValue }) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/duty-roster/charana-tv/delete/${weekStartingDate}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to delete duty roster');
      }

      return { 
        message: `Duty Roster ${rosterName} for week starting ${weekStartingDate} deleted successfully!`,
        rosterName,
        weekStartingDate
      };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const downloadAttendance = createAsyncThunk(
  'other/downloadAttendance',
  async (userId, { rejectWithValue }) => {
    try {
      const loggedInUserId = sessionStorage.getItem('userId');
      if (!loggedInUserId) {
        throw new Error('User session not found. Please log in again.');
      }

      const url = `${API_BASE_URL}/lms/employee/${userId}/excel/${loggedInUserId}`;
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch attendance data');
      }

      const blob = await response.blob();
      const fileName = `employee_report_${userId}.xlsx`;
      
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(a);

      return { fileName, message: 'File downloaded successfully' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const downloadAttendanceByDate = createAsyncThunk(
  'other/downloadAttendanceByDate',
  async ({ userId, date }, { rejectWithValue }) => {
    try {
      const loggedInUserId = sessionStorage.getItem('userId');
      if (!loggedInUserId) {
        throw new Error('User session not found. Please log in again.');
      }

      const dateOnly = date.split('T')[0];
      const url = `${API_BASE_URL}/lms/employee/${userId}/excel/date/${dateOnly}/${loggedInUserId}`;
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch attendance data for the selected date');
      }

      const blob = await response.blob();
      const fileName = `employee_report_${userId}_${dateOnly}.xlsx`;
      
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(a);

      return { fileName, message: 'File downloaded successfully' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const downloadAttendanceByMonth = createAsyncThunk(
  'other/downloadAttendanceByMonth',
  async ({ userId, year, month }, { rejectWithValue }) => {
    try {
      const loggedInUserId = sessionStorage.getItem('userId');
      if (!loggedInUserId) {
        throw new Error('User session not found. Please log in again.');
      }

      const url = `${API_BASE_URL}/lms/employee/${userId}/excel/month/${year}/${month}/${loggedInUserId}`;
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch attendance data for the selected month');
      }

      const blob = await response.blob();
      const monthStr = month.toString().padStart(2, '0');
      const fileName = `employee_report_${userId}_${year}_${monthStr}.xlsx`;
      
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(a);

      return { fileName, message: 'File downloaded successfully' };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

const initialState = {
  dialogs: {
    uploadRoster: false,
    uploadRosterShift: false,
    uploadDutyRoster: false,
    deleteRoster: false,
    deleteRosterShift: false,
    deleteDutyRoster: false,
    getAttendance: false,
    getAttendanceByDate: false,
    getAttendanceByMonth: false,
    holiday: false
  },
  
  form: {
    userId: '',
    startDate: '',
    endDate: '',
    rosterDate: '',
    rosterShiftDate: '',
    selectedYear: new Date().getFullYear(),
    selectedMonth: new Date().getMonth() + 1,
    rosterName: 'CharanaTV_MCR',
    weekStartingDate: ''
  },
  
  files: {
    roster: null,
    rosterShift: null,
    dutyRoster: null
  },
  
  loading: {
    uploadRoster: false,
    uploadRosterShift: false,
    uploadDutyRoster: false,
    deleteRoster: false,
    deleteRosterShift: false,
    deleteDutyRoster: false,
    downloadAttendance: false,
    downloadAttendanceByDate: false,
    downloadAttendanceByMonth: false
  },
  
  error: null,
  
  successMessage: null
};

const otherSlice = createSlice({
  name: 'other',
  initialState,
  reducers: {
    openDialog: (state, action) => {
      const dialogName = action.payload;
      state.dialogs[dialogName] = true;
    },
    
    closeDialog: (state, action) => {
      const dialogName = action.payload;
      state.dialogs[dialogName] = false;
    },
    
    updateFormField: (state, action) => {
      const { field, value } = action.payload;
      state.form[field] = value;
    },
    
    resetForm: (state) => {
      state.form = {
        userId: '',
        startDate: '',
        endDate: '',
        rosterDate: '',
        rosterShiftDate: '',
        selectedYear: new Date().getFullYear(),
        selectedMonth: new Date().getMonth() + 1,
        rosterName: 'CharanaTV_MCR',
        weekStartingDate: ''
      };
    },
    
    setFile: (state, action) => {
      const { fileType, file } = action.payload;
      state.files[fileType] = file;
    },
    
    clearFile: (state, action) => {
      const fileType = action.payload;
      state.files[fileType] = null;
    },
    
    clearMessages: (state) => {
      state.error = null;
      state.successMessage = null;
    }
  },
  
  extraReducers: (builder) => {
    builder
      .addCase(uploadRoster.pending, (state) => {
        state.loading.uploadRoster = true;
        state.error = null;
      })
      .addCase(uploadRoster.fulfilled, (state, action) => {
        state.loading.uploadRoster = false;
        state.successMessage = action.payload.message;
        state.dialogs.uploadRoster = false;
        state.files.roster = null;
      })
      .addCase(uploadRoster.rejected, (state, action) => {
        state.loading.uploadRoster = false;
        state.error = action.payload;
      });
    builder
      .addCase(uploadRosterShift.pending, (state) => {
        state.loading.uploadRosterShift = true;
        state.error = null;
      })
      .addCase(uploadRosterShift.fulfilled, (state, action) => {
        state.loading.uploadRosterShift = false;
        state.successMessage = action.payload.message;
        state.dialogs.uploadRosterShift = false;
        state.files.rosterShift = null;
      })
      .addCase(uploadRosterShift.rejected, (state, action) => {
        state.loading.uploadRosterShift = false;
        state.error = action.payload;
      });
    builder
      .addCase(uploadDutyRoster.pending, (state) => {
        state.loading.uploadDutyRoster = true;
        state.error = null;
      })
      .addCase(uploadDutyRoster.fulfilled, (state, action) => {
        state.loading.uploadDutyRoster = false;
        state.successMessage = action.payload.message;
        state.dialogs.uploadDutyRoster = false;
        state.files.dutyRoster = null;
        state.form.weekStartingDate = '';
      })
      .addCase(uploadDutyRoster.rejected, (state, action) => {
        state.loading.uploadDutyRoster = false;
        state.error = action.payload;
      });
    builder
      .addCase(deleteRoster.pending, (state) => {
        state.loading.deleteRoster = true;
        state.error = null;
      })
      .addCase(deleteRoster.fulfilled, (state, action) => {
        state.loading.deleteRoster = false;
        state.successMessage = action.payload.message;
        state.dialogs.deleteRoster = false;
        state.form.rosterDate = '';
      })
      .addCase(deleteRoster.rejected, (state, action) => {
        state.loading.deleteRoster = false;
        state.error = action.payload;
      });
    builder
      .addCase(deleteRosterShift.pending, (state) => {
        state.loading.deleteRosterShift = true;
        state.error = null;
      })
      .addCase(deleteRosterShift.fulfilled, (state, action) => {
        state.loading.deleteRosterShift = false;
        state.successMessage = action.payload.message;
        state.dialogs.deleteRosterShift = false;
        state.form.rosterShiftDate = '';
      })
      .addCase(deleteRosterShift.rejected, (state, action) => {
        state.loading.deleteRosterShift = false;
        state.error = action.payload;
      });
    builder
      .addCase(deleteDutyRoster.pending, (state) => {
        state.loading.deleteDutyRoster = true;
        state.error = null;
      })
      .addCase(deleteDutyRoster.fulfilled, (state, action) => {
        state.loading.deleteDutyRoster = false;
        state.successMessage = action.payload.message;
        state.dialogs.deleteDutyRoster = false;
        state.form.weekStartingDate = '';
      })
      .addCase(deleteDutyRoster.rejected, (state, action) => {
        state.loading.deleteDutyRoster = false;
        state.error = action.payload;
      });
    builder
      .addCase(downloadAttendance.pending, (state) => {
        state.loading.downloadAttendance = true;
        state.error = null;
      })
      .addCase(downloadAttendance.fulfilled, (state, action) => {
        state.loading.downloadAttendance = false;
        state.successMessage = action.payload.message;
        state.dialogs.getAttendance = false;
        state.form.userId = '';
      })
      .addCase(downloadAttendance.rejected, (state, action) => {
        state.loading.downloadAttendance = false;
        state.error = action.payload;
      });
    builder
      .addCase(downloadAttendanceByDate.pending, (state) => {
        state.loading.downloadAttendanceByDate = true;
        state.error = null;
      })
      .addCase(downloadAttendanceByDate.fulfilled, (state, action) => {
        state.loading.downloadAttendanceByDate = false;
        state.successMessage = action.payload.message;
        state.dialogs.getAttendanceByDate = false;
        state.form.userId = '';
        state.form.startDate = '';
        state.form.endDate = '';
      })
      .addCase(downloadAttendanceByDate.rejected, (state, action) => {
        state.loading.downloadAttendanceByDate = false;
        state.error = action.payload;
      });
    builder
      .addCase(downloadAttendanceByMonth.pending, (state) => {
        state.loading.downloadAttendanceByMonth = true;
        state.error = null;
      })
      .addCase(downloadAttendanceByMonth.fulfilled, (state, action) => {
        state.loading.downloadAttendanceByMonth = false;
        state.successMessage = action.payload.message;
        state.dialogs.getAttendanceByMonth = false;
        state.form.userId = '';
        state.form.selectedYear = new Date().getFullYear();
        state.form.selectedMonth = new Date().getMonth() + 1;
      })
      .addCase(downloadAttendanceByMonth.rejected, (state, action) => {
        state.loading.downloadAttendanceByMonth = false;
        state.error = action.payload;
      });
  }
});

export const {
  openDialog,
  closeDialog,
  updateFormField,
  resetForm,
  setFile,
  clearFile,
  clearMessages
} = otherSlice.actions;

export default otherSlice.reducer;
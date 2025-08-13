import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Helper function to format date for API call
const formatDateForAPI = (dateString) => {
    if (!dateString) return null;
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return null;
        
        // Format as YYYY-MM-DD
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
    } catch (error) {
        console.error('Date formatting error:', error);
        return null;
    }
};

// Helper function to create a delay for rate limiting
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Helper function to process in batches
const processBatches = async (items, batchSize, processor, delayMs = 0) => {
    const results = [];
    
    for (let i = 0; i < items.length; i += batchSize) {
        const batch = items.slice(i, i + batchSize);
        const batchResults = await Promise.allSettled(
            batch.map(item => processor(item))
        );
        
        // Extract successful results and log failures
        batchResults.forEach((result, index) => {
            if (result.status === 'fulfilled') {
                results.push(result.value);
            } else {
                console.warn(`Failed to process item ${i + index}:`, result.reason);
                // Add the original item without enhancement
                results.push({ ...batch[index], inOutEnhanced: false });
            }
        });
        
        // Add delay between batches to prevent overwhelming the server
        if (delayMs > 0 && i + batchSize < items.length) {
            await delay(delayMs);
        }
    }
    
    return results;
};

// Original fetch function
export const fetchActivityRecords = createAsyncThunk(
    'activityRecords/fetchActivityRecords',
    async ({ page, rowsPerPage }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(
                `http://192.168.3.20:8080/lms/${empId}?page=${page}&size=${rowsPerPage}`,
                { credentials: 'include' }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            console.log('Original fetch data:', data);
            return data;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Enhanced fetch function with in-out data enrichment
export const fetchActivityRecordsV2 = createAsyncThunk(
    'activityRecords/fetchActivityRecordsV2',
    async ({ page, rowsPerPage, batchSize = 10, delayMs = 100 }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            // Step 1: Fetch original attendance data
            console.log('Fetching attendance data...');
            const response = await fetch(
                `http://192.168.3.20:8080/lms/${empId}?page=${page}&size=${rowsPerPage}`,
                { credentials: 'include' }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const attendanceData = await response.json();
            console.log('Attendance data fetched:', attendanceData);

            // Step 2: Check if we have records to process
            if (!attendanceData.content || attendanceData.content.length === 0) {
                return attendanceData;
            }

            // Step 3: Create processor function for individual records
            const processRecord = async (record) => {
                try {
                    // Skip if already has complete inOutDTOs data
                    if (record.inOutDTOs && record.inOutDTOs.length >= 2) {
                        return { ...record, inOutEnhanced: true, enhancementSource: 'original' };
                    }

                    // Skip if we don't have required data for API call
                    const userId = record.userId || record.employeeId;
                    const happenDate = formatDateForAPI(record.arrivalDate || record.date);
                    
                    if (!userId || !happenDate) {
                        console.warn(`Skipping record ${record.id}: missing userId or date`);
                        return { ...record, inOutEnhanced: false, enhancementSource: 'skipped' };
                    }

                    // Fetch in-out data from separate endpoint
                    const inOutResponse = await fetch(
                        `http://192.168.3.20:8080/lms/in-out/${happenDate}/earliest/${userId}/${empId}`,
                        { credentials: 'include' }
                    );

                    if (!inOutResponse.ok) {
                        console.warn(`Failed to fetch in-out data for ${userId} on ${happenDate}`);
                        return { ...record, inOutEnhanced: false, enhancementSource: 'api_error' };
                    }

                    const inOutData = await inOutResponse.json();
                    console.log(`In-out data for ${userId}:`, inOutData);

                    // Step 4: Transform the fetched in-out data to match inOutDTOs structure
                    const enhancedInOutDTOs = [];
                    
                    if (inOutData.morning) {
                        enhancedInOutDTOs.push({
                            ...inOutData.morning,
                            // Ensure consistency with existing structure
                            employeeID: inOutData.morning.employeeID || userId,
                        });
                    }
                    
                    if (inOutData.evening) {
                        enhancedInOutDTOs.push({
                            ...inOutData.evening,
                            // Ensure consistency with existing structure
                            employeeID: inOutData.evening.employeeID || userId,
                        });
                    }

                    // Step 5: Merge with existing inOutDTOs (if any) and remove duplicates
                    const existingInOuts = record.inOutDTOs || [];
                    const allInOuts = [...existingInOuts, ...enhancedInOutDTOs];
                    
                    // Remove duplicates based on id
                    const uniqueInOuts = allInOuts.filter((item, index, self) => 
                        index === self.findIndex(t => t.id === item.id)
                    );

                    // Step 6: Update arrival and left times if they were missing
                    let updatedRecord = { ...record };
                    
                    if (!record.arrivalTime && inOutData.morning?.punchTypeTime) {
                        updatedRecord.arrivalTime = inOutData.morning.punchTypeTime;
                    }
                    
                    if (!record.leftTime && inOutData.evening?.punchTypeTime) {
                        updatedRecord.leftTime = inOutData.evening.punchTypeTime;
                    }

                    return {
                        ...updatedRecord,
                        inOutDTOs: uniqueInOuts,
                        inOutEnhanced: true,
                        enhancementSource: 'api_fetch',
                        enhancementTimestamp: new Date().toISOString()
                    };

                } catch (error) {
                    console.error(`Error processing record ${record.id}:`, error);
                    return { ...record, inOutEnhanced: false, enhancementSource: 'error' };
                }
            };

            // Step 7: Process records in batches to handle large datasets efficiently
            console.log(`Processing ${attendanceData.content.length} records in batches of ${batchSize}...`);
            
            const enhancedRecords = await processBatches(
                attendanceData.content,
                batchSize,
                processRecord,
                delayMs
            );

            console.log('Enhancement completed. Enhanced records:', enhancedRecords.length);

            // Step 8: Return enhanced data with same structure as original
            const enhancedData = {
                ...attendanceData,
                content: enhancedRecords,
                enhancementMetadata: {
                    totalRecords: enhancedRecords.length,
                    enhancedCount: enhancedRecords.filter(r => r.inOutEnhanced === true).length,
                    skippedCount: enhancedRecords.filter(r => r.inOutEnhanced === false).length,
                    enhancementTimestamp: new Date().toISOString(),
                    batchSize,
                    delayMs
                }
            };

            return enhancedData;

        } catch (error) {
            console.error('Error in fetchActivityRecordsV2:', error);
            return rejectWithValue(error.message);
        }
    }
);

// Standalone function to fetch in-out data (keeping for compatibility)
export const fetchInOutData = createAsyncThunk(
    'movement/fetchInOutData',
    async ({userId, happenDate}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/in-out/${happenDate}/earliest/${userId}/${empId}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            console.log('Standalone in-out data:', data);
            return data;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const initialState = {
    records: [],
    totalElements: 0,
    totalPages: 0,
    loading: false,
    error: null,
    searchTerm: '',
    filterType: 'all',
    filterStatus: 'all',
    filterIssue: 'all',
    page: 0,
    rowsPerPage: 10,
    enhancementMetadata: null,
    useV2: false, // Flag to switch between versions
};

const activityRecordsSlice = createSlice({
    name: 'activityRecords',
    initialState,
    reducers: {
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
        },
        setFilterType: (state, action) => {
            state.filterType = action.payload;
        },
        setFilterStatus: (state, action) => {
            state.filterStatus = action.payload;
        },
        setFilterIssue: (state, action) => {
            state.filterIssue = action.payload;
        },
        setPage: (state, action) => {
            state.page = action.payload;
        },
        setRowsPerPage: (state, action) => {
            state.rowsPerPage = action.payload;
            state.page = 0;
        },
        clearFilters: (state) => {
            state.searchTerm = '';
            state.filterType = 'all';
            state.filterStatus = 'all';
            state.filterIssue = 'all';
        },
        toggleUseV2: (state) => {
            state.useV2 = !state.useV2;
        },
        setUseV2: (state, action) => {
            state.useV2 = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder
            // Original fetch handlers
            .addCase(fetchActivityRecords.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.enhancementMetadata = null;
            })
            .addCase(fetchActivityRecords.fulfilled, (state, action) => {
                state.loading = false;
                state.records = action.payload.content;
                state.totalElements = action.payload.totalElements;
                state.totalPages = action.payload.totalPages;
                state.enhancementMetadata = null;
            })
            .addCase(fetchActivityRecords.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.enhancementMetadata = null;
            })
            // V2 fetch handlers
            .addCase(fetchActivityRecordsV2.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.enhancementMetadata = null;
            })
            .addCase(fetchActivityRecordsV2.fulfilled, (state, action) => {
                state.loading = false;
                state.records = action.payload.content;
                state.totalElements = action.payload.totalElements;
                state.totalPages = action.payload.totalPages;
                state.enhancementMetadata = action.payload.enhancementMetadata;
            })
            .addCase(fetchActivityRecordsV2.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.enhancementMetadata = null;
            })
            // In-out data handlers (for standalone usage)
            .addCase(fetchInOutData.pending, (state) => {
                // Don't change loading state for standalone in-out fetch
            })
            .addCase(fetchInOutData.fulfilled, (state, action) => {
                // Handle standalone in-out data if needed
            })
            .addCase(fetchInOutData.rejected, (state, action) => {
                // Handle standalone in-out error if needed
            });
    },
});

export const {
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setFilterIssue,
    setPage,
    setRowsPerPage,
    clearFilters,
    toggleUseV2,
    setUseV2,
} = activityRecordsSlice.actions;

// Enhanced selector to work with both versions
export const selectFilteredActivities = (state) => {
    const {
        activityRecords: {
            records = [],
            searchTerm = '',
            filterType = 'all',
            filterStatus = 'all',
            filterIssue = 'all'
        }
    } = state;

    if (!records || records.length === 0) return [];

    const searchTermLower = searchTerm.toLowerCase();

    let filteredRecords = records.filter((activity) => {
        if (searchTerm) {
            const idToSearch = (activity.userId || activity.employeeId || '').toLowerCase();
            if (!idToSearch.includes(searchTermLower)) {
                return false;
            }
        }

        if (filterType !== 'all') {
            switch (filterType) {
                case 'fullDay':
                    if (activity.attendanceType !== 'FULL_DAY' || activity.isLate) return false;
                    break;
                case 'halfDay':
                    if (activity.attendanceType !== 'HALF_DAY') return false;
                    break;
                case 'absent':
                    if (activity.attendanceType !== 'ABSENT') return false;
                    break;
                case 'fullLeave':
                    if (activity.leaveStatus !== 'FULL_LEAVE') return false;
                    break;
                case 'shortLeave':
                    if (activity.leaveStatus !== 'SHORT_LEAVE') return false;
                    break;
                case 'late':
                    if (!activity.isLate) return false;
                    break;
                default:
                    break;
            }
        }

        if (filterStatus !== 'all') {
            switch (filterStatus) {
                case 'Approved':
                    if (activity.leaveStatus !== 'LEAVE_APPROVED') return false;
                    break;
                case 'Pending':
                    if (!(
                        activity.leaveStatus === 'LEAVE_REQUESTED' ||
                        (!activity.isUnSuccessful && !activity.isUnauthorized && !activity.hasIssues)
                    )) return false;
                    break;
                case 'Not Approved':
                    if (!(activity.isUnSuccessful || activity.isUnauthorized)) return false;
                    break;
                default:
                    break;
            }
        }

        if (filterIssue !== 'all') {
            const hasIssue = activity.hasIssues;
            if (filterIssue === 'hasIssue' && !hasIssue) return false;
            if (filterIssue === 'noIssue' && hasIssue) return false;
        }

        return true;
    });

    return filteredRecords.sort((a, b) => {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        return dateB - dateA; 
    });
};

export const selectActivitiesData = (state) => state.activityRecords;
export const selectIsFiltering = (state) => {
    const { searchTerm, filterType, filterStatus, filterIssue } = state.activityRecords;
    return searchTerm !== "" || filterType !== "all" || filterStatus !== "all" || filterIssue !== "all";
};

// New selector for enhancement metadata
export const selectEnhancementMetadata = (state) => state.activityRecords.enhancementMetadata;

// New selector to check if using V2
export const selectUseV2 = (state) => state.activityRecords.useV2;

export default activityRecordsSlice.reducer;
// store/managementSlice.js
import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';

const baseUrl = 'http://localhost:8080/users';

export const saveEmployee = createAsyncThunk(
    'management/saveEmployee',
    async ({employee, isUpdate}, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }
            console.log(employee);
            const url = isUpdate
                ? `${baseUrl}/update/${employee.userId}/${userId}`
                : `${baseUrl}/add/employees/${userId}`;

            const method = isUpdate ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(employee),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save employee');
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);


export const fetchManagementData = createAsyncThunk(
    'management/fetchData',
    async (_, {rejectWithValue}) => {
        try {
            const [roles, users, authorities, profiles, roleNames, sectionNames, profileNames, sections] =
                await Promise.all([
                    fetch(`${baseUrl}/roles`).then(res => res.json()),
                    fetch(`${baseUrl}`).then(res => res.json()),
                    fetch(`${baseUrl}/authorities`).then(res => res.json()),
                    fetch(`${baseUrl}/profile`).then(res => res.json()),
                    fetch(`${baseUrl}/names/roles`).then(res => res.json()),
                    fetch(`${baseUrl}/names/sections`).then(res => res.json()),
                    fetch(`${baseUrl}/names/profiles`).then(res => res.json()),
                    fetch(`${baseUrl}/sections`).then(res => res.json()),
                ]);

            // Fetch dynamic roles
            const dynamicRoles = roleNames || [];
            const roleFetchPromises = dynamicRoles.map(role =>
                fetch(`${baseUrl}/get-role/${role}`).then(res => res.json())
            );
            const roleData = await Promise.all(roleFetchPromises);

            return {
                roles,
                users,
                authorities: authorities.map(a => ({id: a.ID, name: a.name})),
                profiles,
                sections,
                roleNames,
                sectionNames,
                profileNames,
                roleData: dynamicRoles.reduce((acc, role, index) => {
                    acc[role] = roleData[index];
                    return acc;
                }, {})
            };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Paginated users
export const fetchPaginatedUsers = createAsyncThunk(
    'management/fetchPaginatedUsers',
    async ({page, limit}, {rejectWithValue}) => {
        try {
            const response = await fetch(
                `${baseUrl}/all?page=${page}&limit=${limit}`
                , {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });
            if (!response.ok) {
                throw new Error('Failed to fetch paginated users');
            }
            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Role CRUD operations
export const saveRole = createAsyncThunk(
    'management/saveRole',
    async ({roleData, isUpdate, roleId}, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }
            const url = isUpdate ? `${baseUrl}/roles/${roleId}/${userId}` :`${baseUrl}/roles/${userId}`;

            const method = isUpdate ? 'PUT' : 'POST';
            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(roleData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save role');
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const deleteRole = createAsyncThunk(
    'management/deleteRole',
    async (roleId, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const response = await fetch(`${baseUrl}/delete/role/${roleId}/${userId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete role');
            }

            return roleId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Section CRUD operations
export const saveSection = createAsyncThunk(
    'management/saveSection',
    async ({sectionData, isUpdate, publicId}, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const url = isUpdate
                ? `${baseUrl}/section/${publicId}/${userId}`
                : `${baseUrl}/section/${userId}`;

            const method = isUpdate ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    section: sectionData.section,
                    publicId: sectionData.publicId,
                    addedUsers: sectionData.users,
                    deletedUsers: sectionData.deletedUsers
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save section');
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const deleteSection = createAsyncThunk(
    'management/deleteSection',
    async (sectionId, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const response = await fetch(`${baseUrl}/section/${sectionId}/${userId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete section');
            }

            return sectionId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Profile CRUD operations
export const saveProfile = createAsyncThunk(
    'management/saveProfile',
    async ({profileData, isUpdate}, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const url = isUpdate
                ? `${baseUrl}/profile/${profileData.id}/${userId}`
                : `${baseUrl}/profile/${userId}`;

            const method = isUpdate ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    ...profileData,
                    addedUsers: profileData.users,
                    deletedUsers: profileData.deletedUsers
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save profile');
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const deleteProfile = createAsyncThunk(
    'management/deleteProfile',
    async (profileId, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const response = await fetch(`${baseUrl}/profile/${profileId}/${userId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete profile');
            }

            return profileId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const deleteEmployee = createAsyncThunk(
    'management/deleteEmployee',
    async (employeeId, {rejectWithValue}) => {
        try {
            const userId = localStorage.getItem('userId');
            if (!userId) {
                throw new Error('User ID not found');
            }

            const response = await fetch(`${baseUrl}/${employeeId}/${userId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete employee');
            }

            return employeeId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const managementSlice = createSlice({
    name: 'management',
    initialState: {
        data: null,
        paginatedUsers: null,
        loading: false,
        error: null,
        currentPage: 0,
        pageSize: 10,
        saveLoading: false,
        saveError: null,
        saveSuccess: false
    },
    reducers: {
        setCurrentPage: (state, action) => {
            state.currentPage = action.payload;
        },
        setPageSize: (state, action) => {
            state.pageSize = action.payload;
        },
        resetSaveStatus: (state) => {
            state.saveLoading = false;
            state.saveError = null;
            state.saveSuccess = false;
        }
    },
    extraReducers: (builder) => {
        builder
            // Fetch management data
            .addCase(fetchManagementData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(saveEmployee.pending, (state) => {
                state.saveLoading = true;
                state.saveError = null;
                state.saveSuccess = false;
            })
            .addCase(saveEmployee.fulfilled, (state, action) => {
                state.saveLoading = false;
                state.saveSuccess = true;
                // Update the users list in the state
                if (state.data) {
                    if (action.meta.arg.isUpdate) {
                        state.data.users = state.data.users.map(user =>
                            user.userId === action.payload.userId ? action.payload : user
                        );
                    } else {
                        state.data.users = [...state.data.users, action.payload];
                    }
                }
                // Also update paginated users if needed
                if (state.paginatedUsers?.content) {
                    if (action.meta.arg.isUpdate) {
                        state.paginatedUsers.content = state.paginatedUsers.content.map(user =>
                            user.userId === action.payload.userId ? action.payload : user
                        );
                    } else {
                        state.paginatedUsers.content = [...state.paginatedUsers.content, action.payload];
                        state.paginatedUsers.totalElements += 1;
                    }
                }
            })
            .addCase(saveEmployee.rejected, (state, action) => {
                state.saveLoading = false;
                state.saveError = action.payload;
            })
            .addCase(fetchManagementData.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload;
            })
            .addCase(fetchManagementData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })

            // Fetch paginated users
            .addCase(fetchPaginatedUsers.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchPaginatedUsers.fulfilled, (state, action) => {
                state.loading = false;
                state.paginatedUsers = action.payload;
            })
            .addCase(fetchPaginatedUsers.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })

            // Save role
            .addCase(saveRole.pending, (state) => {
                state.saveLoading = true;
                state.saveError = null;
                state.saveSuccess = false;
            })
            .addCase(saveRole.fulfilled, (state, action) => {
                state.saveLoading = false;
                state.saveSuccess = true;
                if (state.data) {
                    if (action.meta.arg.isUpdate) {
                        state.data.roles = state.data.roles.map(role =>
                            role.id === action.payload.id ? action.payload : role
                        );
                    } else {
                        state.data.roles = [...state.data.roles, action.payload];
                    }
                }
            })
            .addCase(saveRole.rejected, (state, action) => {
                state.saveLoading = false;
                state.saveError = action.payload;
            })

            // Delete role
            .addCase(deleteRole.fulfilled, (state, action) => {
                if (state.data) {
                    state.data.roles = state.data.roles.filter(role => role.id !== action.payload);
                }
            })

            // Save section
            .addCase(saveSection.pending, (state) => {
                state.saveLoading = true;
                state.saveError = null;
                state.saveSuccess = false;
            })
            .addCase(saveSection.fulfilled, (state, action) => {
                state.saveLoading = false;
                state.saveSuccess = true;
                if (state.data) {
                    if (action.meta.arg.isUpdate) {
                        state.data.sections = state.data.sections.map(section =>
                            section.id === action.payload.id ? action.payload : section
                        );
                    } else {
                        state.data.sections = [...state.data.sections, action.payload];
                    }
                }
            })
            .addCase(saveSection.rejected, (state, action) => {
                state.saveLoading = false;
                state.saveError = action.payload;
            })

            // Delete section
            .addCase(deleteSection.fulfilled, (state, action) => {
                if (state.data) {
                    state.data.sections = state.data.sections.filter(section => section.id !== action.payload);
                }
            })

            // Save profile
            .addCase(saveProfile.pending, (state) => {
                state.saveLoading = true;
                state.saveError = null;
                state.saveSuccess = false;
            })
            .addCase(saveProfile.fulfilled, (state, action) => {
                state.saveLoading = false;
                state.saveSuccess = true;
                if (state.data) {
                    if (action.meta.arg.isUpdate) {
                        state.data.profiles = state.data.profiles.map(profile =>
                            profile.id === action.payload.id ? action.payload : profile
                        );
                    } else {
                        state.data.profiles = [...state.data.profiles, action.payload];
                    }
                }
            })
            .addCase(saveProfile.rejected, (state, action) => {
                state.saveLoading = false;
                state.saveError = action.payload;
            })
            .addCase(deleteEmployee.fulfilled, (state, action) => {
                if (state.data) {
                    state.data.users = state.data.users.filter(user => user.userId !== action.payload);
                }
                if (state.paginatedUsers?.content) {
                    state.paginatedUsers.content = state.paginatedUsers.content.filter(
                        user => user.userId !== action.payload
                    );
                    state.paginatedUsers.totalElements -= 1;
                }
            })
            // Delete profile
            .addCase(deleteProfile.fulfilled, (state, action) => {
                if (state.data) {
                    state.data.profiles = state.data.profiles.filter(profile => profile.id !== action.payload);
                }
            });

    }
});

export const {setCurrentPage, setPageSize, resetSaveStatus} = managementSlice.actions;
export default managementSlice.reducer;
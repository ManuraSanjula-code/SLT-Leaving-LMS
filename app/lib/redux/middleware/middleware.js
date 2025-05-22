import {setError, clearAuth} from '../redux-user/authSlice';

export const authMiddleware = store => next => action => {
    const restrictedActions = [
        'roster/fetchRosterData',
        'rosterManagement/fetchRosterData',
        'rosterManagement/fetchTeamDetails',
        'rosterManagement/fetchEmployeeDetails',
        'rosterManagement/updateEmployeeRoster',
        'management/saveEmployee',
        'management/fetchData',
        'management/fetchPaginatedUsers',
        'management/fetchPaginatedAdmins',
        'management/saveRole',
        'management/deleteRole',
        'management/saveSection',
        'management/deleteSection',
        'management/saveProfile',
        'management/deleteProfile',
        'management/deleteEmployee',
        'unsuccessfulLeaves/fetch',
        'unsuccessfulLeaves/resolve',
        'unsuccessfulLeaves/bulkResolve',
        'unauthorized-leaves/fetch',
        'unauthorized-leaves/resolve',
        'unauthorized-leaves/approve',
        'unauthorized-leaves/bulkResolve',
        'unauthorized-leaves/deleteMultiple',
        'noPay/fetchNoPayRecords',
        'movement/fetchMovementRequests',
        'movement/deleteMovementRequest',
        'movement/updateMovementRequest',
        'movement/fetchInOutData',
        'movementRequest/submit',
        'movement/fetchMovementRequests',
        'movement/processMovementRequest',
        'movement/processBulkMovementRequests',
        'leaveApplication/fetchBalances',
        'leaveApplication/submitRequest',
        'leave/fetchLeaveRequest',
        'leave/processLeaveRequest',
        'leave/processBulkLeaveRequests',
        'attendance/fetchAttendanceData',
        'employeeActivities/fetch',
        'employeeActivities/create',
        'employeeActivities/update',
        'employeeActivities/delete',
        'activityRecords/fetchActivityRecords',
    ];

    const adminActions = ['admin/updateSettings'];
    const paymentActions = ['payment/process'];

    if (restrictedActions.includes(action.type)) {
        const state = store.getState();
        const {jwt, userDetails} = state.auth;

        if (!jwt) {
            console.error('Authentication required for action:', action.type);

            return store.dispatch(setError({
                message: 'Authentication required. Please log in.',
                code: 'AUTH_REQUIRED',
                originalAction: action.type
            }));
        }

        /*try {
            const tokenData = jwt.split('.')[1];
            if (tokenData) {
                const decodedToken = JSON.parse(atob(tokenData));
                if (decodedToken.exp && decodedToken.exp * 1000 < Date.now()) {
                    console.error('Token expired');
                    store.dispatch(clearAuth());
                    return store.dispatch(setError({
                        message: 'Your session has expired. Please log in again.',
                        code: 'TOKEN_EXPIRED',
                        originalAction: action.type
                    }));
                }
            }
        } catch (error) {
            console.error('Error checking token expiration:', error);
        }*/

        // Check for admin permissions
        /* if (adminActions.includes(action.type)) {
             const hasAdminRole = userDetails?.roles?.some(role =>
                 role === 'ROLE_ADMIN' || role === 'ADMIN'
             );

             if (!hasAdminRole) {
                 console.error('Admin permission required for action:', action.type);
                 return store.dispatch(setError({
                     message: 'You do not have permission to perform this action.',
                     code: 'PERMISSION_DENIED',
                     originalAction: action.type
                 }));
             }
         }

         // Check for payment permissions
         if (paymentActions.includes(action.type)) {
             const hasPaymentPermission = userDetails?.roles?.some(role =>
                 role === 'ROLE_PAYMENT' || role === 'PAYMENT_ADMIN'
             );

             if (!hasPaymentPermission) {
                 console.error('Payment permission required for action:', action.type);
                 return store.dispatch(setError({
                     message: 'You do not have permission to process payments.',
                     code: 'PAYMENT_PERMISSION_DENIED',
                     originalAction: action.type
                 }));
             }
         }*/
    }

    return next(action);
};

export const apiErrorMiddleware = store => next => action => {
    if (action.type?.endsWith('/rejected') && action.error) {
        const {status} = action.payload || {};

        if (status === 401) {
            console.error('API Authentication failed');
            store.dispatch(clearAuth());
            store.dispatch(setError({
                message: 'Your session has expired. Please log in again.',
                code: 'API_AUTH_FAILED'
            }));
        }
    }

    return next(action);
};
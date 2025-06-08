import { createSelector } from '@reduxjs/toolkit';

// Base selectors
const selectAbsentEmployeesState = (state) => state.absentEmployees;

// Memoized selectors
export const selectEmployees = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.employees
);

export const selectLoading = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.loading
);

export const selectError = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.error
);

export const selectCurrentPage = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.currentPage
);

export const selectTotalPages = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.totalPages
);

export const selectTotalElements = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.totalElements
);

export const selectPageSize = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.pageSize
);

export const selectFilters = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.filters
);

export const selectIsAdmin = createSelector(
    [selectAbsentEmployeesState],
    (absentEmployees) => absentEmployees.isAdmin
);

// Computed selectors
export const selectUnresolvedEmployees = createSelector(
    [selectEmployees],
    (employees) => employees.filter(employee => !employee.isResolved)
);

export const selectResolvedEmployees = createSelector(
    [selectEmployees],
    (employees) => employees.filter(employee => employee.isResolved)
);

export const selectPaginationInfo = createSelector(
    [selectCurrentPage, selectPageSize, selectTotalElements],
    (currentPage, pageSize, totalElements) => ({
        startIndex: (currentPage * pageSize) + 1,
        endIndex: Math.min((currentPage + 1) * pageSize, totalElements),
        totalElements
    })
);

export const selectHasEmployees = createSelector(
    [selectEmployees],
    (employees) => employees.length > 0
);

export const selectIsFirstLoad = createSelector(
    [selectLoading, selectEmployees],
    (loading, employees) => loading && employees.length === 0
);

export const selectIsSubsequentLoad = createSelector(
    [selectLoading, selectEmployees],
    (loading, employees) => loading && employees.length > 0
);
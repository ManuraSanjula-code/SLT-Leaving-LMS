"use client";

import React, { useState } from "react";
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
    Button, IconButton,
} from "@mui/material";
import { Delete as DeleteIcon } from "@mui/icons-material";


const EmployeeList = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [profileFilter, setProfileFilter] = useState("All");
    const [sectionFilter, setSectionFilter] = useState("All");
    const [roleFilter, setRoleFilter] = useState("All");
    const [selected, setSelected] = useState([]);

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value);
    };

    const handleProfileFilterChange = (event) => {
        setProfileFilter(event.target.value);
    };

    const handleSectionFilterChange = (event) => {
        setSectionFilter(event.target.value);
    };

    const handleRoleFilterChange = (event) => {
        setRoleFilter(event.target.value);
    };

    const filteredEmployees = allEmployees.filter((employee) => {
        const matchesSearchQuery =
            employee.name.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesProfileFilter =
            profileFilter === "All" || employee.profile === profileFilter;

        const matchesSectionFilter =
            sectionFilter === "All" || employee.section === sectionFilter;

        const matchesRoleFilter =
            roleFilter === "All" || employee.role === roleFilter;

        return (
            matchesSearchQuery &&
            matchesProfileFilter &&
            matchesSectionFilter &&
            matchesRoleFilter
        );
    });

    const handleSelect = (id) => {
        if (selected.includes(id)) {
            setSelected((prev) => prev.filter((item) => item !== id));
        } else {
            setSelected((prev) => [...prev, id]);
        }
    };

    const handleSelectAll = () => {
        if (selected.length === filteredEmployees.length) {
            setSelected([]);
        } else {
            setSelected(filteredEmployees.map((employee) => employee.id));
        }
    };

    const handleDeleteEmployee = (id) => {
    
    };

    const handleDeleteAllSelected = () => {
        setSelected([]);
    };

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    Employee List
                </Typography>

                <TextField
                    label="Search by Employee Name"
                    variant="outlined"
                    fullWidth
                    value={searchQuery}
                    onChange={handleSearchChange}
                    sx={{ mb: 2 }}
                />

                <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                    <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                        <InputLabel>Filter by Profile</InputLabel>
                        <Select
                            value={profileFilter}
                            onChange={handleProfileFilterChange}
                            label="Filter by Profile"
                        >
                            <MenuItem value="All">All</MenuItem>
                            <MenuItem value="VC-VAS">VC-VAS</MenuItem>
                            <MenuItem value="HEADQUARTERS-EMPLOYEE">HEADQUARTERS-EMPLOYEE</MenuItem>
                            <MenuItem value="OUT-STATION-STAFF">OUT-STATION-STAFF</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                        <InputLabel>Filter by Section</InputLabel>
                        <Select
                            value={sectionFilter}
                            onChange={handleSectionFilterChange}
                            label="Filter by Section"
                        >
                            <MenuItem value="All">All</MenuItem>
                            <MenuItem value="HE/MCR">HE/MCR</MenuItem>
                            <MenuItem value="IS/VAS">IS/VAS</MenuItem>
                            <MenuItem value="FINANCE">FINANCE</MenuItem>
                            <MenuItem value="SALES">SALES</MenuItem>
                            <MenuItem value="ADMIN">ADMIN</MenuItem>
                            <MenuItem value="LEAGAL">LEAGAL</MenuItem>
                            <MenuItem value="MARKETING">MARKETING</MenuItem>
                            <MenuItem value="MEDIA">MEDIA</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                        <InputLabel>Filter by Role</InputLabel>
                        <Select
                            value={roleFilter}
                            onChange={handleRoleFilterChange}
                            label="Filter by Role"
                        >
                            <MenuItem value="All">All</MenuItem>
                            <MenuItem value="ROLE_ADMIN">ROLE_ADMIN</MenuItem>
                            <MenuItem value="ROLE_CEO">ROLE_CEO</MenuItem>
                            <MenuItem value="ROLE_EMPLOYEE">ROLE_EMPLOYEE</MenuItem>
                            <MenuItem value="ROLE_HOD">ROLE_HOD</MenuItem>
                            <MenuItem value="ROLE_SUPERVISOR">ROLE_SUPERVISOR</MenuItem>
                        </Select>
                    </FormControl>
                </Box>

                <Button
                    variant="contained"
                    color="error"
                    onClick={handleDeleteAllSelected}
                    disabled={selected.length === 0}
                    sx={{ mb: 2 }}
                >
                    Delete All Selected
                </Button>

                {/* Table */}
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell padding="checkbox">
                                    <Checkbox
                                        indeterminate={
                                            selected.length > 0 && selected.length < filteredEmployees.length
                                        }
                                        checked={selected.length === filteredEmployees.length}
                                        onChange={handleSelectAll}
                                    />
                                </TableCell>
                                <TableCell>Name</TableCell>
                                <TableCell>Profile</TableCell>
                                <TableCell>Section</TableCell>
                                <TableCell>Role</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredEmployees.map((employee) => (
                                <TableRow key={employee.id}>
                                    <TableCell padding="checkbox">
                                        <Checkbox
                                            checked={selected.includes(employee.id)}
                                            onChange={() => handleSelect(employee.id)}
                                        />
                                    </TableCell>
                                    <TableCell>{employee.name}</TableCell>
                                    <TableCell>{employee.profile}</TableCell>
                                    <TableCell>{employee.section}</TableCell>
                                    <TableCell>{employee.role}</TableCell>
                                    <TableCell>
                                        <IconButton
                                            onClick={() => handleDeleteEmployee(employee.id)}
                                        >
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Box>
        </Container>
    );
};

export default EmployeeList;
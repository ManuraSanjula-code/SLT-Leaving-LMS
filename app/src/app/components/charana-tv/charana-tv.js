import React, { useState, useEffect, useCallback } from 'react';

const CharanaTVRosterTable = () => {
    const [rosterData, setRosterData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [selectedWeekStart, setSelectedWeekStart] = useState(() => {
        const today = new Date();
        const monday = new Date(today);
        monday.setDate(today.getDate() - today.getDay() + 1);
        return monday.toISOString().split('T')[0];
    });

    const fetchRosterData = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`http://192.168.3.20:8080/api/duty-roster/charana-tv/week/${selectedWeekStart}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (!data || Object.keys(data).length === 0) {
                throw new Error('No data found for the selected week');
            }

            if (!data.dailyDuties || !Array.isArray(data.dailyDuties)) {
                throw new Error('Invalid data structure received from server');
            }

            setRosterData(data);
        } catch (err) {
            setError(err.message);
            setRosterData(null);
        } finally {
            setLoading(false);
        }
    }, [selectedWeekStart]);

    useEffect(() => {
        fetchRosterData();
    }, [fetchRosterData, selectedWeekStart]);

    const handleFetchData = () => {
        fetchRosterData();
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    };

    const formatTime = (timeString) => {
        return timeString.substring(0, 5); // Remove seconds
    };

    const getShiftTypeColor = (shiftType) => {
        const colors = {
            'MORNING': '#e3f2fd',
            'EVENING': '#fff3e0',
            'NIGHT': '#f3e5f5',
            'AFTERNOON': '#e8f5e8'
        };
        return colors[shiftType] || '#f5f5f5';
    };

    const getShiftTypeChipColor = (shiftType) => {
        const colors = {
            'MORNING': '#1976d2',
            'EVENING': '#ff9800',
            'NIGHT': '#9c27b0',
            'AFTERNOON': '#4caf50'
        };
        return colors[shiftType] || '#757575';
    };

    const styles = {
        container: {
            padding: '24px',
            fontFamily: 'Arial, sans-serif',
            backgroundColor: '#f5f5f5',
            minHeight: '100vh'
        },
        card: {
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            marginBottom: '24px',
            padding: '24px'
        },
        header: {
            textAlign: 'center',
            color: '#333',
            marginBottom: '24px',
            fontSize: '2rem',
            fontWeight: 'bold'
        },
        controlsContainer: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '16px',
            marginBottom: '16px',
            flexWrap: 'wrap'
        },
        input: {
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontSize: '14px',
            minWidth: '200px'
        },
        button: {
            padding: '10px 20px',
            backgroundColor: '#1976d2',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 'bold',
            disabled: loading
        },
        buttonDisabled: {
            backgroundColor: '#ccc',
            cursor: 'not-allowed'
        },
        tableContainer: {
            overflowX: 'auto',
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
        },
        table: {
            width: '100%',
            borderCollapse: 'collapse',
            minWidth: '800px'
        },
        tableHeader: {
            backgroundColor: '#1976d2',
            color: 'white'
        },
        tableHeaderCell: {
            padding: '12px',
            textAlign: 'center',
            fontWeight: 'bold',
            border: '1px solid #ddd'
        },
        tableCell: {
            padding: '12px',
            textAlign: 'center',
            border: '1px solid #ddd'
        },
        tableRow: {
            '&:nth-child(even)': {
                backgroundColor: '#f9f9f9'
            }
        },
        chip: {
            padding: '4px 8px',
            borderRadius: '16px',
            fontSize: '12px',
            fontWeight: 'bold',
            border: '1px solid #ddd',
            display: 'inline-block',
            margin: '2px'
        },
        loading: {
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            padding: '40px',
            fontSize: '16px'
        },
        error: {
            backgroundColor: '#ffebee',
            border: '1px solid #f44336',
            borderRadius: '4px',
            padding: '16px',
            color: '#d32f2f',
            marginBottom: '24px'
        },
        info: {
            backgroundColor: '#e3f2fd',
            border: '1px solid #2196f3',
            borderRadius: '4px',
            padding: '16px',
            color: '#1976d2',
            textAlign: 'center'
        },
        dayCell: {
            fontWeight: 'bold',
            borderRight: '2px solid #ddd',
            verticalAlign: 'middle',
            backgroundColor: '#f5f5f5'
        },
        summaryGrid: {
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '16px',
            marginTop: '16px'
        },
        summaryBox: {
            textAlign: 'center',
            padding: '16px',
            backgroundColor: '#f5f5f5',
            borderRadius: '8px',
            border: '1px solid #ddd'
        },
        summaryNumber: {
            fontSize: '2rem',
            fontWeight: 'bold',
            color: '#1976d2'
        },
        legendContainer: {
            display: 'flex',
            flexWrap: 'wrap',
            gap: '8px',
            marginTop: '16px'
        }
    };

    const renderTableContent = () => {
        if (!rosterData || !rosterData.dailyDuties) {
            return null;
        }

        return (
            <div style={styles.tableContainer}>
                <table style={styles.table}>
                    <thead style={styles.tableHeader}>
                    <tr>
                        <th style={styles.tableHeaderCell}>Day &amp; Date</th>
                        <th style={styles.tableHeaderCell}>Shift Time</th>
                        <th style={styles.tableHeaderCell}>Shift Type</th>
                        <th style={styles.tableHeaderCell}>Primary Employee</th>
                        <th style={styles.tableHeaderCell}>Secondary Employee</th>
                        <th style={styles.tableHeaderCell}>All Assigned Employees</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rosterData.dailyDuties.map((day, dayIndex) => {
                        const dayRowSpan = day.timeSlots.length;

                        return day.timeSlots.map((timeSlot, slotIndex) => (
                            <tr
                                key={`${day.date}-${slotIndex}`}
                                style={{
                                    backgroundColor: slotIndex % 2 === 0 ? '#f9f9f9' : 'white',
                                    ...getShiftTypeColor(timeSlot.shiftType) && {
                                        backgroundColor: getShiftTypeColor(timeSlot.shiftType)
                                    }
                                }}
                            >
                                {slotIndex === 0 && (
                                    <td
                                        rowSpan={dayRowSpan}
                                        style={{...styles.tableCell, ...styles.dayCell}}
                                    >
                                        <div>
                                            <div style={{ fontWeight: 'bold', fontSize: '14px' }}>
                                                {day.dayOfWeek}
                                            </div>
                                            <div style={{ fontSize: '12px', color: '#666' }}>
                                                {formatDate(day.date)}
                                            </div>
                                        </div>
                                    </td>
                                )}

                                <td style={styles.tableCell}>
                                    <div style={{ fontWeight: 'bold' }}>
                                        {formatTime(timeSlot.startTime)} - {formatTime(timeSlot.endTime)}
                                    </div>
                                </td>

                                <td style={styles.tableCell}>
                                    <span
                                        style={{
                                            ...styles.chip,
                                            backgroundColor: getShiftTypeChipColor(timeSlot.shiftType),
                                            color: 'white'
                                        }}
                                    >
                                        {timeSlot.shiftType}
                                    </span>
                                </td>

                                <td style={styles.tableCell}>
                                    <span
                                        style={{
                                            ...styles.chip,
                                            backgroundColor: timeSlot.primaryEmployee ? '#4caf50' : '#ccc',
                                            color: 'white'
                                        }}
                                    >
                                        {timeSlot.primaryEmployee || 'Not Assigned'}
                                    </span>
                                </td>

                                <td style={styles.tableCell}>
                                    <span
                                        style={{
                                            ...styles.chip,
                                            backgroundColor: timeSlot.secondaryEmployee ? '#2196f3' : '#ccc',
                                            color: 'white'
                                        }}
                                    >
                                        {timeSlot.secondaryEmployee || 'Not Assigned'}
                                    </span>
                                </td>

                                <td style={styles.tableCell}>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', justifyContent: 'center' }}>
                                        {timeSlot.assignedEmployees && timeSlot.assignedEmployees.length > 0 ? (
                                            timeSlot.assignedEmployees.map((employee, empIndex) => (
                                                <span
                                                    key={empIndex}
                                                    style={{
                                                        ...styles.chip,
                                                        backgroundColor: '#ff9800',
                                                        color: 'white',
                                                        fontSize: '11px',
                                                        maxWidth: '120px',
                                                        overflow: 'hidden',
                                                        textOverflow: 'ellipsis'
                                                    }}
                                                >
                                                    {employee}
                                                </span>
                                            ))
                                        ) : (
                                            <span
                                                style={{
                                                    ...styles.chip,
                                                    backgroundColor: '#ccc',
                                                    color: 'white'
                                                }}
                                            >
                                                No Assignments
                                            </span>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ));
                    })}
                    </tbody>
                </table>
            </div>
        );
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h1 style={styles.header}>CharanaTV Weekly Duty Roster</h1>

                <div style={styles.controlsContainer}>
                    <input
                        type="date"
                        value={selectedWeekStart}
                        onChange={(e) => setSelectedWeekStart(e.target.value)}
                        style={styles.input}
                        title="Week Starting Date (Monday)"
                    />
                    <button
                        onClick={handleFetchData}
                        disabled={loading}
                        style={{
                            ...styles.button,
                            ...(loading && styles.buttonDisabled)
                        }}
                    >
                        {loading ? 'Loading...' : 'Refresh Data'}
                    </button>
                </div>

                <p style={{ textAlign: 'center', color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    📅 Data automatically loads when you change the week starting date
                </p>

                {rosterData && (
                    <div style={{ textAlign: 'center', marginBottom: '16px' }}>
                        <h3 style={{ color: '#666', marginBottom: '8px' }}>
                            {rosterData.rosterName}
                        </h3>
                        <p style={{ fontSize: '14px', color: '#666' }}>
                            Week of {formatDate(rosterData.weekStartingDate)} | Created: {formatDate(rosterData.createdDate)}
                            {rosterData.updatedDate !== rosterData.createdDate &&
                                ` | Updated: ${formatDate(rosterData.updatedDate)}`
                            }
                        </p>
                    </div>
                )}
            </div>

            {loading && (
                <div style={styles.loading}>
                    <div style={{ marginRight: '16px' }}>⏳</div>
                    Loading weekly roster data...
                </div>
            )}

            {error && (
                <div style={styles.error}>
                    <h3>Error Loading Data</h3>
                    <p>{error}</p>
                    <p style={{ marginTop: '8px' }}>
                        Please check:
                        <br />• Server is running on 192.168.3.20:8080
                        <br />• API endpoint is accessible
                        <br />• Selected week starting date has data available
                        <br />• Network connection is stable
                    </p>
                </div>
            )}

            {!loading && !error && rosterData && renderTableContent()}

            {!loading && !error && !rosterData && (
                <div style={styles.info}>
                    <h3>No Data</h3>
                    <p>
                        Please select a week starting date (Monday), then click &quot;Refresh Data&quot; to fetch the weekly roster.
                    </p>
                </div>
            )}

            {/* Summary Stats */}
            {rosterData && (
                <div style={styles.card}>
                    <h3 style={{ marginBottom: '16px' }}>Weekly Summary</h3>
                    <div style={styles.summaryGrid}>
                        <div style={styles.summaryBox}>
                            <div style={styles.summaryNumber}>
                                {rosterData.dailyDuties.length}
                            </div>
                            <div style={{ fontSize: '14px', color: '#666' }}>Days Scheduled</div>
                        </div>
                        <div style={styles.summaryBox}>
                            <div style={{...styles.summaryNumber, color: '#4caf50'}}>
                                {rosterData.dailyDuties.reduce((total, day) => total + day.timeSlots.length, 0)}
                            </div>
                            <div style={{ fontSize: '14px', color: '#666' }}>Total Shifts</div>
                        </div>
                        <div style={styles.summaryBox}>
                            <div style={{...styles.summaryNumber, color: '#ff9800'}}>
                                {rosterData.dailyDuties.reduce((total, day) =>
                                    total + day.timeSlots.filter(slot => slot.shiftType === 'MORNING').length, 0
                                )}
                            </div>
                            <div style={{ fontSize: '14px', color: '#666' }}>Morning Shifts</div>
                        </div>
                        <div style={styles.summaryBox}>
                            <div style={{...styles.summaryNumber, color: '#f44336'}}>
                                {rosterData.dailyDuties.reduce((total, day) =>
                                    total + day.timeSlots.filter(slot => slot.shiftType === 'EVENING').length, 0
                                )}
                            </div>
                            <div style={{ fontSize: '14px', color: '#666' }}>Evening Shifts</div>
                        </div>
                    </div>
                </div>
            )}

            {rosterData && (
                <div style={styles.card}>
                    <h3 style={{ marginBottom: '16px' }}>Shift Type Legend</h3>
                    <div style={styles.legendContainer}>
                        {['MORNING', 'EVENING', 'AFTERNOON', 'NIGHT'].map((shiftType) => (
                            <span
                                key={shiftType}
                                style={{
                                    ...styles.chip,
                                    backgroundColor: getShiftTypeChipColor(shiftType),
                                    color: 'white',
                                    padding: '8px 16px',
                                    fontSize: '14px'
                                }}
                            >
                                {shiftType}
                            </span>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default CharanaTVRosterTable;
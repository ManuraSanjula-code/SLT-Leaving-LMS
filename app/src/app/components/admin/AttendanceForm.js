import React, { useState } from 'react';
import { PlusCircle, X, Calendar, Clock } from 'lucide-react';

const AttendanceForm = ({ isVisible, onClose }) => {
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        date: new Date().toISOString().split('T')[0],
        employeeID: '',
        isFullDay: false,
        arrivalDate: new Date().toISOString().split('T')[0],
        arrivalTime: '',
        leftTime: '',
        isLate: false,
        lateCover: false,
        isHalfDay: false,
        isFullLeave: false,
        isShortLeave: false,
        isAbsent: false,
        isUnSuccessful: false,
        isNoPay: false,
        issues: false,
        isUnAuthorized: false,
        resolve: false,
        leaveSuccess: false,
        leaveReq: false,
        issueDescription: '',
        dueDateForUA: '',
        active: true,
        nopay: false,
        userId: '',
        viaMovement: false,
        viaLeave: false
    });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async () => {
        try {
            const response = await fetch('http://localhost:8080/lms', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
                credentials: 'include' // This will send cookies with the request
            });

            if (response.ok) {
                alert('Attendance submitted successfully!');
                setShowModal(false);
                onClose();
                // Reset form or perform other actions as needed
            } else {
                alert('Failed to submit attendance.');
            }
        } catch (error) {
            console.error('Error submitting attendance:', error);
            alert('Error submitting attendance. Please try again.');
        }
    };
    if (!isVisible) return null;

    return (
        <div className="relative">
            {/* Plus Button */}
            <button
                onClick={() => setShowModal(true)}
                className="flex items-center justify-center p-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors"
            >
                <PlusCircle size={24} />
            </button>

            {/* Modal */}

        </div>
    );
};

export default AttendanceForm;
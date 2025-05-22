"use client";

import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { clearAuth } from '../../lib/redux/redux-user/authSlice';

export function BrowserCloseHandler() {
    const dispatch = useDispatch();

    useEffect(() => {
        const handleTabClose = () => {
            // Clear redux state when browser tab/window is closing
            dispatch(clearAuth());
        };

        window.addEventListener('beforeunload', handleTabClose);

        return () => {
            window.removeEventListener('beforeunload', handleTabClose);
        };
    }, [dispatch]);

    return null; // This component doesn't render anything
}
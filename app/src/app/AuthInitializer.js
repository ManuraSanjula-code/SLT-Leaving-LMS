"use client";

import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { setCredentials, setUserDetails, setError, clearAuth } from "./redux-user/authSlice";
import { fetchData_ } from "./api";
import { persistor } from "./redux-user/store";

export default function AuthInitializer() {
  const dispatch = useDispatch();

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        await persistor.flush();

        const jwt = sessionStorage.getItem('jwt');
        const userId = sessionStorage.getItem('userId');
        const storedDetails = sessionStorage.getItem('userDetails');

        if (!jwt || !userId) return;

        // Sanitize stored user details before parsing
        if (storedDetails) {
          try {
            const parsedDetails = JSON.parse(storedDetails);
            // Validate basic structure before using stored details
            if (parsedDetails && typeof parsedDetails === 'object') {
              dispatch(setCredentials({ jwt, userId }));
              dispatch(setUserDetails(parsedDetails));
              return;
            }
          } catch (parseError) {
            // If stored data is corrupted, clear it and fetch fresh
            console.error("Stored user details invalid, fetching fresh data");
            sessionStorage.removeItem('userDetails');
          }
        }

        // Set credentials anyway to authenticate API requests
        dispatch(setCredentials({ jwt, userId }));

        // Fetch fresh user data
        const userData = await fetchData_(userId, jwt);

        // Validate API response before storing
        if (!userData || !userData.roles) {
          throw new Error('Invalid user data received');
        }

        // Store sanitized user details
        const sanitizedData = {
          ...userData,
          // Remove any sensitive fields if needed before storing
        };

        sessionStorage.setItem('userDetails', JSON.stringify(sanitizedData));
        dispatch(setUserDetails(sanitizedData));
      } catch (error) {
        console.error("Auth initialization error:", error);

        // Sanitize error messages before displaying to user
        const safeErrorMessage = error.message?.includes('network')
            ? 'Network connection error'
            : 'Authentication error, please log in again';

        dispatch(setError(safeErrorMessage));
        // Handle logout for critical auth errors
        handleLogout();
      }
    };

    const handleLogout = () => {
      sessionStorage.removeItem('jwt');
      sessionStorage.removeItem('userId');
      sessionStorage.removeItem('userDetails');
      dispatch(clearAuth());
      // Optional: Redirect to login page
      // window.location.href = '/login';
    };

    initializeAuth();
  }, [dispatch]);

  return null;
}
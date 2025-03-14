"use client";
import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { setCredentials, setUserDetails, setError } from "./redux/authSlice";
import { fetchData_ } from "./api";
import { persistor } from "./redux/store";

export default function AuthInitializer() {
  const dispatch = useDispatch();

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        await persistor.flush();

        const jwt = localStorage.getItem('jwt');
        const userId = localStorage.getItem('userId');
        const storedDetails = localStorage.getItem('userDetails');
        
        if (!jwt || !userId) return;

        dispatch(setCredentials({ jwt, userId }));

        if (storedDetails) {
          dispatch(setUserDetails(JSON.parse(storedDetails)));
          return;
        }

        const userData = await fetchData_(userId);
        localStorage.setItem('userDetails', JSON.stringify(userData));
        dispatch(setUserDetails(userData));
      } catch (error) {
        console.error("Auth initialization error:", error);
        dispatch(setError(error.message));
       // handleLogout();
      }
    };

    initializeAuth();
  }, [dispatch]);

  return null;
}
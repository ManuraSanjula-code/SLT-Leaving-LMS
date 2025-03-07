"use client"; // This is a client component

import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { getCookie } from "cookies-next";
import { fetchData } from "./api";
import { setCredentials, setUserDetails, setError, setLoading } from "./redux/authSlice";

export default function AuthInitializer() {
  const dispatch = useDispatch();

  useEffect(() => {
    // Step 1: Check for cookies
    const jwtFromCookie = getCookie("jwt")?.toString();
    const userIdFromCookie = getCookie("userId")?.toString();

    if (jwtFromCookie && userIdFromCookie) {
      // Step 2: Store credentials in Redux
      dispatch(setCredentials({ jwt: jwtFromCookie, userId: userIdFromCookie }));

      // Step 3: Check if user details are already in sessionStorage
      const storedUserDetails = sessionStorage.getItem("userDetails");
      if (storedUserDetails) {
        console.log("Restoring user details from sessionStorage:", JSON.parse(storedUserDetails));
        dispatch(setUserDetails(JSON.parse(storedUserDetails))); // Restore from sessionStorage
      } else {
        // Step 4: Fetch user details using JWT and userId
        dispatch(setLoading(true)); // Start loading
        fetchData(`/users/${userIdFromCookie}`, jwtFromCookie)
          .then((userData) => {
            console.log("Fetched User Data:", userData); // Log the API response
            sessionStorage.setItem("userDetails", JSON.stringify(userData)); // Save to sessionStorage
            dispatch(setUserDetails(userData)); // Store user details in Redux
          })
          .catch((error) => {
            console.error("API Error:", error); // Log detailed error
            dispatch(setError("Failed to fetch user details"));
          })
          .finally(() => {
            dispatch(setLoading(false)); // Stop loading
          });
      }
    } else {
      // Step 5: Handle missing cookies (authentication failure)
      console.warn("Missing cookies: Clearing auth state");
      dispatch(setError("Authentication failed: Missing credentials"));
    }
  }, [dispatch]);

  return null; // This component doesn’t render anything
}
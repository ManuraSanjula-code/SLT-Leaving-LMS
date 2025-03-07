"use client";

import { usePathname } from "next/navigation";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { hasAccess } from "./roleAccess";
import UnauthorizedPage from "./unauthorized/page";
import { Box, CircularProgress } from "@mui/material";
import {clearAuth } from "./redux/authSlice"

export default function ClientProtectedRoute({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const reduxUser = useSelector((state) => state.auth); // Use the correct slice name
  const [isLoading, setIsLoading] = useState(true);
  const [isUnauthorized, setIsUnauthorized] = useState(false);
  const [isServerError, setIsServerError] = useState(false); // New state for server errors

  const handleLogout = () => {
    document.cookie = "userId=; path=/;";
    document.cookie = "jwt=; path=/;";
    dispatch(clearAuth()); // Now dispatch is defined
    router.push("/login");
  };


  // Get cookies
  const getUserIdFromCookie = () => {
    return document.cookie
      .split("; ")
      .find((row) => row.startsWith("userId="))
      ?.split("=")[1];
  };

  const getJwtFromCookie = () => {
    return document.cookie
      .split("; ")
      .find((row) => row.startsWith("jwt="))
      ?.split("=")[1];
  };

  useEffect(() => {
    // Handle login route

    // if (pathname === "/login") {
    //   if (reduxUser?.userId && getJwtFromCookie()) {
    //     router.replace("/dashboard"); // Redirect authenticated users
    //     return;
    //   }
    //   setIsLoading(false);
    //   return;
    // }

    if (pathname === "/login" || pathname === "/error") {
      if (pathname === "/login" && reduxUser?.userId && getJwtFromCookie()) {
        router.replace("/dashboard");
        return;
      }
      setIsLoading(false);
      return;
    }

    // Check authentication
    const userIdCookie = getUserIdFromCookie();
    const jwtCookie = getJwtFromCookie();

    if (!userIdCookie || !jwtCookie) {
      router.push("/login");
      setIsLoading(false);
      return;
    }

    // Wait for user data to be fetched and validated
    if (reduxUser.loading) {
      return; // Still loading, do nothing
    }

    // Validate user data
    if (!reduxUser.userId || reduxUser.userId !== userIdCookie) {
      router.push("/login");
      setIsLoading(false);
      return;
    }

    // Check if roles are available
    const roles = reduxUser.userDetails?.roles || [];

    // if (roles.length === 0) {
    //   // No roles found, likely due to server error
    //   // setIsServerError(true);
    //   // setIsLoading(false);
    //   handleLogout();
    //   return;
    // }

    if (roles.length === 0 && pathname !== '/error') {
      router.push('/error'); // Redirect to generic error instead of logout
      setIsLoading(false);
      return;
    }

    if (roles.length === 0) {
      if (pathname !== '/error') {
        router.push('/error');
      }
      setIsLoading(false);
      return;
    }

    // Check role access
    const hasRouteAccess = hasAccess(roles, pathname);

    if (!hasRouteAccess) {
      setIsUnauthorized(true);
    }

    setIsLoading(false);
  }, [pathname, reduxUser, router]);

  // Redirect for unauthorized access
  useEffect(() => {
    if (isUnauthorized) {
      router.push("/unauthorized");
    }
  }, [isUnauthorized, router]);

  if (isLoading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="100vh"
      >
        <CircularProgress size={60} thickness={4} />
      </Box>
    );
  }

  if (isServerError) {
    return <UnauthorizedPage />;
  }

  if (isUnauthorized) {
    return <UnauthorizedPage />;
  }

  return children;
}
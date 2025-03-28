"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { Box, CircularProgress } from "@mui/material";
import { clearAuth } from "./redux/authSlice";

const PUBLIC_ROUTES = ["/login", "/error", "/unauthorized", "/forgot-password"];

export default function ClientProtectedRoute({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const dispatch = useDispatch();
  const reduxUser = useSelector((state) => state.auth);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const resetInvalidAuth = () => {
      const userId = localStorage.getItem('userId');
      const jwt = localStorage.getItem('jwt');

      // If tokens exist but don't match Redux, or Redux has error state
      if ((userId && !reduxUser.userId) ||
          (userId && reduxUser.userId && userId !== reduxUser.userId) ||
          reduxUser.errorMessage) {
        localStorage.removeItem('userId');
        localStorage.removeItem('jwt');
        dispatch(clearAuth());
      }
    };

    resetInvalidAuth();
  }, [dispatch]);

  useEffect(() => {
    const checkAuth = () => {
      const userId = localStorage.getItem('userId');
      const jwt = localStorage.getItem('jwt');

      // Public routes - no auth needed
      if (PUBLIC_ROUTES.includes(pathname)) {
        if (pathname === "/login" && (userId || reduxUser?.userId)) {
          router.replace("/dashboard");
        }
        setIsLoading(false);
        return;
      }

      if (!userId || !jwt || !reduxUser.userId || userId !== reduxUser.userId) {
        localStorage.removeItem('userId');
        localStorage.removeItem('jwt');
        dispatch(clearAuth());
        router.push("/login");
        return;
      }

      setIsLoading(false);
    };

    const timer = setTimeout(checkAuth, 100);
    return () => clearTimeout(timer);
  }, [pathname, reduxUser, router, dispatch]);

  if (isLoading) {
    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
          <CircularProgress size={60} thickness={4} />
        </Box>
    );
  }

  return children;
}
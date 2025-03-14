"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { hasAccess } from "./roleAccess";
import { Box, CircularProgress } from "@mui/material";
import { clearAuth } from "./redux/authSlice";

export default function ClientProtectedRoute({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const dispatch = useDispatch();
  const reduxUser = useSelector((state) => state.auth);
  const [isLoading, setIsLoading] = useState(true);

  const handleLogout = () => {
    try {
      localStorage.removeItem('userId');
      localStorage.removeItem('jwt');
      dispatch(clearAuth());
      router.push("/login");
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        const userId = localStorage.getItem('userId');
        const jwt = localStorage.getItem('jwt');

        // Skip protection checks for these routes
        if (["/login", "/error", "/unauthorized"].includes(pathname)) {
          if (pathname === "/login" && (userId || reduxUser?.userId)) {
            router.replace("/dashboard");
          }
          setIsLoading(false);
          return;
        }

        if (!userId || !jwt) {
          await handleLogout();
          return;
        }

        if (reduxUser.loading) return;

        if (!reduxUser.userId || reduxUser.userId !== userId) {
          await handleLogout();
          return;
        }

        const roles = reduxUser.userDetails?.roles || [];
        
        if (roles.length === 0) {
          console.error("No roles found - server error");
          await handleLogout();
          return;
        }

        const hasRouteAccess = hasAccess(roles, pathname);
        
        if (!hasRouteAccess) {
          router.push("/unauthorized");
          return;
        }

        setIsLoading(false);
      } catch (error) {
        console.error("Auth verification error:", error);
        await handleLogout();
      }
    };

    verifyAuth();
  }, [pathname, reduxUser, router, dispatch]);

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

  return children;
}
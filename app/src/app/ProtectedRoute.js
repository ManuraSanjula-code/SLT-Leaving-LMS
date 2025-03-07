'use client'; // This is a client component

import { useRouter } from 'next/navigation'; // Use Next.js 13+ router
import { useEffect } from 'react';
import { getCookie } from 'cookies-next';
import { useDispatch, useSelector } from 'react-redux';
import { fetchData } from './api';
import { setCredentials, setUserDetails, setError, setLoading } from './redux/authSlice';
import LoadingSpinner from './components/LoadingSpinner'; // Import the spinner

const ProtectedRoute = ({ children }) => {
  const router = useRouter();
  const dispatch = useDispatch();
  const { userDetails, loading, errorMessage } = useSelector((state) => state.auth);

  useEffect(() => {
    // Step 1: Check for cookies
    const jwtFromCookie = getCookie('jwt')?.toString();
    const userIdFromCookie = getCookie('userId')?.toString();

    if (!jwtFromCookie || !userIdFromCookie) {
      console.warn('Missing cookies: Redirecting to login');
      router.push('/login'); // Redirect to login if cookies are missing
      return;
    }

    // Step 2: Store credentials in Redux
    dispatch(setCredentials({ jwt: jwtFromCookie, userId: userIdFromCookie }));

    // Step 3: Fetch user details if not already fetched
    if (!userDetails.firstName && !loading) {
      dispatch(setLoading(true)); // Start loading
      fetchData(`/users/${userIdFromCookie}`, jwtFromCookie)
        .then((userData) => {
          console.log('Fetched User Data:', userData); // Log the API response
          dispatch(setUserDetails(userData)); // Store user details in Redux
        })
        .catch((error) => {
          console.error('API Error:', error); // Log detailed error
          dispatch(setError('Failed to fetch user details'));
          router.push('/login'); // Redirect to login on error
        })
        .finally(() => {
          dispatch(setLoading(false)); // Stop loading
        });
    }
  }, [router, dispatch, userDetails, loading]);

  // Step 4: Show loading spinner while fetching user details
  if (loading) {
    return <LoadingSpinner />;
  }

  // Step 5: Render children if authenticated
  return <>{children}</>;
};

export default ProtectedRoute;
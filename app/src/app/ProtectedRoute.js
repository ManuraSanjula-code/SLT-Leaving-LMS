'use client';

import { useRouter } from 'next/navigation'; 
import { useEffect } from 'react';
import { getCookie } from 'cookies-next';
import { useDispatch, useSelector } from 'react-redux';
import { fetchData } from './api';
import { setCredentials, setUserDetails, setError, setLoading } from '../../lib/redux/redux-user/authSlice';
import LoadingSpinner from './components/LoadingSpinner'; 

const ProtectedRoute = ({ children }) => {
  const router = useRouter();
  const dispatch = useDispatch();
  const { userDetails, loading, errorMessage } = useSelector((state) => state.auth);

  useEffect(() => {
    const jwtFromCookie = getCookie('jwt')?.toString();
    const userIdFromCookie = getCookie('userId')?.toString();

    if (!jwtFromCookie || !userIdFromCookie) {
      console.warn('Missing cookies: Redirecting to login');
      router.push('/login'); 
      return;
    }

    dispatch(setCredentials({ jwt: jwtFromCookie, userId: userIdFromCookie }));

    if (!userDetails.firstName && !loading) {
      dispatch(setLoading(true)); 
      fetchData(`/users/${userIdFromCookie}`, jwtFromCookie)
        .then((userData) => {
          dispatch(setUserDetails(userData)); 
        })
        .catch((error) => {
          console.error('API Error:', error); 
          dispatch(setError('Failed to fetch user details'));
          router.push('/login'); 
        })
        .finally(() => {
          dispatch(setLoading(false)); 
        });
    }
  }, [router, dispatch, userDetails, loading]);

  if (loading) {
    return <LoadingSpinner />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
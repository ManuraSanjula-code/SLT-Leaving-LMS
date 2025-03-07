// app/error-boundary.js
"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function ErrorBoundary({ children }) {
  const router = useRouter();

  useEffect(() => {
    const handleError = (event) => {
      router.push('/error');
    };

    window.addEventListener('error', handleError);
    return () => window.removeEventListener('error', handleError);
  }, [router]);

  return children;
}
// In app/RootLayout.js

import { Geist, Geist_Mono } from "next/font/google";
import './styles/globals.css';
import { ReduxProvider } from './redux-user/provider';
import AuthInitializer from './AuthInitializer';
import Notification from './Notification';
import ClientProtectedRoute from './ClientProtectedRoute';
import ErrorBoundary from './error-boundary';
import { BrowserCloseHandler } from './BrowserCloseHandler'; // Import the component

const geistSans = Geist({ /* ... */ });
const geistMono = Geist_Mono({ /* ... */ });

export default async function RootLayout({ children }) {
  return (
      <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
      <ReduxProvider>
        <AuthInitializer />
        <Notification />
        <BrowserCloseHandler /> {/* Add it here inside the ReduxProvider */}
        <ErrorBoundary>
          <ClientProtectedRoute>
            {children}
          </ClientProtectedRoute>
        </ErrorBoundary>
      </ReduxProvider>
      </body>
      </html>
  );
}
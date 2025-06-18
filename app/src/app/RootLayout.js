import { Geist, Geist_Mono } from "next/font/google";
import './styles/globals.css';
import { ReduxProvider } from '../../lib/redux/provider';
import AuthInitializer from './AuthInitializer';
import Notification from './Notification';
import ClientProtectedRoute from './ClientProtectedRoute';
import ErrorBoundary from './error-boundary';
import { BrowserCloseHandler } from './BrowserCloseHandler';

export default async function RootLayout({ children }) {
  return (
      <html lang="en">
      <body>
      <ReduxProvider>
        <AuthInitializer />
        <Notification />
        <BrowserCloseHandler />
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
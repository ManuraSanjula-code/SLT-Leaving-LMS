import { Geist, Geist_Mono } from "next/font/google";
import './styles/globals.css';
import { ReduxProvider } from './redux/provider';
import AuthInitializer from './AuthInitializer';
import Notification from './Notification';
import ClientProtectedRoute from './ClientProtectedRoute';
import ErrorBoundary from './error-boundary'

const geistSans = Geist({ /* ... */ });
const geistMono = Geist_Mono({ /* ... */ });

export default async function RootLayout({ children }) {

  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <ReduxProvider>
          <AuthInitializer />
          <Notification />
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
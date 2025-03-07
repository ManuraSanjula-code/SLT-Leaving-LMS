// app/layout.js (Server Component)
import RootLayout from "./RootLayout"; // Import the client component

export const metadata = {
  title: "SLT Leaving Management System",
  description: "SLT Leaving Management System",
};

export default function Layout({ children }) {
  return <RootLayout>{children}</RootLayout>;
}
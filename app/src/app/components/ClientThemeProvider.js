// "use client";
//
// import { ThemeProvider, createTheme } from '@mui/material/styles';
// import { CssBaseline } from '@mui/material';
//
// // Create a consistent light theme that prevents dark mode
// const lightTheme = createTheme({
//     palette: {
//         mode: 'light',
//         primary: {
//             main: '#1976d2',
//         },
//         secondary: {
//             main: '#dc004e',
//         },
//         background: {
//             default: '#ffffff',
//             paper: '#ffffff',
//         },
//         text: {
//             primary: '#000000',
//             secondary: '#666666',
//         },
//     },
//     components: {
//         // Force light theme on all MUI components
//         MuiCssBaseline: {
//             styleOverrides: {
//                 body: {
//                     backgroundColor: '#ffffff !important',
//                     color: '#000000 !important',
//                     colorScheme: 'light !important',
//                 },
//                 html: {
//                     backgroundColor: '#ffffff !important',
//                     colorScheme: 'light !important',
//                 },
//                 // Override system dark mode
//                 '@media (prefers-color-scheme: dark)': {
//                     body: {
//                         backgroundColor: '#ffffff !important',
//                         color: '#000000 !important',
//                     },
//                     html: {
//                         backgroundColor: '#ffffff !important',
//                     },
//                 },
//             },
//         },
//         MuiPaper: {
//             styleOverrides: {
//                 root: {
//                     backgroundColor: '#ffffff !important',
//                     color: '#000000 !important',
//                 },
//             },
//         },
//         MuiContainer: {
//             styleOverrides: {
//                 root: {
//                     backgroundColor: '#ffffff !important',
//                     color: '#000000 !important',
//                 },
//             },
//         },
//         MuiTypography: {
//             styleOverrides: {
//                 root: {
//                     color: '#000000 !important',
//                 },
//             },
//         },
//     },
// });
//
// export default function ClientThemeProvider({ children }) {
//     return (
//         <ThemeProvider theme={lightTheme}>
//             <CssBaseline />
//             {children}
//         </ThemeProvider>
//     );
// }
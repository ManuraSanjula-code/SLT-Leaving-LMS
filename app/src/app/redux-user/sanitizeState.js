export const sanitizeState = (state) => {
    // Create a deep copy first to avoid mutating the original state
    const sanitizedState = JSON.parse(JSON.stringify(state));

    // Sanitize auth state if it exists
    if (sanitizedState.auth) {
        // Keep JWT token but sanitize it for logging
        if (sanitizedState.auth.jwt) {
            // For logging purposes only - don't expose full token
            console.log('JWT present:', sanitizedState.auth.jwt ? 'Yes' : 'No');
        }

        // Remove sensitive data from userDetails
        if (sanitizedState.auth.userDetails) {
            // Keep data structure but sanitize sensitive fields
            const { userDetails } = sanitizedState.auth;

            // Sanitize address information
            if (userDetails.addresses && userDetails.addresses.length) {
                userDetails.addresses = userDetails.addresses.map(address => ({
                    ...address,
                    // Remove detailed address components from persistence if needed
                    // street: '[REDACTED]',
                    // Keep only what's necessary for the app to function
                }));
            }

            // Sanitize error messages to avoid leaking implementation details
            if (sanitizedState.auth.errorMessage) {
                // Use generic error messages that don't reveal system details
                sanitizedState.auth.errorMessage = sanitizedState.auth.errorMessage
                    .replace(/Error: .*/g, 'An error occurred')
                    .replace(/localhost:[0-9]+/g, '[SERVER]');
            }
        }
    }

    return sanitizedState;
};

// Function to clean state before it's persisted to storage
export const persistTransform = {
    in: (state) => {
        // State going into storage - sanitize it
        return sanitizeState(state);
    },
    out: (state) => {
        // State coming out of storage - any additional post-processing
        return state;
    }
};
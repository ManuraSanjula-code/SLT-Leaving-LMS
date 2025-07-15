export const sanitizeState = (state) => {
    const sanitizedState = JSON.parse(JSON.stringify(state));

    if (sanitizedState.auth) {
        if (sanitizedState.auth.jwt) {
            console.log('JWT present:', sanitizedState.auth.jwt ? 'Yes' : 'No');
        }

        if (sanitizedState.auth.userDetails) {
            const { userDetails } = sanitizedState.auth;

            if (userDetails.addresses && userDetails.addresses.length) {
                userDetails.addresses = userDetails.addresses.map(address => ({
                    ...address,
                }));
            }

            if (sanitizedState.auth.errorMessage) {
                sanitizedState.auth.errorMessage = sanitizedState.auth.errorMessage
                    .replace(/Error: .*/g, 'An error occurred')
                    .replace(/localhost:[0-9]+/g, '[SERVER]');
            }
        }
    }

    return sanitizedState;
};

export const persistTransform = {
    in: (state) => {
        return sanitizeState(state);
    },
    out: (state) => {
        return state;
    }
};
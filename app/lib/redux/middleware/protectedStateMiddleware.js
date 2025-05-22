/*const protectedStateMiddleware = store => next => action => {
    // Check if action attempts to modify protected sections
    if (action.type.startsWith('users/') && !action.meta?.hasAdminRights) {
        console.warn('Attempted unauthorized access to protected state section');
        // Either block the action or modify it
        return next({
            ...action,
            payload: {
                ...action.payload,
                // Strip sensitive data or limit scope
                restrictedData: undefined
            }
        });
    }

    return next(action);
};

export default protectedStateMiddleware;*/
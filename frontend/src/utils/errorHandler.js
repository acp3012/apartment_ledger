/**
 * Extracts a user-friendly error message from an API / Axios error.
 * Handles service-down / connection refused, proxy gateway timeouts,
 * rate limiting, and backend validation/business errors.
 *
 * @param {any} err - The caught error object
 * @param {string} [fallbackMessage="An unexpected error occurred. Please try again."] - Fallback when no specific message is available
 * @returns {string} The user-facing error message
 */
export const getApiErrorMessage = (
  err,
  fallbackMessage = "An unexpected error occurred. Please try again.",
) => {
  // 1. Backend is offline, connection refused (ECONNREFUSED), or network disconnected
  if (
    !err?.response ||
    err.code === "ERR_NETWORK" ||
    err.message === "Network Error"
  ) {
    return "Unable to connect to the server. The service may be offline or starting up. Please try again in a few moments.";
  }

  // 2. Gateway / Proxy errors (502 Bad Gateway, 503 Service Unavailable, 504 Gateway Timeout)
  if (err.response.status >= 500) {
    return "Service is temporarily unavailable. Please try again shortly.";
  }

  // 3. Rate limiting (429 Too Many Requests)
  if (err.response.status === 429) {
    return (
      err.response.data?.message ||
      "Too many attempts. Please wait a minute before trying again."
    );
  }

  // 4. Backend business / validation error (400, 401, 403, 404, 409)
  return err.response.data?.message || fallbackMessage;
};

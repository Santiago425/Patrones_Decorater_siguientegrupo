/** Turns anything thrown by the API layer into a message fit for the UI. */
export function describeError(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return 'Unexpected error.'
}
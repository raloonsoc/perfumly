// Mirrors the backend's @Pattern on RegisterRequest/ResetPasswordRequest
// (min 8 chars, upper, lower, digit, special char).
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=[\]{};:'",.<>/\\|~`]).{8,}$/

export function isPasswordValid(password: string) {
  return PASSWORD_PATTERN.test(password)
}

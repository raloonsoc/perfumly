import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api";

interface CurrentUser {
  id: string;
  username: string;
  email: string;
}


export function useCurrentUser() {
  return useQuery({
    queryKey: ["auth", "me"],
    queryFn: () => apiFetch<CurrentUser>("/api/auth/me"),
    retry: false,
  })
}


export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => apiFetch("/api/auth/logout", { method: "POST" }),
    onSuccess: () => {
      // We know for certain there's no user now — write it directly instead of
      // invalidating, which would refetch /api/auth/me and briefly keep stale data.
      queryClient.setQueryData(["auth", "me"], null)
    }
  })
}

export function useLogoutAllSessions() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => apiFetch("/api/auth/logout-all", { method: "POST" }),
    onSuccess: () => {
      queryClient.setQueryData(["auth", "me"], null)
    }
  })
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (credentials: { email: string, password: string }) => apiFetch<CurrentUser>("/api/auth/login", { method: "POST", body: JSON.stringify(credentials) }),
    onSuccess: (user) => {
      queryClient.setQueryData(["auth", "me"], user)
    }
  });
}

interface RegisterResponse {
  message: string;
}

export function useRegister() {
  // Registration no longer logs the user in (email must be verified first),
  // so there's no "auth", "me" cache to update here.
  return useMutation({
    mutationFn: (credentials: { username: string, email: string, password: string }) =>
      apiFetch<RegisterResponse>("/api/auth/register", { method: "POST", body: JSON.stringify(credentials) }),
  });
}

export function useVerifyEmail() {
  return useMutation({
    mutationFn: (token: string) => apiFetch<void>(`/api/auth/verify-email?token=${encodeURIComponent(token)}`),
  });
}

interface MessageResponse {
  message: string;
}

export function useForgotPassword() {
  return useMutation({
    mutationFn: (email: string) =>
      apiFetch<MessageResponse>("/api/auth/forgot-password", { method: "POST", body: JSON.stringify({ email }) }),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (credentials: { token: string, newPassword: string }) =>
      apiFetch<MessageResponse>("/api/auth/reset-password", { method: "POST", body: JSON.stringify(credentials) }),
  });
}

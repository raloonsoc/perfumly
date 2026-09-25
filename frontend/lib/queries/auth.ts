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

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (credentials: { email: string, password: string }) => apiFetch<CurrentUser>("/api/auth/login", { method: "POST", body: JSON.stringify(credentials) }),
    onSuccess: (user) => {
      queryClient.setQueryData(["auth", "me"], user)
    }
  });
}

export function useRegister() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (credentials: { username: string, email: string, password: string }) => apiFetch<CurrentUser>("/api/auth/register", { method: "POST", body: JSON.stringify(credentials) }),
    onSuccess: (user) => {
      queryClient.setQueryData(["auth", "me"], user)
    }
  });
}

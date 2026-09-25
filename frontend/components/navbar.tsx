"use client"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { ThemeToggle } from "@/components/theme-toggle"
import { useCurrentUser } from "@/lib/queries/auth"
import { UserMenu } from "@/components/user-menu"

export default function Navbar() {
  // `status` is explicit ('pending' | 'error' | 'success'), unlike `isLoading`/`data`
  // which can't reliably distinguish "not checked yet" from "confirmed logged out"
  // once retry:false has settled on a 401. See queries/auth.ts.
  const { data: currentUser, status } = useCurrentUser();

  return (
    <header className="fixed top-4 inset-x-0 z-50 flex justify-center px-4">
      <nav className="flex items-center gap-6 rounded-full border border-border bg-card/70 backdrop-blur-md px-5 py-2.5 shadow-sm">
        <Link href="/" className="font-serif text-lg font-semibold">Perfumly</Link>
        <Link href="/perfumes" className="text-sm text-muted-foreground hover:text-foreground">Catalogue</Link>
        <div className="flex items-center gap-2 ml-2">
          <ThemeToggle />
          {status === "pending" ? null : status === "success" && currentUser ? (
            <UserMenu id={currentUser.id} username={currentUser.username} email={currentUser.email} />
          ) : (
            <Button size="sm" nativeButton={false} render={<Link href="/login" />}>Log in</Button>
          )}
        </div>
      </nav>
    </header>
  )
}

"use client"

import * as React from "react"
import { useRouter, usePathname, useSearchParams } from "next/navigation"
import { cn } from "@/lib/utils"
import { CatalogueSearch } from "@/components/catalogue-search"
import { BrandCombobox } from "@/components/brand-combobox"

const GENDERS = [
  { value: undefined, label: "All" },
  { value: "FEMALE", label: "Women" },
  { value: "MALE", label: "Men" },
  { value: "UNISEX", label: "Unisex" },
] as const

const CatalogueTransitionContext = React.createContext<{
  navigate: (params: URLSearchParams) => void
  isPending: boolean
} | null>(null)

export function useCatalogueTransition() {
  const ctx = React.useContext(CatalogueTransitionContext)
  if (!ctx) throw new Error("useCatalogueTransition must be used within CatalogueFilters")
  return ctx
}

export function CatalogueFilters({
  gender,
  search,
  brandId,
  brandName,
  children,
}: {
  gender?: string
  search?: string
  brandId?: string
  brandName?: string
  children: React.ReactNode
}) {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const [isPending, startTransition] = React.useTransition()

  const navigate = React.useCallback(
    (params: URLSearchParams) => {
      startTransition(() => {
        router.push(`${pathname}?${params.toString()}`)
      })
    },
    [router, pathname]
  )

  function setGender(value: string | undefined) {
    const params = new URLSearchParams(searchParams.toString())
    params.delete("page")
    if (value) params.set("gender", value)
    else params.delete("gender")
    navigate(params)
  }

  return (
    <CatalogueTransitionContext.Provider value={{ navigate, isPending }}>
      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <nav className="flex items-center gap-1 rounded-full border border-border bg-card p-1 w-fit">
          {GENDERS.map((option) => {
            const isActive = gender === option.value || (!gender && !option.value)
            return (
              <button
                key={option.label}
                type="button"
                onClick={() => setGender(option.value)}
                className={cn(
                  "rounded-full px-3.5 py-1.5 text-sm transition-colors",
                  isActive
                    ? "bg-primary text-primary-foreground font-medium"
                    : "text-muted-foreground hover:text-foreground"
                )}
              >
                {option.label}
              </button>
            )
          })}
        </nav>

        <CatalogueSearch defaultValue={search} />
        <BrandCombobox brandId={brandId} brandName={brandName} />
      </div>

      <div className={cn("transition-opacity", isPending && "opacity-60")}>
        {children}
      </div>
    </CatalogueTransitionContext.Provider>
  )
}

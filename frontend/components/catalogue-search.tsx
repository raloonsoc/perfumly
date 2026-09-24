"use client"

import * as React from "react"
import { useSearchParams } from "next/navigation"
import { Input } from "@/components/ui/input"
import { useCatalogueTransition } from "@/components/catalogue-filters"

export function CatalogueSearch({ defaultValue }: { defaultValue?: string }) {
  const searchParams = useSearchParams()
  const { navigate } = useCatalogueTransition()
  const [value, setValue] = React.useState(defaultValue ?? "")

  React.useEffect(() => {
    const timeout = setTimeout(() => {
      const current = searchParams.get("search") ?? ""
      if (value === current) return

      const params = new URLSearchParams(searchParams.toString())
      params.delete("page")
      if (value) params.set("search", value)
      else params.delete("search")

      navigate(params)
    }, 350)

    return () => clearTimeout(timeout)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value])

  return (
    <Input
      type="search"
      placeholder="Search by name…"
      value={value}
      onChange={(e) => setValue(e.target.value)}
      className="w-full sm:max-w-xs rounded-full"
    />
  )
}

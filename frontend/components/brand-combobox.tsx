"use client"

import * as React from "react"
import { useSearchParams } from "next/navigation"
import {
  Combobox,
  ComboboxInput,
  ComboboxContent,
  ComboboxList,
  ComboboxItem,
  ComboboxEmpty,
} from "@/components/ui/combobox"
import { apiFetch } from "@/lib/api"
import { BrandSummary, PageResponse } from "@/lib/types"
import { useCatalogueTransition } from "@/components/catalogue-filters"

export function BrandCombobox({
  brandId,
  brandName,
}: {
  brandId?: string
  brandName?: string
}) {
  const searchParams = useSearchParams()
  const { navigate } = useCatalogueTransition()

  const [items, setItems] = React.useState<BrandSummary[]>([])
  const [loading, setLoading] = React.useState(false)

  const selected = brandId && brandName ? { id: brandId, name: brandName, country: "" } : null

  async function fetchBrands(search: string) {
    setLoading(true)
    try {
      const data = await apiFetch<PageResponse<BrandSummary>>(
        `/api/brands?search=${encodeURIComponent(search)}&size=10`
      )
      setItems(data.content)
    } catch {
      setItems([])
    } finally {
      setLoading(false)
    }
  }

  function handleValueChange(value: BrandSummary | null) {
    const params = new URLSearchParams(searchParams.toString())
    params.delete("page")
    if (value) {
      params.set("brandId", value.id)
      params.set("brandName", value.name)
    } else {
      params.delete("brandId")
      params.delete("brandName")
    }
    navigate(params)
  }

  return (
    <Combobox<BrandSummary>
      items={items}
      value={selected}
      onValueChange={handleValueChange}
      onInputValueChange={(value) => {
        if (value.length >= 2) fetchBrands(value)
        else setItems([])
      }}
      itemToStringLabel={(item) => item.name}
      isItemEqualToValue={(a, b) => a.id === b.id}
    >
      <ComboboxInput
        placeholder="Filter by brand…"
        showClear
        className="w-full sm:w-56"
      />
      <ComboboxContent>
        <ComboboxList>
          {items.map((brand) => (
            <ComboboxItem key={brand.id} value={brand}>
              {brand.name}
            </ComboboxItem>
          ))}
          <ComboboxEmpty>
            {loading ? "Searching…" : "No brands found"}
          </ComboboxEmpty>
        </ComboboxList>
      </ComboboxContent>
    </Combobox>
  )
}

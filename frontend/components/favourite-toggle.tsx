"use client"

import { Heart } from "lucide-react"
import { Toggle } from "@/components/ui/toggle"
import { cn } from "@/lib/utils"

export function FavouriteToggle({ className }: { className?: string }) {
  return (
    <Toggle
      size="sm"
      aria-label="Add to favourites"
      className={cn(
        "bg-black/20 text-white hover:bg-black/30 hover:text-white aria-pressed:bg-black/30  backdrop-blur-sm",
        className
      )}
      onClick={(e) => {
        e.preventDefault()
        e.stopPropagation()
      }}
    >
      <Heart className="group-aria-pressed/toggle:fill-foreground" />
    </Toggle>
  )
}

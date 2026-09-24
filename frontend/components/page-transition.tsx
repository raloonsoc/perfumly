import { ViewTransition } from "react";

export function PageTransition({ children }: { children: React.ReactNode }) {
  return (
    <ViewTransition default="auto" exit="page-fade" enter="page-fade">
      {children}
    </ViewTransition>
  );
}

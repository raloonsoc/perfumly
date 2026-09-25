"use client"

import { useEffect, ViewTransition } from "react";

// The browser aborts an in-flight View Transition (InvalidStateError: "Document hidden")
// whenever the tab loses visibility mid-transition — e.g. switching tabs right after a
// navigation. It's benign and doesn't affect the app, but surfaces as an unhandled
// rejection; swallow just that one case instead of letting it hit the console as an error.
function useSwallowAbortedViewTransitions() {
  useEffect(() => {
    function handleRejection(event: PromiseRejectionEvent) {
      if (event.reason instanceof DOMException && event.reason.name === "InvalidStateError") {
        event.preventDefault();
      }
    }
    window.addEventListener("unhandledrejection", handleRejection);
    return () => window.removeEventListener("unhandledrejection", handleRejection);
  }, []);
}

export function PageTransition({ children }: { children: React.ReactNode }) {
  useSwallowAbortedViewTransitions();

  return (
    <ViewTransition default="auto" exit="page-fade" enter="page-fade">
      {children}
    </ViewTransition>
  );
}

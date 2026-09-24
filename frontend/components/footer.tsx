import Link from "next/link";

const links = [
  { href: "/perfumes", label: "Catalogue" },
  { href: "/login", label: "Log in" },
  { href: "/register", label: "Sign up" },
];

export default function Footer() {
  return (
    <footer className="mt-16 border-t border-border">
      <div className="container mx-auto px-4 py-12">
        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-8">
          <div>
            <p className="font-serif text-lg">Perfumly</p>
            <p className="mt-1 text-sm text-muted-foreground max-w-sm text-balance">
              A catalogue and review platform for fragrance enthusiasts.
            </p>
          </div>

          <nav className="flex flex-wrap gap-x-6 gap-y-2 text-sm">
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>

        <p className="mt-10 text-xs text-muted-foreground text-balance">
          Fragrance data sourced for cataloguing purposes only. Perfumly is not affiliated with any brand listed.
        </p>
      </div>
    </footer>
  );
}

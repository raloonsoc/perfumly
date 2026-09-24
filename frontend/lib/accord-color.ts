function hashString(value: string, salt = ""): number {
  let hash = 0;
  const input = salt + value;
  for (let i = 0; i < input.length; i++) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function accordGradient(accord: string): string {
  const key = accord.toLowerCase();
  const hue = hashString(key, "h") % 360;
  const hueShift = 12 + (hashString(key, "s") % 24); // 12–35deg between stops
  const lightness = 0.68 + (hashString(key, "l") % 16) / 100; // 0.68–0.83
  const chroma = 0.07 + (hashString(key, "c") % 8) / 100; // 0.07–0.14

  const fromHue = hue;
  const toHue = (hue + hueShift) % 360;
  const angle = hashString(key, "a") % 360;

  const from = `oklch(${lightness.toFixed(2)} ${chroma.toFixed(2)} ${fromHue})`;
  const to = `oklch(${(lightness - 0.28).toFixed(2)} ${(chroma + 0.04).toFixed(2)} ${toHue})`;
  return `linear-gradient(${angle}deg, ${from}, ${to})`;
}

function hashString(value: string): number {
  let hash = 0;
  for (let i = 0; i < value.length; i++) {
    hash = (hash << 5) - hash + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function accordGradient(accord: string): string {
  const hue = hashString(accord.toLowerCase()) % 360;
  const from = `oklch(0.78 0.09 ${hue})`;
  const to = `oklch(0.5 0.13 ${hue})`;
  return `linear-gradient(135deg, ${from}, ${to})`;
}

export function formatPeso(value: number) {
  return `₱${Math.abs(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function signedPeso(value: number) {
  return `${value >= 0 ? "+" : "-"}${formatPeso(value)}`;
}
type CurrencyCode = "PHP" | "USD" | "EUR";

const EXCHANGE_RATES: Record<CurrencyCode, number> = {
  PHP: 1,
  USD: 61.50,
  EUR: 72.16,
};

function currencySymbol(code: CurrencyCode) {
  switch (code) {
    case "PHP":
      return "₱";
    case "USD":
      return "$";
    case "EUR":
      return "€";
    default:
      return "₱";
  }
}

function getCurrentCurrency(): CurrencyCode {
  if (typeof window === "undefined") return "PHP";
  const saved = (localStorage.getItem("currency") as CurrencyCode) || "PHP";
  return saved;
}

export function formatCurrency(value: number, code?: CurrencyCode) {
  const currency = code || getCurrentCurrency();
  const convertedValue = value / EXCHANGE_RATES[currency];
  const symbol = currencySymbol(currency);
  const prefix = convertedValue < 0 ? "-" : "";
  return `${prefix}${symbol}${Math.abs(convertedValue).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function signedCurrency(value: number, code?: CurrencyCode) {
  return `${value >= 0 ? "+" : "-"}${formatCurrency(Math.abs(value), code)}.`.replace(/\.$/, "");
}

// Backwards-compatible aliases
export const formatPeso = (v: number) => formatCurrency(v, "PHP");
export const signedPeso = (v: number) => signedCurrency(v, "PHP");

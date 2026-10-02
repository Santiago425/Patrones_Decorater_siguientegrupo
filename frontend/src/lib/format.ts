const copFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

const dateTimeFormatter = new Intl.DateTimeFormat('en-US', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

/** `$ 2.000.000`, the way Colombian pesos are written. */
export function formatCop(amount: number): string {
  return copFormatter.format(Math.round(amount))
}

/** Always signed, so a layer amount can be read at a glance. */
export function formatSignedCop(amount: number): string {
  return `${amount < 0 ? '-' : '+'} ${formatCop(Math.abs(amount))}`
}

export function formatDateTime(value: string): string {
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? value : dateTimeFormatter.format(parsed)
}

/** Keeps the amount field digits only, grouped by thousands as the user types. */
export function sanitizeAmountInput(raw: string): string {
  const digits = raw.replace(/\D/g, '').replace(/^0+(?=\d)/, '')
  return digits === '' ? '' : digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

export function parseAmountInput(text: string): number {
  const digits = text.replace(/\D/g, '')
  return digits === '' ? 0 : Number(digits)
}
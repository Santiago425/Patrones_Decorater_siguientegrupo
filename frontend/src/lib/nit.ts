/**
 * Colombian NIT check digit, same algorithm as the backend `@ValidNit`
 * validator: DIAN weights applied from right to left, `r = sum % 11`,
 * `dv = r in (0, 1) ? r : 11 - r`.
 */
const DIAN_WEIGHTS = [3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71]

export type NitIssue = 'required' | 'format' | 'check-digit'

export interface NitValidation {
  valid: boolean
  issue?: NitIssue
  /** Check digit implied by the entered digits, when it could be computed. */
  expectedCheckDigit?: number
  /** Canonical `body-dv` form sent to the API. */
  normalized: string
}

/** Returns the check digit of a NIT body, or `null` when it is not 1-15 digits. */
export function nitCheckDigit(body: string): number | null {
  if (!/^\d{1,15}$/.test(body)) {
    return null
  }
  let sum = 0
  for (let offset = 0; offset < body.length; offset += 1) {
    sum += Number(body.charAt(body.length - 1 - offset)) * DIAN_WEIGHTS[offset]
  }
  const remainder = sum % 11
  return remainder <= 1 ? remainder : 11 - remainder
}

/**
 * Accepts `123456789-0` and digits only. Dots and spaces are ignored so
 * `900.373.913-1` is read as `900373913-1`.
 */
export function validateNit(raw: string): NitValidation {
  const compact = raw.replace(/[\s.]/g, '')
  if (compact === '') {
    return { valid: false, issue: 'required', normalized: '' }
  }
  const match = /^(\d{1,15})-?(\d)$/.exec(compact)
  if (!match) {
    return { valid: false, issue: 'format', normalized: compact }
  }
  const [, body, checkDigitText] = match
  const expectedCheckDigit = nitCheckDigit(body)
  if (expectedCheckDigit === null) {
    return { valid: false, issue: 'format', normalized: compact }
  }
  const normalized = `${body}-${checkDigitText}`
  if (Number(checkDigitText) !== expectedCheckDigit) {
    return { valid: false, issue: 'check-digit', expectedCheckDigit, normalized }
  }
  return { valid: true, normalized }
}

export function nitErrorMessage(validation: NitValidation): string {
  switch (validation.issue) {
    case 'required':
      return 'Merchant NIT is required.'
    case 'format':
      return 'Use digits plus the check digit, for example 123456789-0.'
    case 'check-digit':
      return `Check digit should be ${validation.expectedCheckDigit}.`
    default:
      return ''
  }
}
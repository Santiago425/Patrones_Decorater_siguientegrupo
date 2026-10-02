export type Bearer = 'PAYER' | 'MERCHANT'

export type OptionCode = 'VAT' | 'GATEWAY_FEE' | 'WITHHOLDING' | 'GMF' | 'CASHBACK'

export interface OptionDefinition {
  code: string
  name: string
  description: string
  pricingRule: string
}

export interface Layer {
  code: string
  label: string
  amountCop: number
  bearer: Bearer
  notes: string[]
}

export interface Quote {
  currency: string
  baseAmountCop: number
  payerTotalCop: number
  merchantNetCop: number
  layers: Layer[]
}

export interface QuoteRequest {
  amountCop: number
  merchantNit: string
  description?: string
  options: string[]
}

export interface Payment {
  id: string
  status: string
  createdAt: string
  quote: Quote
}

export interface LoginResponse {
  token: string
  expiresAt: string
}

export interface ApiErrorBody {
  error: string
  message: string
}

export const MIN_AMOUNT_COP = 1_000
export const MAX_AMOUNT_COP = 100_000_000
import type { OptionDefinition, Payment, Quote, QuoteRequest } from '../types'
import { getJson, postJson } from './client'

export interface CreatePaymentResult {
  payment: Payment
  /** True when the backend answered with `Idempotent-Replayed: true`. */
  replayed: boolean
}

export async function fetchOptions(signal?: AbortSignal): Promise<OptionDefinition[]> {
  return getJson<OptionDefinition[]>('/api/v1/options', { signal })
}

export async function fetchQuote(payload: QuoteRequest, signal?: AbortSignal): Promise<Quote> {
  return postJson<Quote>('/api/v1/payments/quote', payload, { signal }).then((result) => result.data)
}

export async function createPayment(payload: QuoteRequest, idempotencyKey: string): Promise<CreatePaymentResult> {
  const { data, headers } = await postJson<Payment>('/api/v1/payments', payload, {
    headers: { 'Idempotency-Key': idempotencyKey },
  })
  return { payment: data, replayed: headers.get('Idempotent-Replayed') === 'true' }
}

export async function fetchPayments(): Promise<Payment[]> {
  return getJson<Payment[]>('/api/v1/payments')
}
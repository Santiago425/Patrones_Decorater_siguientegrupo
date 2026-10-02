import { useEffect, useState } from 'react'
import { fetchQuote } from '../api/paymentsApi'
import { describeError } from '../lib/errors'
import type { Quote, QuoteRequest } from '../types'

export interface QuoteState {
  quote: Quote | null
  error: string | null
  loading: boolean
}

interface QuoteResult {
  /** Input the stored quote or error belongs to. */
  input: QuoteRequest | null
  quote: Quote | null
  error: string | null
}

const EMPTY_RESULT: QuoteResult = { input: null, quote: null, error: null }

/**
 * Quotes the payment every time the debounced input changes. The previous quote
 * stays visible while a new one is being fetched, so typing does not blank the
 * right column.
 */
export function useQuote(input: QuoteRequest | null): QuoteState {
  const [result, setResult] = useState<QuoteResult>(EMPTY_RESULT)

  useEffect(() => {
    if (!input) {
      return
    }
    const controller = new AbortController()
    let active = true
    fetchQuote(input, controller.signal)
      .then((quote) => {
        if (active) {
          setResult({ input, quote, error: null })
        }
      })
      .catch((error: unknown) => {
        if (!active || controller.signal.aborted) {
          return
        }
        setResult({ input, quote: null, error: describeError(error) })
      })
    return () => {
      active = false
      controller.abort()
    }
  }, [input])

  const settled = input !== null && result.input === input
  return {
    quote: input ? result.quote : null,
    error: input ? result.error : null,
    loading: input !== null && !settled,
  }
}
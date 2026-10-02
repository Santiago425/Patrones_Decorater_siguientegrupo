import { useCallback, useEffect, useRef, useState } from 'react'
import { fetchPayments } from '../api/paymentsApi'
import { describeError } from '../lib/errors'
import type { Payment } from '../types'

export interface PaymentsHistoryState {
  payments: Payment[]
  loading: boolean
  error: string | null
  refresh: () => Promise<void>
}

/** Loads the payment history and exposes a refresh to call after a new payment. */
export function usePaymentsHistory(): PaymentsHistoryState {
  const [payments, setPayments] = useState<Payment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const mounted = useRef(true)

  useEffect(() => {
    mounted.current = true
    return () => {
      mounted.current = false
    }
  }, [])

  const load = useCallback(async () => {
    try {
      const list = await fetchPayments()
      if (mounted.current) {
        setPayments(list)
        setError(null)
      }
    } catch (caught: unknown) {
      if (mounted.current) {
        setError(describeError(caught))
      }
    } finally {
      if (mounted.current) {
        setLoading(false)
      }
    }
  }, [])

  const refresh = useCallback(async () => {
    setLoading(true)
    setError(null)
    await load()
  }, [load])

  useEffect(() => {
    // oxlint-disable-next-line react/set-state-in-effect -- fetching the history on mount is the external-system sync this hook performs.
    void load()
  }, [load])

  return { payments, loading, error, refresh }
}
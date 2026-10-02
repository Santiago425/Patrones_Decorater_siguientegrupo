import { useEffect, useState } from 'react'
import { fetchOptions } from '../api/paymentsApi'
import { FALLBACK_OPTIONS } from '../lib/optionCatalog'
import type { OptionDefinition } from '../types'

export interface OptionsState {
  options: OptionDefinition[]
  loading: boolean
  /** True when the catalog came from the spec instead of `GET /api/v1/options`. */
  usingFallback: boolean
}

export function useOptions(): OptionsState {
  const [options, setOptions] = useState<OptionDefinition[]>([])
  const [source, setSource] = useState<'loading' | 'api' | 'fallback'>('loading')

  useEffect(() => {
    const controller = new AbortController()
    fetchOptions(controller.signal)
      .then((list) => {
        if (!controller.signal.aborted) {
          setOptions(list)
          setSource('api')
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setOptions(FALLBACK_OPTIONS)
          setSource('fallback')
        }
      })
    return () => controller.abort()
  }, [])

  return { options, loading: source === 'loading', usingFallback: source === 'fallback' }
}
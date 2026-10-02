import { cloneElement, type ReactElement, type ReactNode } from 'react'

export type Tone = 'neutral' | 'accent' | 'warn'

const toneClasses: Record<Tone, string> = {
  neutral: 'bg-ink-100 text-ink-700 ring-ink-200',
  accent: 'bg-accent-100 text-accent-700 ring-accent-200',
  warn: 'bg-warn-100 text-warn-700 ring-warn-200',
}

/** Shared look for every text input and textarea of the form. */
export const inputClass =
  'w-full rounded-md border border-ink-200 bg-white px-3 py-2 text-sm text-ink-900 shadow-xs placeholder:text-ink-300 focus:border-accent-500 aria-invalid:border-warn-500'

type ControlProps = { 'aria-describedby'?: string }

export function Card({
  title,
  description,
  footer,
  children,
}: {
  title: string
  description?: string
  footer?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="rounded-lg border border-ink-200 bg-white shadow-xs">
      <header className="border-b border-ink-100 px-4 py-3 sm:px-5">
        <h2 className="text-sm font-semibold tracking-wide text-ink-900 uppercase">{title}</h2>
        {description ? <p className="mt-1 text-xs text-ink-700">{description}</p> : null}
      </header>
      <div className="px-4 py-4 sm:px-5">{children}</div>
      {footer ? <div className="border-t border-ink-100 px-4 py-3 sm:px-5">{footer}</div> : null}
    </section>
  )
}

export function Field({
  id,
  label,
  hint,
  error,
  children,
}: {
  id: string
  label: string
  hint?: string
  error?: string
  children: ReactElement<ControlProps>
}) {
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined
  const control = describedBy ? cloneElement(children, { 'aria-describedby': describedBy }) : children
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-ink-800">
        {label}
      </label>
      {control}
      {hint && !error ? (
        <p id={`${id}-hint`} className="text-xs text-ink-700">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={`${id}-error`} className="text-xs font-medium text-warn-700">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export function Badge({ tone, children }: { tone: Tone; children: ReactNode }) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold tracking-wide uppercase ring-1 ring-inset ${toneClasses[tone]}`}
    >
      {children}
    </span>
  )
}

export function Spinner({ label }: { label: string }) {
  return (
    <span className="inline-flex items-center gap-2 text-sm text-ink-700" role="status">
      <span
        aria-hidden="true"
        className="size-4 animate-spin rounded-full border-2 border-ink-200 border-t-accent-500"
      />
      {label}
    </span>
  )
}

export function ErrorState({
  title,
  message,
  onRetry,
  retryLabel = 'Try again',
}: {
  title: string
  message: string
  onRetry?: () => void
  retryLabel?: string
}) {
  return (
    <div className="flex flex-col items-start gap-3 rounded-md border border-warn-200 bg-warn-100 px-4 py-3">
      <div>
        <p className="text-sm font-semibold text-warn-700">{title}</p>
        <p className="mt-1 text-sm text-ink-800">{message}</p>
      </div>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="rounded-md bg-ink-900 px-3 py-1.5 text-sm font-medium text-white transition hover:bg-ink-800"
        >
          {retryLabel}
        </button>
      ) : null}
    </div>
  )
}

export function EmptyState({ title, message }: { title: string; message: string }) {
  return (
    <div className="rounded-md border border-dashed border-ink-200 bg-ink-50 px-4 py-6 text-center">
      <p className="text-sm font-medium text-ink-800">{title}</p>
      <p className="mt-1 text-sm text-ink-700">{message}</p>
    </div>
  )
}
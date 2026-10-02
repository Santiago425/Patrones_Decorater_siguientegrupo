/** 8 to 64 characters, as the `Idempotency-Key` header requires. */
export function newIdempotencyKey(): string {
  const webCrypto = globalThis.crypto
  if (webCrypto && typeof webCrypto.randomUUID === 'function') {
    return webCrypto.randomUUID()
  }
  return `key-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
}
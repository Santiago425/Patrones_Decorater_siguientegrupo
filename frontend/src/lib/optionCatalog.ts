import type { OptionDefinition } from '../types'

/**
 * Catalog from the design spec, used only as a fallback when
 * `GET /api/v1/options` is unreachable so the form still shows its rules.
 */
export const FALLBACK_OPTIONS: OptionDefinition[] = [
  {
    code: 'VAT',
    name: 'VAT (IVA)',
    description: 'Value added tax charged on top of the purchase.',
    pricingRule: '+19% of the base amount, paid by the payer',
  },
  {
    code: 'GATEWAY_FEE',
    name: 'Gateway fee',
    description: 'Percentage plus fixed fee charged by the payment gateway.',
    pricingRule: '2.65% of the base amount + 900 COP, borne by the merchant',
  },
  {
    code: 'WITHHOLDING',
    name: 'Withholding tax (retefuente)',
    description: 'Withholding the merchant pays on the sale.',
    pricingRule: '2.5% of the base amount, only from 27 UVT, borne by the merchant',
  },
  {
    code: 'GMF',
    name: 'GMF (4x1000)',
    description: 'Financial transaction tax on the payer total.',
    pricingRule: '0.4% of base amount plus VAT, paid by the payer',
  },
  {
    code: 'CASHBACK',
    name: 'Cashback',
    description: 'Credit returned to the payer after the layers above.',
    pricingRule: '1% of the base amount, capped at 20,000 COP, credited to the payer',
  },
]
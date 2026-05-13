import { formatCurrency, formatPeso, signedCurrency, signedPeso } from '../../../shared/utils/format';

describe('Format Utilities', () => {
  describe('formatPeso', () => {
    it('should format number as currency with peso sign', () => {
      expect(formatPeso(1000)).toBe('₱1,000.00');
    });

    it('should format decimal numbers correctly', () => {
      expect(formatPeso(1234.56)).toBe('₱1,234.56');
    });

    it('should format zero', () => {
      expect(formatPeso(0)).toBe('₱0.00');
    });

    it('should format small decimal values', () => {
      expect(formatPeso(0.99)).toBe('₱0.99');
    });

    it('should format negative numbers', () => {
      expect(formatPeso(-500)).toBe('-₱500.00');
    });

    it('should format large numbers', () => {
      expect(formatPeso(1000000)).toBe('₱1,000,000.00');
    });

    it('should handle number with many decimal places', () => {
      // Should round to 2 decimal places
      expect(formatPeso(123.456789)).toMatch(/₱123\.\d{2}/);
    });
  });

  describe('formatCurrency', () => {
    it('should convert php to usd', () => {
      expect(formatCurrency(50, 'USD')).toBe('$0.81');
    });

    it('should convert php to eur', () => {
      expect(formatCurrency(50, 'EUR')).toBe('€0.69');
    });

    it('should keep php amounts unchanged in php format', () => {
      expect(formatCurrency(50, 'PHP')).toBe('₱50.00');
    });
  });

  describe('signedPeso', () => {
    it('should format positive amount with + sign', () => {
      expect(signedPeso(500)).toBe('+₱500.00');
    });

    it('should format negative amount with - sign', () => {
      expect(signedPeso(-500)).toMatch(/^-₱500\.00|^₱-500\.00/);
    });

    it('should format zero as positive', () => {
      expect(signedPeso(0)).toBe('+₱0.00');
    });

    it('should maintain decimal places', () => {
      expect(signedPeso(123.45)).toBe('+₱123.45');
    });

    it('should format large negative numbers', () => {
      expect(signedPeso(-1000000)).toMatch(/^-₱|^₱-/);
    });

    it('should maintain precision for financial calculations', () => {
      // Test common split scenario: 1500 / 3 = 500
      expect(signedPeso(1500 / 3)).toMatch(/500/);
    });
  });

  describe('signedCurrency', () => {
    it('should convert and sign usd amounts', () => {
      expect(signedCurrency(50, 'USD')).toBe('+$0.81');
    });

    it('should convert and sign eur amounts', () => {
      expect(signedCurrency(-50, 'EUR')).toBe('-€0.69');
    });
  });

  describe('Financial Calculations', () => {
    it('should correctly format result of expense split', () => {
      const totalExpense = 3000;
      const numParticipants = 3;
      const perPersonAmount = totalExpense / numParticipants;

      expect(formatPeso(perPersonAmount)).toBe('₱1,000.00');
      expect(signedPeso(-perPersonAmount)).toMatch(/1,000/);
    });

    it('should handle group balance calculation', () => {
      // User A pays 3000 for 3 people (owes 1000 each)
      const payerAmount = 3000;
      const participantShare = payerAmount / 3;
      const payerBalance = payerAmount - 2 * participantShare; // 1000

      expect(formatPeso(payerBalance)).toBe('₱1,000.00');
    });

    it('should format settlement amount correctly', () => {
      const debtAmount = 500;
      expect(signedPeso(debtAmount)).toBe('+₱500.00');
      expect(signedPeso(-debtAmount)).toMatch(/500/);
    });
  });
});

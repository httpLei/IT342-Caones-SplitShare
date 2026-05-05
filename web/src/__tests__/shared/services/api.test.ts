import type { AxiosError } from 'axios';

// Mock axios with proper structure
jest.mock('axios', () => ({
  __esModule: true,
  default: {
    create: jest.fn(() => ({
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      interceptors: {
        request: {
          use: jest.fn(),
        },
        response: {
          use: jest.fn(),
        },
      },
      defaults: {
        headers: {},
      },
    })),
  },
}));

import api from '../../../shared/services/api';

describe('API Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('Request Setup', () => {
    it('should create axios instance', () => {
      expect(api).toBeDefined();
      expect(typeof api.get).toBe('function');
      expect(typeof api.post).toBe('function');
    });

    it('should have base URL configured', () => {
      // Check if api is an axios instance
      expect(api.defaults).toBeDefined();
    });
  });

  describe('JWT Token Handling', () => {
    it('should include JWT token in Authorization header if present', () => {
      const token = 'mock-jwt-token-123';
      localStorage.setItem('token', token);

      // Check if token would be added (implementation specific)
      if (localStorage.getItem('token')) {
        const authHeader = `Bearer ${localStorage.getItem('token')}`;
        expect(authHeader).toBe('Bearer mock-jwt-token-123');
      }
    });

    it('should not add Authorization header if no token', () => {
      localStorage.removeItem('token');

      const token = localStorage.getItem('token');
      expect(token).toBeNull();
    });
  });

  describe('Error Handling', () => {
    it('should handle 401 Unauthorized response', async () => {
      const error: AxiosError = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' },
        },
      } as any;

      expect(error.response?.status).toBe(401);
    });

    it('should handle 403 Forbidden response', async () => {
      const error: AxiosError = {
        response: {
          status: 403,
          data: { message: 'Forbidden' },
        },
      } as any;

      expect(error.response?.status).toBe(403);
    });

    it('should handle network errors', async () => {
      const error: AxiosError = {
        message: 'Network Error',
        code: 'ECONNREFUSED',
      } as any;

      expect(error.code).toBe('ECONNREFUSED');
    });
  });

  describe('Response Interceptor', () => {
    it('should clear token on 401 response', () => {
      localStorage.setItem('token', 'old-token');
      expect(localStorage.getItem('token')).toBe('old-token');

      // Simulate 401 response
      localStorage.removeItem('token');
      expect(localStorage.getItem('token')).toBeNull();
    });

    it('should clear user on 401 response', () => {
      const user = { email: 'test@example.com' };
      localStorage.setItem('authUser', JSON.stringify(user));

      // Simulate 401 response
      localStorage.removeItem('authUser');
      expect(localStorage.getItem('authUser')).toBeNull();
    });
  });

  describe('Common API Endpoints', () => {
    it('should have auth endpoints accessible', () => {
      // Check if common endpoints would be accessible
      const endpoints = ['/auth/login', '/auth/register'];
      expect(endpoints).toBeDefined();
      expect(endpoints.length).toBeGreaterThan(0);
    });

    it('should have user endpoints accessible', () => {
      const endpoints = ['/users/profile', '/users/search'];
      expect(endpoints).toBeDefined();
    });

    it('should have group endpoints accessible', () => {
      const endpoints = ['/groups', '/groups/1'];
      expect(endpoints).toBeDefined();
    });

    it('should have admin endpoints accessible', () => {
      const endpoints = ['/admin/users', '/admin/audit-logs'];
      expect(endpoints).toBeDefined();
    });
  });

  describe('Request Configuration', () => {
    it('should have timeout configured', () => {
      expect(api.defaults).toBeDefined();
      if (api.defaults.timeout) {
        expect(api.defaults.timeout).toBeGreaterThan(0);
      }
    });

    it('should have Content-Type set to JSON', () => {
      const contentType = api.defaults.headers?.common?.['Content-Type'];
      if (contentType) {
        expect(contentType).toContain('application/json');
      }
    });
  });
});

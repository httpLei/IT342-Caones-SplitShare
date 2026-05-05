import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AuthProvider, useAuth } from '../../../features/auth/AuthContext';
import type { UserDto } from '../../../features/auth/types/auth';

// Mock axios
jest.mock('../../../shared/services/api', () => ({
  __esModule: true,
  default: {
    post: jest.fn(),
  },
}));

describe('AuthContext', () => {
  const mockUser: UserDto = { email: 'test@example.com', firstname: 'John', lastname: 'Doe' };
  
  // Test component to use AuthContext
  const TestComponent = () => {
    const { user, isAuthenticated, login, logout } = useAuth();
    
    return (
      <div>
        <div data-testid="auth-status">
          {isAuthenticated ? 'Authenticated' : 'Not Authenticated'}
        </div>
        {user && <div data-testid="user-email">{user.email}</div>}
        <button onClick={() => login(mockUser, 'access-token-test', 'refresh-token-test')}>
          Login
        </button>
        <button onClick={logout}>Logout</button>
      </div>
    );
  };

  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  it('should render provider without crashing', () => {
    render(
      <AuthProvider>
        <div>Test</div>
      </AuthProvider>
    );
    expect(screen.getByText('Test')).toBeInTheDocument();
  });

  it('should provide useAuth hook', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    expect(screen.getByTestId('auth-status')).toHaveTextContent(
      'Not Authenticated'
    );
  });

  it('should initialize user from localStorage if token exists', () => {
    const storedUser: UserDto = { email: 'stored@example.com', firstname: 'John', lastname: 'Doe' };
    localStorage.setItem(
      'user',
      JSON.stringify(storedUser)
    );
    localStorage.setItem('accessToken', 'mock-token-123');

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('Authenticated');
    expect(screen.getByTestId('user-email')).toHaveTextContent('stored@example.com');
  });

  it('should logout and clear user data', () => {
    const logoutUser: UserDto = { email: 'logout@example.com', firstname: 'John', lastname: 'Doe' };
    localStorage.setItem('user', JSON.stringify(logoutUser));
    localStorage.setItem('accessToken', 'mock-token-123');

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('Authenticated');

    const logoutButton = screen.getByText('Logout');
    fireEvent.click(logoutButton);

    expect(screen.getByTestId('auth-status')).toHaveTextContent('Not Authenticated');
    expect(localStorage.getItem('accessToken')).toBeNull();
  });

  it('should throw error when useAuth is used outside provider', () => {
    // Suppress console.error for this test
    const consoleError = jest.spyOn(console, 'error').mockImplementation();

    const TestComponentOutsideProvider = () => {
      try {
        useAuth();
        return <div>Should not render</div>;
      } catch (error) {
        return <div>Error caught</div>;
      }
    };

    render(<TestComponentOutsideProvider />);
    expect(screen.getByText('Error caught')).toBeInTheDocument();

    consoleError.mockRestore();
  });

  it('should maintain authentication state across re-renders', () => {
    const persistUser: UserDto = { email: 'persist@example.com', firstname: 'Jane', lastname: 'Doe' };
    localStorage.setItem('user', JSON.stringify(persistUser));
    localStorage.setItem('accessToken', 'mock-token-456');

    const { rerender } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-email')).toHaveTextContent('persist@example.com');

    rerender(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-email')).toHaveTextContent('persist@example.com');
  });
});

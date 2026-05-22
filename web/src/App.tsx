import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./features/auth/AuthContext";
import { ThemeProvider } from "./features/settings/contexts/ThemeContext";
import { CategoriesProvider } from "./features/settings/contexts/CategoriesContext";
import { CurrencyProvider } from "./features/settings/contexts/CurrencyContext";
import ProtectedRoute from "./features/auth/components/ProtectedRoute";
import AdminRoute from "./features/auth/components/AdminRoute";
import UserLayout from "./features/layout/components/UserLayout";
import Login from "./features/auth/pages/LoginPage";
import OAuth2Success from "./features/auth/pages/OAuth2SuccessPage";
import Register from "./features/auth/pages/RegisterPage";
import Dashboard from "./features/dashboard/pages/DashboardPage";
import Groups from "./features/groups/pages/GroupsPage";
import GroupDetails from "./features/groups/pages/GroupDetailsPage";
import Activity from "./features/activity/pages/ActivityPage";
import ActivityDetails from "./features/activity/pages/ActivityDetailsPage";
import Profile from "./features/profile/pages/ProfilePage";
import Settings from "./features/settings/pages/SettingsPage";
import Admin from "./features/admin/pages/AdminPage";

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <CategoriesProvider>
          <CurrencyProvider>
          <AuthProvider>
            <Routes>
              <Route path="/"         element={<Navigate to="/login" replace />} />
              <Route path="/login"    element={<Login />} />
              <Route path="/oauth2/success" element={<OAuth2Success />} />
              <Route path="/register" element={<Register />} />
              <Route
                element={
                  <ProtectedRoute>
                    <UserLayout />
                  </ProtectedRoute>
                }
              >
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/groups" element={<Groups />} />
                <Route path="/groups/:groupId" element={<GroupDetails />} />
                <Route path="/groups/:groupId/expenses/:activityId" element={<ActivityDetails />} />
                <Route path="/activity" element={<Activity />} />
                <Route path="/activity/:activityId" element={<ActivityDetails />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/settings" element={<Settings />} />
              </Route>
              <Route
                path="/admin"
                element={
                  <AdminRoute>
                    <Admin />
                  </AdminRoute>
                }
              />
            </Routes>
          </AuthProvider>
          </CurrencyProvider>
        </CategoriesProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}

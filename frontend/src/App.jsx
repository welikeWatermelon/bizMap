import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './components/PrivateRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import MapPage from './pages/MapPage';
import StoreListPage from './pages/StoreListPage';
import StoreFormPage from './pages/StoreFormPage';
import StoreDetailPage from './pages/StoreDetailPage';
import DashboardPage from './pages/DashboardPage';
import FindStorePage from './pages/FindStorePage';
import WidgetPage from './pages/WidgetPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/find" element={<FindStorePage />} />
        <Route path="/map" element={<PrivateRoute><MapPage /></PrivateRoute>} />
        <Route path="/stores" element={<PrivateRoute><StoreListPage /></PrivateRoute>} />
        <Route path="/stores/new" element={<PrivateRoute><StoreFormPage /></PrivateRoute>} />
        <Route path="/stores/:id" element={<PrivateRoute><StoreDetailPage /></PrivateRoute>} />
        <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
        <Route path="/widget" element={<PrivateRoute><WidgetPage /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

import { Link, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Calendar,
  LogOut,
  Stethoscope,
  Activity,
  MapPin,
  FileText,
  Clock,
  UserCheck,
  ClipboardList,
  AlertTriangle
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { cn } from '../../lib/utils';
import DisableProfileButton from '../ui/DisableProfileButton';

const menuItems = {
  PATIENT: [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/appointments', label: 'Appuntamenti', icon: Calendar },
    { path: '/map', label: 'Cerca Nutrizionisti', icon: MapPin },
    { path: '/health-data', label: 'Dati Salute', icon: Activity },
    { path: '/plans', label: 'Piani Nutrizionali', icon: FileText },
  ],
  NUTRITIONIST: [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/proposals', label: 'Richieste', icon: ClipboardList },
    { path: '/appointments', label: 'Appuntamenti', icon: Calendar },
    { path: '/plans', label: 'Piani Nutrizionali', icon: FileText },
    { path: '/schedule', label: 'Orari', icon: Clock },
  ],
  ADMIN: [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/pending', label: 'Approvazioni', icon: UserCheck },
    { path: '/pending-disable', label: 'Richieste Disabilitazione', icon: AlertTriangle },
    { path: '/logs', label: 'Log Sistema', icon: Activity },
  ],
};

export function Sidebar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  
  if (!user) return null;
  
  const items = menuItems[user.role as keyof typeof menuItems] || [];

  const isActive = (path: string) => {
    if (path === '/dashboard') {
      return location.pathname === '/dashboard';
    }
    return location.pathname === path;
  };

  return (
    <div className="w-64 h-screen bg-slate-900 text-white fixed left-0 top-0 flex flex-col z-50">
      <div className="p-6 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <Stethoscope className="h-8 w-8 text-emerald-400" />
          <span className="font-bold text-xl">Nutritionists</span>
        </div>
        <div className="mt-4">
          <p className="text-sm text-slate-300">
            {user.firstName} {user.lastName}
          </p>
          <p className="text-xs text-slate-500 mt-1">{user.role}</p>
        </div>
      </div>
      
      <nav className="flex-1 p-4 space-y-1">
        {items.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.path);
          return (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                "flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200",
                active 
                  ? "bg-emerald-600 text-white shadow-lg" 
                  : "text-slate-300 hover:bg-slate-800 hover:text-white"
              )}
            >
              <Icon className={cn("h-5 w-5", active ? "text-white" : "text-slate-400")} />
              <span className="text-sm font-medium">{item.label}</span>
            </Link>
          );
        })}
      </nav>
      
      {/* BOTTONE DISABILITAZIONE */}
      <div className="p-4 border-t border-slate-800 space-y-2">
        {user.role !== 'ADMIN' && (
          <div className="mb-2">
            <DisableProfileButton />
          </div>
        )}
        
        <button
          onClick={logout}
          className="flex items-center gap-3 px-4 py-2.5 w-full rounded-lg text-slate-300 hover:bg-slate-800 hover:text-white transition-all duration-200"
        >
          <LogOut className="h-5 w-5" />
          <span className="text-sm font-medium">Logout</span>
        </button>
      </div>
    </div>
  );
}
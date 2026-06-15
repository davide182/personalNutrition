import type { ReactNode } from 'react';
import { Sidebar } from './Sidebar';
import { useAuth } from '../../context/AuthContext';

interface LayoutProps {
  children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const { user } = useAuth();
  
  if (!user) {
    return <>{children}</>;
  }
  
  return (
    <div className="min-h-screen bg-slate-50">
      <Sidebar />
      <main className="ml-64 p-8">
        <div className="max-w-7xl mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
}
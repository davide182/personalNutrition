import React, { createContext, useState, useContext, useEffect } from 'react';
import type { ReactNode } from 'react';
import api from '../api/axiosConfig';
import type { User, AuthResponse, RegisterData } from '../types';
import { toast } from 'sonner';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  checkUserStatus: () => Promise<void>;
  redirectToLogin: () => void;
  setNavigateCallback: (callback: (path: string) => void) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    const savedToken = localStorage.getItem('token');
    console.log('🔑 Token all\'avvio:', savedToken ? 'presente' : 'mancante');
    return savedToken;
  });
  const [loading, setLoading] = useState(false);
  

  let navigateCallback: ((path: string) => void) | null = null;


  const setNavigateCallback = (callback: (path: string) => void) => {
    navigateCallback = callback;
  };


  const redirectToLogin = () => {
    if (navigateCallback) {
      navigateCallback('/login');
    } else {
      window.location.href = '/login';
    }
  };

  useEffect(() => {
    if (token && user) {
      console.log('✅ Token presente all\'avvio');
      checkUserStatus();
    }
  }, [token]);


  useEffect(() => {
    if (!token || !user) return;
    
    const interval = setInterval(async () => {
      await checkUserStatus();
    }, 30000);
    
    return () => clearInterval(interval);
  }, [token, user]);


  const checkUserStatus = async () => {
    if (!token) return;
    
    try {
      const res = await api.get('/user/status');
      const data = res.data;
      
      if (!data.active) {
        console.log('⚠️ Account non più attivo:', data.message);
        toast.error(data.message);
        
        setTimeout(() => {
          logout();
          redirectToLogin();
        }, 2000);
      } else {
        if (user && user.status !== data.status) {
          const updatedUser = { ...user, status: data.status };
          setUser(updatedUser);
          localStorage.setItem('user', JSON.stringify(updatedUser));
        }
      }
    } catch (err) {
      console.error('❌ Errore verifica stato:', err);
    }
  };

  const login = async (email: string, password: string) => {
    setLoading(true);
    console.log('🔐 Tentativo login per:', email);
    try {
      const res = await api.post<AuthResponse>('/auth/login', { email, password });
      console.log('📦 Risposta login:', res.data);
      
      if (res.data.token) {
        setToken(res.data.token);
        localStorage.setItem('token', res.data.token);
        console.log('✅ Token salvato in localStorage');
        
        const userData: User = {
          userId: res.data.userId || 0,
          email: res.data.email,
          role: res.data.role,
          status: res.data.status,
          firstName: res.data.firstName,
          lastName: res.data.lastName,
        };
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        console.log('✅ Utente salvato:', userData.email, 'Ruolo:', userData.role);
        
        await checkUserStatus();
      }
    } catch (err: any) {
      console.error('❌ Errore login:', err.response?.data?.error || err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data: RegisterData) => {
    setLoading(true);
    console.log('📝 Tentativo registrazione per:', data.email);
    try {
      await api.post('/auth/register', data);
      console.log('✅ Registrazione completata');
    } catch (err: any) {
      console.error('❌ Errore registrazione:', err.response?.data?.error || err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    console.log('🚪 Logout in corso');
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    toast.info('Logout effettuato');
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      token, 
      loading, 
      login, 
      register, 
      logout, 
      checkUserStatus, 
      redirectToLogin, 
      setNavigateCallback 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
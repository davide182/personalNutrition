import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'sonner';

interface AccountStatusGuardProps {
  children: React.ReactNode;
}

const AccountStatusGuard: React.FC<AccountStatusGuardProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      if (user.status === 'SELF_DISABLED') {
        toast.error('Il tuo account è stato disabilitato. Contatta l\'amministratore per riattivarlo.');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      } else if (user.status === 'SUSPENDED') {
        toast.error('Il tuo account è in attesa di revisione. Riceverai una email quando sarà riattivato.');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      } else if (user.status === 'DISABLED') {
        toast.error('Il tuo account è stato disabilitato permanentemente.');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      } else if (user.status === 'PENDING') {
        toast.error('Il tuo account è in attesa di approvazione. Riceverai una email quando sarà attivato.');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      }
    }
  }, [user, logout, navigate]);

  return <>{children}</>;
};

export default AccountStatusGuard;
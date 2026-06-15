// src/components/ui/DisableProfileButton.tsx
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axiosConfig';
import { Button } from './button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './card';
import { Label } from './label';
import { UserX, Loader2, X } from 'lucide-react';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';

interface DisableProfileButtonProps {
  onDisabled?: () => void;
}

const DisableProfileButton: React.FC<DisableProfileButtonProps> = ({ onDisabled }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [reason, setReason] = useState('');

  const handleDisable = async () => {
    if (!user) return;

    setLoading(true);
    try {
      if (user.role === 'PATIENT') {
        await api.post('/profile/disable');
        toast.success('Profilo disabilitato con successo');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      } else if (user.role === 'NUTRITIONIST') {
        await api.post('/profile/disable/request', { reason });
        toast.success('Richiesta di disabilitazione inviata all\'amministratore');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
        setShowConfirm(false);
        setReason('');
      }
      onDisabled?.();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore durante la disabilitazione');
    } finally {
      setLoading(false);
    }
  };

  if (!showConfirm) {
    return (
      <Button 
        variant="destructive" 
        onClick={() => setShowConfirm(true)}
        className="w-full bg-red-600 hover:bg-red-700"
      >
        <UserX className="w-4 h-4 mr-2" />
        {user?.role === 'PATIENT' ? 'Disabilita Profilo' : 'Richiedi Disabilitazione'}
      </Button>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setShowConfirm(false)}>
      <Card className="max-w-md w-full mx-4 border-red-200 bg-red-50" onClick={(e) => e.stopPropagation()}>
        <CardHeader className="pb-2">
          <div className="flex justify-between items-start">
            <CardTitle className="text-red-700 flex items-center gap-2">
              <UserX className="w-5 h-5" />
              Conferma Disabilitazione
            </CardTitle>
            <button 
              onClick={() => setShowConfirm(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
          <CardDescription className="text-red-600">
            {user?.role === 'PATIENT' 
              ? 'Sei sicuro? Non potrai più accedere fino a quando l\'amministratore non riattiverà il tuo profilo.'
              : 'La tua richiesta sarà valutata dall\'amministratore. Riceverai una email di conferma. Verrai disconnesso immediatamente.'}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {user?.role === 'NUTRITIONIST' && (
            <div>
              <Label htmlFor="reason" className="text-gray-700">Motivo della disabilitazione (opzionale)</Label>
              <textarea
                id="reason"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Spiega il motivo della tua richiesta..."
                rows={3}
                className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
          )}
          <div className="flex gap-3">
            <Button 
              onClick={handleDisable} 
              disabled={loading}
              className="bg-red-600 hover:bg-red-700 flex-1"
            >
              {loading ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : null}
              Conferma
            </Button>
            <Button 
              variant="outline" 
              onClick={() => {
                setShowConfirm(false);
                setReason('');
              }}
              className="flex-1"
            >
              Annulla
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default DisableProfileButton;
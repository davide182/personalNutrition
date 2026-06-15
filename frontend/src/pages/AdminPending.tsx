import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, RefreshCw, Users } from 'lucide-react';
import { toast } from 'sonner';

interface PendingUser {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  status: string;
}

const AdminPending: React.FC = () => {
  const [pending, setPending] = useState<PendingUser[]>([]);
  const [disabled, setDisabled] = useState<PendingUser[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const pendingRes = await api.get('/admin/pending-nutritionists');
      setPending(pendingRes.data);
      const disabledRes = await api.get('/admin/disabled-nutritionists');
      setDisabled(disabledRes.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore nel caricamento');
    } finally {
      setLoading(false);
    }
  };

  const approve = async (userId: number) => {
    try {
      await api.put(`/admin/approve-nutritionist/${userId}`);
      toast.success('Nutrizionista approvato!');
      setPending(prev => prev.filter(u => u.userId !== userId));
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const reject = async (userId: number) => {
    try {
      await api.put(`/admin/reject-nutritionist/${userId}`);
      toast.success('Nutrizionista rifiutato');
      setPending(prev => prev.filter(u => u.userId !== userId));
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const reenable = async (userId: number) => {
    try {
      await api.put(`/admin/reenable-nutritionist/${userId}`);
      toast.success('Nutrizionista riportato in attesa');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Gestione Nutrizionisti</h1>
        <p className="text-gray-500 mt-1">Approva o rifiuta le nuove registrazioni</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5 text-yellow-500" />
            In attesa di approvazione
          </CardTitle>
          <CardDescription>Nutrizionisti che hanno richiesto la registrazione</CardDescription>
        </CardHeader>
        <CardContent>
          <Button onClick={loadData} variant="outline" className="mb-4" disabled={loading}>
            <RefreshCw className="h-4 w-4 mr-1" />
            Aggiorna
          </Button>
          {pending.length === 0 ? (
            <p className="text-gray-500 text-center py-8">✅ Nessuna richiesta in attesa</p>
          ) : (
            <div className="space-y-3">
              {pending.map(u => (
                <div key={u.userId} className="border rounded-lg p-4 flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{u.firstName} {u.lastName}</p>
                    <p className="text-sm text-gray-500">{u.email}</p>
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={() => approve(u.userId)} className="bg-green-600 hover:bg-green-700">
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Approva
                    </Button>
                    <Button onClick={() => reject(u.userId)} variant="destructive">
                      <XCircle className="h-4 w-4 mr-1" />
                      Rifiuta
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5 text-red-500" />
            Nutrizionisti disabilitati
          </CardTitle>
          <CardDescription>Account che possono essere riabilitati</CardDescription>
        </CardHeader>
        <CardContent>
          {disabled.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun nutrizionista disabilitato</p>
          ) : (
            <div className="space-y-3">
              {disabled.map(u => (
                <div key={u.userId} className="border rounded-lg p-4 flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{u.firstName} {u.lastName}</p>
                    <p className="text-sm text-gray-500">{u.email}</p>
                  </div>
                  <Button onClick={() => reenable(u.userId)} variant="outline" className="bg-yellow-50 hover:bg-yellow-100">
                    <RefreshCw className="h-4 w-4 mr-1" />
                    Riabilita
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default AdminPending;
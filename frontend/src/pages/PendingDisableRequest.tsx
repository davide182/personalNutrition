// src/pages/AdminPendingDisable.tsx
import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { CheckCircle, XCircle, Loader2, Users } from 'lucide-react';
import { toast } from 'sonner';
import { Label } from '@/components/ui/label';

interface PendingDisableRequest {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  status: string;
}

const AdminPendingDisable: React.FC = () => {
  const [requests, setRequests] = useState<PendingDisableRequest[]>([]);
  const [disabled, setDisabled] = useState<PendingDisableRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [showRejectDialog, setShowRejectDialog] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const pendingRes = await api.get('/profile/admin/pending-disable-requests');
      setRequests(pendingRes.data);
      const disabledRes = await api.get('/profile/admin/self-disabled');
      setDisabled(disabledRes.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore nel caricamento');
    } finally {
      setLoading(false);
    }
  };

  const approveDisable = async (userId: number) => {
    try {
      await api.put(`/profile/admin/disable-nutritionist/${userId}/approve`);
      toast.success('Disabilitazione approvata');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const rejectDisable = async (userId: number) => {
    try {
      await api.put(`/profile/admin/disable-nutritionist/${userId}/reject?reason=${encodeURIComponent(rejectReason)}`);
      toast.success('Richiesta di disabilitazione negata');
      setShowRejectDialog(false);
      setRejectReason('');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const reenableProfile = async (userId: number) => {
    try {
      await api.put(`/profile/admin/reenable/${userId}`);
      toast.success('Profilo riabilitato con successo');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Gestione Richieste Disabilitazione</h1>
        <p className="text-gray-500 mt-1">Approva o nega le richieste di disabilitazione dei nutrizionisti</p>
      </div>

      {/* Richieste in attesa */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5 text-yellow-500" />
            Richieste in attesa
          </CardTitle>
          <CardDescription>Nutrizionisti che hanno richiesto la disabilitazione del profilo</CardDescription>
        </CardHeader>
        <CardContent>
          {requests.length === 0 ? (
            <p className="text-gray-500 text-center py-8">✅ Nessuna richiesta in attesa</p>
          ) : (
            <div className="space-y-3">
              {requests.map(u => (
                <div key={u.userId} className="border rounded-lg p-4 flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{u.firstName} {u.lastName}</p>
                    <p className="text-sm text-gray-500">{u.email}</p>
                    <p className="text-xs text-yellow-600 mt-1">In attesa di approvazione</p>
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={() => approveDisable(u.userId)} className="bg-red-600 hover:bg-red-700">
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Approva Disabilitazione
                    </Button>
                    <Button 
                      variant="outline" 
                      onClick={() => {
                        setSelectedUserId(u.userId);
                        setShowRejectDialog(true);
                      }}
                    >
                      <XCircle className="h-4 w-4 mr-1" />
                      Nega
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Profili disabilitati */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5 text-red-500" />
            Profili Disabilitati
          </CardTitle>
          <CardDescription>Account disabilitati volontariamente che possono essere riabilitati</CardDescription>
        </CardHeader>
        <CardContent>
          {disabled.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun profilo disabilitato</p>
          ) : (
            <div className="space-y-3">
              {disabled.map(u => (
                <div key={u.userId} className="border rounded-lg p-4 flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{u.firstName} {u.lastName}</p>
                    <p className="text-sm text-gray-500">{u.email}</p>
                    <p className="text-xs text-gray-500 mt-1">Ruolo: {u.role}</p>
                  </div>
                  <Button onClick={() => reenableProfile(u.userId)} variant="outline" className="bg-green-50 hover:bg-green-100">
                    <CheckCircle className="h-4 w-4 mr-1" />
                    Riabilita Profilo
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialog per rifiuto */}
      <Dialog open={showRejectDialog} onOpenChange={setShowRejectDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nega Richiesta di Disabilitazione</DialogTitle>
            <DialogDescription>
              Inserisci il motivo del rifiuto. Il nutrizionista riceverà una email di notifica.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="rejectReason">Motivo del rifiuto</Label>
              <textarea
                id="rejectReason"
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Inserisci il motivo..."
                rows={3}
                className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
            </div>
            <div className="flex gap-3 justify-end">
              <Button variant="outline" onClick={() => setShowRejectDialog(false)}>
                Annulla
              </Button>
              <Button 
                variant="destructive" 
                onClick={() => selectedUserId && rejectDisable(selectedUserId)}
                disabled={!rejectReason.trim()}
              >
                Conferma Rifiuto
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminPendingDisable;
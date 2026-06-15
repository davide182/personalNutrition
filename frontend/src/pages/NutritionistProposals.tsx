import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, CalendarDays } from 'lucide-react';
import { toast } from 'sonner';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface Proposal {
  appointmentId: number;
  patientFirstName: string;
  patientLastName: string;
  patientEmail: string;
  startTime: string;
  endTime: string;
  distanceKm: number;
  positionInQueue: number;
}

const NutritionistProposals: React.FC = () => {
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState<Proposal | null>(null);
  const [price, setPrice] = useState('');

  useEffect(() => {
    loadProposals();
  }, []);

  const loadProposals = async () => {
    setLoading(true);
    try {
      const res = await api.get('/nutritionist/proposals');
      setProposals(res.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    } finally {
      setLoading(false);
    }
  };


  const handleAccept = async (proposal: Proposal) => {
    if (!price) {
      toast.error('Inserisci prezzo per l\'appuntamento');
      return;
    }
    const priceNum = parseFloat(price);
    if (isNaN(priceNum) || priceNum <= 0) {
      toast.error('Il prezzo deve essere maggiore di 0');
      return;
    }
    
    try {
      await api.put(`/nutritionist/proposals/${proposal.appointmentId}/accept`, {
        price: priceNum
      });
      toast.success('Proposta accettata! Appuntamento confermato.');
      setSelectedAppointment(null);
      setPrice('');
      loadProposals();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const handleReject = async (id: number) => {
    try {
      await api.put(`/nutritionist/proposals/${id}/reject`);
      toast.success('Proposta rifiutata');
      loadProposals();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Richieste in arrivo</h1>
        <p className="text-gray-500 mt-1">Gestisci le richieste di appuntamento dai pazienti</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Nuove richieste</CardTitle>
          <CardDescription>Accetta o rifiuta le richieste dei pazienti</CardDescription>
        </CardHeader>
        <CardContent>
          {loading && <p>Caricamento...</p>}
          {!loading && proposals.length === 0 && (
            <p className="text-gray-500 text-center py-8">Nessuna richiesta in arrivo</p>
          )}
          {proposals.map((p) => (
            <div key={p.appointmentId} className="border rounded-lg p-4 mb-4">
              {selectedAppointment?.appointmentId === p.appointmentId ? (
                <div className="space-y-4">
                  <h3 className="font-semibold">Conferma appuntamento</h3>
                  <div className="space-y-2">
                    <Label>Prezzo (€) *</Label>
                    <Input
                      type="number"
                      min="0.01"
                      step="0.01"
                      placeholder="es. 50"
                      value={price}
                      onChange={(e) => setPrice(e.target.value)}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={() => handleAccept(p)} className="bg-green-600">
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Conferma
                    </Button>
                    <Button variant="outline" onClick={() => setSelectedAppointment(null)}>
                      Annulla
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-semibold">{p.patientFirstName} {p.patientLastName}</p>
                    <p className="text-sm text-gray-500">{p.patientEmail}</p>
                    <p className="text-sm mt-1 flex items-center gap-1">
                      <CalendarDays className="h-3 w-3" />
                      {new Date(p.startTime).toLocaleString()}
                    </p>
                    <p className="text-sm text-gray-500">Distanza: {p.distanceKm} km</p>
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={() => setSelectedAppointment(p)} className="bg-green-600 hover:bg-green-700">
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Accetta
                    </Button>
                    <Button onClick={() => handleReject(p.appointmentId)} variant="destructive">
                      <XCircle className="h-4 w-4 mr-1" />
                      Rifiuta
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};

export default NutritionistProposals;
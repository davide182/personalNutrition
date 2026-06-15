import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CalendarDays, MapPin, Euro, CheckCircle, FileText, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';

interface Appointment {
  appointmentId: number;
  startTime: string;
  endTime: string;
  status: string;
  patientFirstName?: string;
  patientLastName?: string;
  location?: string;
  price?: number;
}

const NutritionistAppointments: React.FC = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      console.log('🔄 Caricamento appuntamenti nutrizionista...');
      const res = await api.get('/nutritionist/appointments');
      console.log('📅 Risposta:', res.data);
      setAppointments(res.data);
    } catch (err: any) {
      console.error('❌ Errore dettagliato:', err);
      if (err.response?.status === 404) {
        setAppointments([]);
      } else if (err.response?.status === 403) {
        toast.error('Sessione scaduta. Effettua di nuovo il login.');
      } else if (err.response?.data?.error) {
        toast.error(err.response.data.error);
      } else {
        console.error('Errore sconosciuto:', err);
      }
    } finally {
      setLoading(false);
    }
  };

  const completeAppointment = async (id: number) => {
    try {
      await api.put(`/nutritionist/appointments/${id}/complete`);
      toast.success('✅ Visita completata! Ora puoi creare un piano nutrizionale.');
      loadAppointments();

      navigate(`/plans?appointmentId=${id}`);
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      CONFIRMED: 'bg-green-100 text-green-800',
      COMPLETED: 'bg-purple-100 text-purple-800',
      CANCELLED: 'bg-red-100 text-red-800',
    };
    const labels: Record<string, string> = {
      CONFIRMED: 'Confermato',
      COMPLETED: 'Completato',
      CANCELLED: 'Cancellato',
    };
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status] || 'bg-gray-100'}`}>
        {labels[status] || status}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">I miei Appuntamenti</h1>
        <p className="text-gray-500 mt-1">Appuntamenti confermati con i pazienti</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Appuntamenti</CardTitle>
          <CardDescription>Elenco di tutti i tuoi appuntamenti</CardDescription>
        </CardHeader>
        <CardContent>
          <Button onClick={loadAppointments} variant="outline" className="mb-4" disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} />
            Aggiorna
          </Button>
          
          {loading && <p className="text-center py-8">Caricamento...</p>}
          
          {!loading && appointments.length === 0 && (
            <p className="text-gray-500 text-center py-8">Nessun appuntamento</p>
          )}
          
          <div className="space-y-3">
            {appointments.map((a) => (
              <div key={a.appointmentId} className="border rounded-lg p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-semibold">
                      Paziente: {a.patientFirstName || 'N/A'} {a.patientLastName || ''}
                    </p>
                    <p className="text-sm flex items-center gap-1 mt-1">
                      <CalendarDays className="h-3 w-3" />
                      {new Date(a.startTime).toLocaleString()}
                    </p>
                    {a.location && (
                      <p className="text-sm flex items-center gap-1 mt-1 text-gray-500">
                        <MapPin className="h-3 w-3" />
                        {a.location}
                      </p>
                    )}
                    {a.price && (
                      <p className="text-sm flex items-center gap-1 mt-1 text-gray-500">
                        <Euro className="h-3 w-3" />
                        €{a.price}
                      </p>
                    )}
                    <div className="mt-2">{getStatusBadge(a.status)}</div>
                  </div>
                  {a.status === 'CONFIRMED' && (
                    <Button onClick={() => completeAppointment(a.appointmentId)} className="bg-purple-600 hover:bg-purple-700">
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Completa visita
                    </Button>
                  )}
                  {a.status === 'COMPLETED' && (
                    <Button 
                      variant="outline" 
                      className="border-emerald-600 text-emerald-600"
                      onClick={() => navigate(`/plans?appointmentId=${a.appointmentId}`)}
                    >
                      <FileText className="h-4 w-4 mr-1" />
                      Crea/Visualizza Piano
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default NutritionistAppointments;
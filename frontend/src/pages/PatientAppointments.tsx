import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { X, Plus, Sparkles } from 'lucide-react';
import { toast } from 'sonner';

interface Appointment {
  appointmentId: number;
  startTime: string;
  endTime: string;
  status: string;
  nutritionistName?: string;
  price?: number;
  location?: string;
}

interface Specialization {
  specializationId: number;
  name: string;
  description: string;
}

const DEFAULT_SPECIALIZATIONS: Specialization[] = [
  { specializationId: 1, name: 'Nutrizione clinica', description: '' },
  { specializationId: 2, name: 'Nutrizione sportiva', description: '' },
  { specializationId: 3, name: 'Disturbi alimentari', description: '' },
  { specializationId: 4, name: 'Pediatria nutrizionale', description: '' },
  { specializationId: 5, name: 'Metabolismo e dimagrimento', description: '' },
  { specializationId: 6, name: 'Nutrizione vegetariana/vegana', description: '' },
  { specializationId: 7, name: 'Nutrizione in gravidanza', description: '' },
  { specializationId: 8, name: 'Gastroenterologia nutrizionale', description: '' },
];

const PatientAppointments: React.FC = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [specializations, setSpecializations] = useState<Specialization[]>(DEFAULT_SPECIALIZATIONS);
  const [appointmentDate, setAppointmentDate] = useState('');
  const [selectedNutritionistId, setSelectedNutritionistId] = useState<number | null>(null);
  const [selectedSpecializationId, setSelectedSpecializationId] = useState<number | null>(null);
  const [requestType, setRequestType] = useState<'specific' | 'general'>('specific');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const savedId = localStorage.getItem('selectedNutritionistId');
    if (savedId) {
      setSelectedNutritionistId(parseInt(savedId));
      localStorage.removeItem('selectedNutritionistId');
      const savedName = localStorage.getItem('selectedNutritionistName');
      if (savedName) {
        toast.info(`Nutrizionista ${savedName} selezionato. Scegli data e ora.`);
        localStorage.removeItem('selectedNutritionistName');
      }
      setRequestType('specific');
    }
    loadAppointments();
    loadSpecializations();
  }, []);

  const loadAppointments = async () => {
    try {
      const res = await api.get('/patient/appointments');
      setAppointments(res.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const loadSpecializations = async () => {
    try {
      const res = await api.get('/public/specializations');
      if (res.data && res.data.length > 0) {
        setSpecializations(res.data);
      } else {
        setSpecializations(DEFAULT_SPECIALIZATIONS);
      }
    } catch (err) {
      console.error('Errore caricamento specializzazioni, uso dati di default', err);
      setSpecializations(DEFAULT_SPECIALIZATIONS);
    }
  };

  const formatLocalDateTime = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  };

  const createAppointment = async () => {
    if (!appointmentDate) {
      toast.error('Seleziona una data');
      return;
    }
    
    if (requestType === 'specific' && !selectedNutritionistId) {
      toast.error('Seleziona un nutrizionista dalla mappa');
      return;
    }
    
    const startTime = new Date(appointmentDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    if (startTime < tomorrow) {
      toast.error('Puoi richiedere appuntamenti solo a partire da domani');
      return;
    }
    
    const endTime = new Date(startTime);
    endTime.setHours(endTime.getHours() + 1);
    
    setLoading(true);
    
    try {
      const payload: any = {
        startTime: formatLocalDateTime(startTime),
        endTime: formatLocalDateTime(endTime),
      };
      
      if (requestType === 'specific' && selectedNutritionistId) {
        payload.nutritionistId = selectedNutritionistId;
      }
      
      if (requestType === 'general' && selectedSpecializationId) {
        payload.specializationId = selectedSpecializationId;
      }
      
      console.log('📅 Payload inviato:', payload);
      
      await api.post('/patient/appointments', payload);
      
      if (requestType === 'specific') {
        toast.success('Appuntamento richiesto al nutrizionista selezionato! Attendi conferma.');
      } else {
        toast.success('Richiesta di appuntamento inviata! Cercherò il nutrizionista più vicino disponibile.');
      }
      
      loadAppointments();
      setAppointmentDate('');
      setSelectedNutritionistId(null);
      setSelectedSpecializationId(null);
    } catch (err: any) {
      console.error('❌ Errore creazione appuntamento:', err);
      toast.error(err.response?.data?.error || 'Errore nella creazione dell\'appuntamento');
    } finally {
      setLoading(false);
    }
  };

  const cancelAppointment = async (id: number) => {
    try {
      await api.delete(`/patient/appointments/${id}`);
      toast.success('Appuntamento cancellato');
      loadAppointments();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      PENDING_PROPOSAL: 'bg-yellow-100 text-yellow-800',
      PROPOSED: 'bg-blue-100 text-blue-800',
      CONFIRMED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800',
      COMPLETED: 'bg-purple-100 text-purple-800',
      FAILED: 'bg-gray-100 text-gray-800',
    };
    const labels: Record<string, string> = {
      PENDING_PROPOSAL: 'In attesa',
      PROPOSED: 'Proposta inviata',
      CONFIRMED: 'Confermato',
      CANCELLED: 'Cancellato',
      COMPLETED: 'Completato',
      FAILED: 'Fallito',
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
        <p className="text-gray-500 mt-1">Gestisci le tue prenotazioni</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Nuova Richiesta</CardTitle>
          <CardDescription>Scegli il tipo di richiesta e i dettagli</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-4">
            <Button
              type="button"
              variant={requestType === 'specific' ? 'default' : 'outline'}
              onClick={() => setRequestType('specific')}
              className={requestType === 'specific' ? 'bg-emerald-600' : ''}
            >
              Nutrizionista Specifico
            </Button>
            <Button
              type="button"
              variant={requestType === 'general' ? 'default' : 'outline'}
              onClick={() => setRequestType('general')}
              className={requestType === 'general' ? 'bg-emerald-600' : ''}
            >
              <Sparkles className="h-4 w-4 mr-1" />
              Cerca Automatico
            </Button>
          </div>

          {requestType === 'specific' && (
            <div className="p-3 bg-blue-50 rounded-lg">
              {selectedNutritionistId ? (
                <p className="text-sm text-blue-700">
                  ✅ Nutrizionista selezionato. Scegli data e ora.
                </p>
              ) : (
                <p className="text-sm text-amber-600">
                  ⚠️ Vai sulla mappa per selezionare un nutrizionista specifico
                </p>
              )}
            </div>
          )}

          {requestType === 'general' && (
            <div className="space-y-2">
              <Label htmlFor="specialization">Specializzazione (opzionale)</Label>
              <select
                id="specialization"
                value={selectedSpecializationId || ''}
                onChange={(e) => setSelectedSpecializationId(parseInt(e.target.value) || null)}
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="">Tutte le specializzazioni</option>
                {specializations.map(spec => (
                  <option key={spec.specializationId} value={spec.specializationId}>
                    {spec.name}
                  </option>
                ))}
              </select>
              <p className="text-xs text-muted-foreground">
                La richiesta sarà inviata ai nutrizionisti più vicini con questa specializzazione
              </p>
            </div>
          )}

          <div className="flex gap-4">
            <Input
              type="datetime-local"
              value={appointmentDate}
              onChange={(e) => setAppointmentDate(e.target.value)}
              className="flex-1"
            />
            <Button 
              onClick={createAppointment} 
              disabled={loading || (requestType === 'specific' && !selectedNutritionistId)}
              className="bg-emerald-600 hover:bg-emerald-700"
            >
              <Plus className="h-4 w-4 mr-1" />
              Richiedi Appuntamento
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Elenco Appuntamenti</CardTitle>
          <CardDescription>Tutti i tuoi appuntamenti</CardDescription>
        </CardHeader>
        <CardContent>
          {appointments.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun appuntamento</p>
          ) : (
            <div className="space-y-3">
              {appointments.map((a) => (
                <div key={a.appointmentId} className="flex items-center justify-between p-4 border rounded-lg">
                  <div>
                    <p className="font-medium">{new Date(a.startTime).toLocaleString()}</p>
                    {a.location && <p className="text-sm text-gray-500">📍 {a.location}</p>}
                    {a.price && <p className="text-sm text-gray-500">💰 €{a.price}</p>}
                    <div className="mt-1">{getStatusBadge(a.status)}</div>
                  </div>
                  <div className="flex gap-2">
                    {a.status === 'CONFIRMED' && (
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => navigate(`/appointments/${a.appointmentId}`)}
                      >
                        Dettagli
                      </Button>
                    )}
                    {(a.status === 'PENDING_PROPOSAL' || a.status === 'PROPOSED') && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => cancelAppointment(a.appointmentId)}
                        className="text-red-600 hover:text-red-700"
                      >
                        <X className="h-4 w-4 mr-1" />
                        Cancella
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PatientAppointments;
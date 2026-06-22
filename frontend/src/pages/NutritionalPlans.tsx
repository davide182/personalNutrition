import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Calendar, FileText, MessageSquare, Plus, Save, Loader2, Stethoscope } from 'lucide-react';
import type { Appointment, NutritionalPlan } from '../types';

const NutritionalPlans: React.FC = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [selectedAppointmentId, setSelectedAppointmentId] = useState<number | null>(null);
  const [plan, setPlan] = useState<NutritionalPlan | null>(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [recommendations, setRecommendations] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingPlan, setLoadingPlan] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadAppointments();
  }, [user]);

  useEffect(() => {
    const appointmentIdParam = searchParams.get('appointmentId');
    if (appointmentIdParam && appointments.length > 0) {
      const id = parseInt(appointmentIdParam);
      if (appointments.some(apt => apt.appointmentId === id)) {
        setSelectedAppointmentId(id);
      }
    }
  }, [appointments, searchParams]);


  useEffect(() => {
    if (selectedAppointmentId) {
      loadPlan();
    } else {
      setPlan(null);
      setDiagnosis('');
      setRecommendations('');
    }
  }, [selectedAppointmentId]);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      let url = '';
      if (user?.role === 'PATIENT') {
        url = '/patient/appointments';
      } else if (user?.role === 'NUTRITIONIST') {
        url = '/nutritionist/appointments';
      } else {
        setLoading(false);
        return;
      }
      
      const res = await api.get(url);
      let apts = res.data;
      
      apts = apts.filter((apt: Appointment) => apt.status === 'COMPLETED');
      
      setAppointments(apts);
      

      const appointmentIdParam = searchParams.get('appointmentId');
      if (!appointmentIdParam && apts.length > 0 && !selectedAppointmentId) {
        setSelectedAppointmentId(apts[0].appointmentId);
      }
    } catch (err: any) {
      console.error(err);
      toast.error(err.response?.data?.error || 'Errore nel caricamento degli appuntamenti');
    } finally {
      setLoading(false);
    }
  };

  const loadPlan = async () => {
    if (!selectedAppointmentId) return;
    
    setLoadingPlan(true);
    try {
      let url = '';
      if (user?.role === 'PATIENT') {
        url = `/patient/appointments/${selectedAppointmentId}/nutritional-plan`;
      } else if (user?.role === 'NUTRITIONIST') {
        url = `/nutritionist/appointments/${selectedAppointmentId}/nutritional-plan`;
      } else {
        return;
      }
      
      const res = await api.get(url);
      setPlan(res.data);
      setDiagnosis(res.data.diagnosis || '');
      setRecommendations(res.data.recommendations);
    } catch (err: any) {
      if (err.response?.status === 404) {
        setPlan(null);
        setDiagnosis('');
        setRecommendations('');
      } else {
        console.error(err);
        toast.error(err.response?.data?.error || 'Errore nel caricamento del piano');
      }
    } finally {
      setLoadingPlan(false);
    }
  };

  const handleSavePlan = async () => {
    if (!selectedAppointmentId) return;
    
    if (!recommendations.trim()) {
      toast.error('Le raccomandazioni sono obbligatorie');
      return;
    }
    
    setSaving(true);
    try {
      if (plan) {
        await api.put(`/nutritionist/nutritional-plans/${plan.planId}`, {
          appointmentId: selectedAppointmentId,
          diagnosis,
          recommendations
        });
        toast.success('Piano nutrizionale aggiornato con successo!');
        await loadPlan();
      } else {
        const response = await api.post('/nutritionist/nutritional-plans', {
          appointmentId: selectedAppointmentId,
          diagnosis,
          recommendations
        });
        toast.success('Piano nutrizionale creato con successo!');
        const newPlan = response.data;
        setPlan(newPlan);
        setDiagnosis(newPlan.diagnosis || '');
        setRecommendations(newPlan.recommendations);
      }
    } catch (err: any) {
      console.error(err);
      toast.error(err.response?.data?.error || 'Errore nel salvataggio del piano');
    } finally {
      setSaving(false);
    }
  };

  const handleAppointmentChange = (appointmentId: number) => {
    setPlan(null);
    setDiagnosis('');
    setRecommendations('');
    setSelectedAppointmentId(appointmentId);
  };

  const getAppointmentLabel = (apt: Appointment) => {
    if (user?.role === 'NUTRITIONIST') {
      return `${apt.patientFirstName} ${apt.patientLastName} - ${new Date(apt.startTime).toLocaleDateString('it-IT')}`;
    } else {
      return `Visita del ${new Date(apt.startTime).toLocaleDateString('it-IT')}${apt.nutritionistName ? ` - Dott. ${apt.nutritionistName}` : ''}`;
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
        <h1 className="text-2xl font-bold text-gray-800">Piani Nutrizionali</h1>
        <p className="text-gray-500 mt-1">
          {user?.role === 'NUTRITIONIST' 
            ? 'Crea e gestisci i piani nutrizionali per i tuoi pazienti dopo aver completato una visita'
            : 'Visualizza i tuoi piani nutrizionali personalizzati creati dal tuo nutrizionista'}
        </p>
      </div>

      {appointments.length === 0 && (
        <Card className="bg-yellow-50 border-yellow-200">
          <CardContent className="pt-6">
            <div className="flex items-center gap-2 text-yellow-700">
              <Calendar className="w-4 h-4" />
              <p>
                {user?.role === 'NUTRITIONIST'
                  ? 'Non hai ancora appuntamenti completati. Completa una visita per poter creare un piano nutrizionale.'
                  : 'Non hai ancora piani nutrizionali disponibili. I tuoi piani appariranno qui dopo che il nutrizionista li avrà creati.'}
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      {appointments.length > 0 && (
        <>
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Stethoscope className="w-5 h-5" />
                Seleziona Appuntamento
              </CardTitle>
              <CardDescription>
                {user?.role === 'NUTRITIONIST' 
                  ? 'Scegli l\'appuntamento completato per cui vuoi creare/modificare il piano nutrizionale'
                  : 'Scegli l\'appuntamento per visualizzare il piano nutrizionale'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <select
                value={selectedAppointmentId?.toString() || ''}
                onChange={(e: React.ChangeEvent<HTMLSelectElement>) => handleAppointmentChange(parseInt(e.target.value))}
                className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                <option value="" disabled>Scegli un appuntamento...</option>
                {appointments.map((apt) => (
                  <option key={apt.appointmentId} value={apt.appointmentId.toString()}>
                    {getAppointmentLabel(apt)}
                  </option>
                ))}
              </select>
            </CardContent>
          </Card>

          {selectedAppointmentId && (
            <div className="grid md:grid-cols-2 gap-6">
              {user?.role === 'NUTRITIONIST' && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      {plan ? (
                        <>
                          <Save className="w-5 h-5" />
                          Modifica Piano Nutrizionale
                        </>
                      ) : (
                        <>
                          <Plus className="w-5 h-5" />
                          Crea Piano Nutrizionale
                        </>
                      )}
                    </CardTitle>
                    <CardDescription>
                      {plan 
                        ? 'Modifica la diagnosi e le raccomandazioni del piano esistente'
                        : 'Compila il form per creare un nuovo piano nutrizionale per questo paziente'}
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <Label htmlFor="diagnosis">Diagnosi (opzionale)</Label>
                      <textarea
                        id="diagnosis"
                        value={diagnosis}
                        onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setDiagnosis(e.target.value)}
                        placeholder="Inserisci la diagnosi..."
                        rows={4}
                        className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                      />
                    </div>

                    <div>
                      <Label htmlFor="recommendations" className="text-red-500">* Raccomandazioni</Label>
                      <textarea
                        id="recommendations"
                        value={recommendations}
                        onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setRecommendations(e.target.value)}
                        placeholder="Inserisci le raccomandazioni nutrizionali..."
                        rows={6}
                        className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        required
                      />
                    </div>

                    <Button 
                      onClick={handleSavePlan} 
                      disabled={saving || !recommendations.trim()}
                      className="w-full bg-emerald-600 hover:bg-emerald-700"
                    >
                      {saving ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Salvataggio...
                        </>
                      ) : plan ? (
                        'Aggiorna Piano'
                      ) : (
                        'Crea Piano'
                      )}
                    </Button>
                  </CardContent>
                </Card>
              )}

              {loadingPlan ? (
                <Card>
                  <CardContent className="flex justify-center items-center h-64">
                    <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
                  </CardContent>
                </Card>
              ) : plan ? (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="w-5 h-5 text-emerald-600" />
                      Piano Nutrizionale
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <Calendar className="w-4 h-4" />
                      <span>Creato il: {new Date(plan.createdAt).toLocaleDateString('it-IT')}</span>
                    </div>

                    {plan.diagnosis && (
                      <div>
                        <h3 className="font-semibold text-gray-700 mb-2 flex items-center gap-2">
                          <span className="w-2 h-2 bg-emerald-500 rounded-full"></span>
                          Diagnosi
                        </h3>
                        <p className="text-gray-600 whitespace-pre-wrap pl-4">{plan.diagnosis}</p>
                      </div>
                    )}

                    <div>
                      <h3 className="font-semibold text-gray-700 mb-2 flex items-center gap-2">
                        <MessageSquare className="w-4 h-4" />
                        Raccomandazioni
                      </h3>
                      <p className="text-gray-600 whitespace-pre-wrap pl-4">{plan.recommendations}</p>
                    </div>

                    {user?.role === 'PATIENT' && (
                      <div className="mt-4 pt-4 border-t border-gray-100 text-sm text-gray-400 italic">
                        Piano personalizzato creato dal tuo nutrizionista
                      </div>
                    )}

                    {user?.role === 'NUTRITIONIST' && plan && (
                      <div className="mt-4 pt-4 border-t border-gray-100 text-sm text-emerald-600 italic">
                        ✅ Questo piano è visibile al paziente
                      </div>
                    )}
                  </CardContent>
                </Card>
              ) : user?.role === 'PATIENT' ? (
                <Card className="bg-blue-50 border-blue-200">
                  <CardContent className="pt-6">
                    <p className="text-blue-700">
                      Nessun piano nutrizionale disponibile per questo appuntamento. 
                      Il nutrizionista lo creerà dopo aver completato la visita.
                    </p>
                  </CardContent>
                </Card>
              ) : user?.role === 'NUTRITIONIST' && !plan && (
                <Card className="bg-blue-50 border-blue-200">
                  <CardContent className="pt-6">
                    <p className="text-blue-700">
                      Nessun piano creato per questo appuntamento. Compila il form a sinistra per crearlo.
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default NutritionalPlans;
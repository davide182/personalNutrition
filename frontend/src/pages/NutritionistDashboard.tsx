import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import { Calendar, Users, FileText, Clock, Loader2, UserPlus } from 'lucide-react';
import { toast } from 'sonner';

const NutritionistDashboard: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    pendingProposals: 0,
    totalAppointments: 0,
    totalPatients: 0,
    totalPlans: 0,
  });
  const [nextAppointment, setNextAppointment] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const proposalsRes = await api.get('/nutritionist/proposals').catch(() => ({ data: [] }));
      const proposals = proposalsRes.data || [];
      const pendingProposals = proposals.filter((p: any) => p.positionInQueue > 0).length;
      
      const appointmentsRes = await api.get('/nutritionist/appointments').catch(() => ({ data: [] }));
      const appointments = appointmentsRes.data || [];
      
      const now = new Date();
      const futureAppointments = appointments
        .filter((apt: any) => apt.status === 'CONFIRMED' && new Date(apt.startTime) > now)
        .sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
      setNextAppointment(futureAppointments[0] || null);
      
      const uniquePatients = new Set();
      appointments.forEach((apt: any) => {
        if (apt.userId) uniquePatients.add(apt.userId);
      });
      
      const plansRes = await api.get('/nutritionist/nutritional-plans').catch(() => ({ data: [] }));
      const plans = plansRes.data || [];
      
      setStats({
        pendingProposals,
        totalAppointments: appointments.length,
        totalPatients: uniquePatients.size,
        totalPlans: plans.length,
      });
    } catch (err: any) {
      console.error(err);
      toast.error('Errore nel caricamento della dashboard');
    } finally {
      setLoading(false);
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
        <h1 className="text-2xl font-bold text-gray-800">
          Ciao, Dott. {user?.lastName}! 👋
        </h1>
        <p className="text-gray-500 mt-1">Ecco il riepilogo della tua attività</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Richieste in attesa</p>
                <p className="text-2xl font-bold">{stats.pendingProposals}</p>
              </div>
              <UserPlus className="h-8 w-8 text-yellow-500" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Pazienti totali</p>
                <p className="text-2xl font-bold">{stats.totalPatients}</p>
              </div>
              <Users className="h-8 w-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Appuntamenti totali</p>
                <p className="text-2xl font-bold">{stats.totalAppointments}</p>
              </div>
              <Calendar className="h-8 w-8 text-emerald-500" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Piani Creati</p>
                <p className="text-2xl font-bold">{stats.totalPlans}</p>
              </div>
              <FileText className="h-8 w-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-emerald-600" />
              Prossimo Appuntamento
            </CardTitle>
          </CardHeader>
          <CardContent>
            {nextAppointment ? (
              <div>
                <p className="font-semibold">
                  {nextAppointment.patientFirstName} {nextAppointment.patientLastName}
                </p>
                <p className="text-sm text-gray-500 mt-1">
                  {new Date(nextAppointment.startTime).toLocaleDateString('it-IT')} alle{' '}
                  {new Date(nextAppointment.startTime).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })}
                </p>
                {nextAppointment.location && (
                  <p className="text-sm text-gray-500 mt-1">📍 {nextAppointment.location}</p>
                )}
                <Button asChild variant="outline" className="mt-4 w-full">
                  <Link to="/appointments">Vedi tutti gli appuntamenti</Link>
                </Button>
              </div>
            ) : (
              <div className="text-center py-6">
                <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-2" />
                <p className="text-gray-500">Nessun appuntamento in programma</p>
                <Button asChild variant="outline" className="mt-4">
                  <Link to="/schedule">Gestisci i tuoi orari</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Azioni Rapide</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button asChild className="w-full bg-emerald-600 hover:bg-emerald-700">
              <Link to="/proposals">📋 Gestisci Richieste</Link>
            </Button>
            <Button asChild variant="outline" className="w-full">
              <Link to="/plans">📄 Crea Piano Nutrizionale</Link>
            </Button>
            <Button asChild variant="outline" className="w-full">
              <Link to="/schedule">⏰ Gestisci Orari</Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      
    </div>
  );
};

export default NutritionistDashboard;
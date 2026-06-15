// src/pages/PatientDashboard.tsx
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import { Calendar, FileText, Activity, TrendingUp, Apple, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const PatientDashboard: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    upcomingAppointments: 0,
    totalAppointments: 0,
    nutritionalPlans: 0,
    healthDataComplete: false,
  });
  const [loading, setLoading] = useState(true);
  const [recentPlans, setRecentPlans] = useState<any[]>([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const appointmentsRes = await api.get('/patient/appointments');
      const appointments = appointmentsRes.data;
      const now = new Date();
      const upcoming = appointments.filter((apt: any) => 
        apt.status === 'CONFIRMED' && new Date(apt.startTime) > now
      );
      
      const plansRes = await api.get('/patient/nutritional-plans').catch(() => ({ data: [] }));
      const plans = plansRes.data || [];
      
      const healthRes = await api.get('/patient/health-data').catch(() => ({ data: null }));
      const hasHealthData = healthRes.data && healthRes.data.weight && healthRes.data.height;
      
      setStats({
        upcomingAppointments: upcoming.length,
        totalAppointments: appointments.length,
        nutritionalPlans: plans.length,
        healthDataComplete: !!hasHealthData,
      });
      
      setRecentPlans(plans.slice(0, 3));
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
          Ciao, {user?.firstName}! 👋
        </h1>
        <p className="text-gray-500 mt-1">Ecco il riepilogo della tua attività</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Prossimi appuntamenti</p>
                <p className="text-2xl font-bold">{stats.upcomingAppointments}</p>
              </div>
              <Calendar className="h-8 w-8 text-emerald-500" />
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
              <TrendingUp className="h-8 w-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Piani Nutrizionali</p>
                <p className="text-2xl font-bold">{stats.nutritionalPlans}</p>
              </div>
              <FileText className="h-8 w-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Profilo Salute</p>
                <p className="text-2xl font-bold">{stats.healthDataComplete ? '✅' : '⚠️'}</p>
              </div>
              <Activity className="h-8 w-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {recentPlans.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Apple className="w-5 h-5 text-emerald-600" />
              Piani Nutrizionali Recenti
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {recentPlans.map((plan: any) => (
                <div key={plan.planId} className="border rounded-lg p-3">
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="font-medium">Piano del {new Date(plan.createdAt).toLocaleDateString('it-IT')}</p>
                      <p className="text-sm text-gray-500 line-clamp-1">{plan.recommendations.substring(0, 100)}...</p>
                    </div>
                    <Button asChild variant="outline" size="sm">
                      <Link to="/plans">Visualizza</Link>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Prenota una visita</CardTitle>
          </CardHeader>
          <CardContent>
            <Button asChild className="w-full bg-emerald-600 hover:bg-emerald-700">
              <Link to="/map">Cerca Nutrizionista</Link>
            </Button>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Aggiorna i tuoi dati</CardTitle>
          </CardHeader>
          <CardContent>
            <Button asChild variant="outline" className="w-full">
              <Link to="/health-data">Vai a Dati Salute</Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      
    </div>
  );
};

export default PatientDashboard;
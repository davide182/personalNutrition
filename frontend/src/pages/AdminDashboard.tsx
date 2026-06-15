import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Users, UserCheck, Activity, TrendingUp, PieChart, Calendar, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const AdminDashboard: React.FC = () => {
  const [pendingCount, setPendingCount] = useState(0);
  const [totalNutritionists, setTotalNutritionists] = useState(0);
  const [totalPatients, setTotalPatients] = useState(0);
  const [appointments, setAppointments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const pendingRes = await api.get('/admin/pending-nutritionists');
      setPendingCount(pendingRes.data.length);
      
      const usersRes = await api.get('/admin/all-users');
      const users = usersRes.data;
      setTotalNutritionists(users.filter((u: any) => u.role === 'NUTRITIONIST' && u.status === 'ACTIVE').length);
      setTotalPatients(users.filter((u: any) => u.role === 'PATIENT' && u.status === 'ACTIVE').length);
      
      const appointmentsRes = await api.get('/admin/appointments').catch(() => ({ data: [] }));
      setAppointments(appointmentsRes.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Errore nel caricamento dei dati');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { style: string; label: string }> = {
      'IN ATTESA': { style: 'bg-yellow-100 text-yellow-800', label: 'In attesa' },
      'PROPOSTO': { style: 'bg-blue-100 text-blue-800', label: 'Proposto' },
      'CONFERMATO': { style: 'bg-green-100 text-green-800', label: 'Confermato' },
      'COMPLETATO': { style: 'bg-purple-100 text-purple-800', label: 'Completato' },
      'CANCELLATO': { style: 'bg-red-100 text-red-800', label: 'Cancellato' },
      'FALLITO': { style: 'bg-gray-100 text-gray-800', label: 'Fallito' },
    };
    
    const mapped = statusMap[status] || { style: 'bg-gray-100 text-gray-800', label: status };
    
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${mapped.style}`}>
        {mapped.label}
      </span>
    );
  };

  const appointmentStats = {
    pending: appointments.filter(a => a.status === 'IN ATTESA' || a.status === 'PROPOSTO').length,
    confirmed: appointments.filter(a => a.status === 'CONFERMATO').length,
    completed: appointments.filter(a => a.status === 'COMPLETATO').length,
    cancelled: appointments.filter(a => a.status === 'CANCELLATO').length,
    failed: appointments.filter(a => a.status === 'FALLITO').length,
  };

  const getNutritionistName = (apt: any) => {
    if (apt.nutritionistFirstName && apt.nutritionistLastName) {
      return `${apt.nutritionistFirstName} ${apt.nutritionistLastName}`;
    }
    return '—';
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
        <h1 className="text-2xl font-bold text-gray-900">Pannello Amministrazione</h1>
        <p className="text-gray-500 mt-1">Panoramica generale della piattaforma</p>
      </div>

      {/* Statistiche generali */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Nutrizionisti attivi</p>
                <p className="text-2xl font-bold">{totalNutritionists}</p>
              </div>
              <Users className="h-8 w-8 text-emerald-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Pazienti attivi</p>
                <p className="text-2xl font-bold">{totalPatients}</p>
              </div>
              <UserCheck className="h-8 w-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">In attesa di approvazione</p>
                <p className="text-2xl font-bold">{pendingCount}</p>
              </div>
              <TrendingUp className="h-8 w-8 text-yellow-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Appuntamenti totali</p>
                <p className="text-2xl font-bold">{appointments.length}</p>
              </div>
              <PieChart className="h-8 w-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Statistiche Appuntamenti per stato */}
      <div className="grid gap-4 md:grid-cols-5">
        <Card className="bg-yellow-50">
          <CardContent className="pt-4 text-center">
            <p className="text-2xl font-bold text-yellow-700">{appointmentStats.pending}</p>
            <p className="text-sm text-yellow-600">In attesa/Proposti</p>
          </CardContent>
        </Card>
        <Card className="bg-green-50">
          <CardContent className="pt-4 text-center">
            <p className="text-2xl font-bold text-green-700">{appointmentStats.confirmed}</p>
            <p className="text-sm text-green-600">Confermati</p>
          </CardContent>
        </Card>
        <Card className="bg-purple-50">
          <CardContent className="pt-4 text-center">
            <p className="text-2xl font-bold text-purple-700">{appointmentStats.completed}</p>
            <p className="text-sm text-purple-600">Completati</p>
          </CardContent>
        </Card>
        <Card className="bg-red-50">
          <CardContent className="pt-4 text-center">
            <p className="text-2xl font-bold text-red-700">{appointmentStats.cancelled}</p>
            <p className="text-sm text-red-600">Cancellati</p>
          </CardContent>
        </Card>
        <Card className="bg-gray-50">
          <CardContent className="pt-4 text-center">
            <p className="text-2xl font-bold text-gray-700">{appointmentStats.failed}</p>
            <p className="text-sm text-gray-600">Falliti</p>
          </CardContent>
        </Card>
      </div>

      {/*Lista di tutti gli appuntamenti*/}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5" />
            Tutti gli Appuntamenti
          </CardTitle>
          <CardDescription>Elenco completo di tutti gli appuntamenti della piattaforma ({appointments.length} totali)</CardDescription>
        </CardHeader>
        <CardContent>
          {appointments.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun appuntamento presente</p>
          ) : (
            <div className="overflow-x-auto max-h-[600px] overflow-y-auto">
              <table className="w-full text-sm">
                <thead className="border-b sticky top-0 bg-white">
                  <tr>
                    <th className="text-left py-2 px-3">ID</th>
                    <th className="text-left py-2 px-3">Paziente</th>
                    <th className="text-left py-2 px-3">Nutrizionista</th>
                    <th className="text-left py-2 px-3">Data/Ora</th>
                    <th className="text-left py-2 px-3">Stato</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr key={apt.appointmentId} className="border-b hover:bg-gray-50">
                      <td className="py-2 px-3">#{apt.appointmentId}</td>
                      <td className="py-2 px-3">
                        {apt.patientFirstName} {apt.patientLastName}
                      </td>
                      <td className="py-2 px-3">
                        {getNutritionistName(apt)}
                      </td>
                      <td className="py-2 px-3">
                        {new Date(apt.startTime).toLocaleString('it-IT')}
                      </td>
                      <td className="py-2 px-3">{getStatusBadge(apt.status)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Azioni rapide */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <UserCheck className="h-5 w-5" />
              Approvazioni nutrizionisti
            </CardTitle>
            <CardDescription>{pendingCount} nutrizionisti in attesa di approvazione</CardDescription>
          </CardHeader>
          <CardContent>
            <Button asChild className="w-full">
              <Link to="/pending">Gestisci approvazioni</Link>
            </Button>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Monitoraggio sistema
            </CardTitle>
            <CardDescription>Visualizza i log di sistema e le statistiche</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" asChild className="w-full">
              <Link to="/logs">Vedi log</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboard;
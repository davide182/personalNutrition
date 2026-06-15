import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Plus, Trash2, Clock } from 'lucide-react';
import { toast } from 'sonner';

interface WorkSchedule {
  workScheduleId?: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

const NutritionistSchedule: React.FC = () => {
  const [schedules, setSchedules] = useState<WorkSchedule[]>([]);
  const [dayOfWeek, setDayOfWeek] = useState('MONDAY');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('18:00');
  const [loading, setLoading] = useState(false);

  const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  useEffect(() => {
    loadSchedules();
  }, []);

  const loadSchedules = async () => {
    try {
      const res = await api.get('/nutritionist/schedule');
      setSchedules(res.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const addSchedule = async () => {
    if (!startTime || !endTime) {
      toast.error('Inserisci orario di inizio e fine');
      return;
    }
    setLoading(true);
    try {
      await api.post('/nutritionist/schedule', { dayOfWeek, startTime, endTime });
      toast.success('Orario aggiunto!');
      loadSchedules();
      setStartTime('09:00');
      setEndTime('18:00');
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    } finally {
      setLoading(false);
    }
  };

  const deleteSchedule = async (id: number) => {
    try {
      await api.delete(`/nutritionist/schedule/${id}`);
      toast.success('Orario eliminato');
      loadSchedules();
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    }
  };

  const getDayLabel = (day: string) => {
    const labels: Record<string, string> = {
      MONDAY: 'Lunedì',
      TUESDAY: 'Martedì',
      WEDNESDAY: 'Mercoledì',
      THURSDAY: 'Giovedì',
      FRIDAY: 'Venerdì',
      SATURDAY: 'Sabato',
      SUNDAY: 'Domenica',
    };
    return labels[day] || day;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Gestione Orari</h1>
        <p className="text-gray-500 mt-1">Imposta i tuoi orari di lavoro</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Aggiungi orario</CardTitle>
          <CardDescription>Imposta i giorni e gli orari in cui sei disponibile</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4 flex-wrap items-end">
            <div className="space-y-2">
              <Label>Giorno</Label>
              <select 
                value={dayOfWeek} 
                onChange={(e) => setDayOfWeek(e.target.value)}
                className="border rounded-md px-3 py-2"
              >
                {days.map(d => <option key={d} value={d}>{getDayLabel(d)}</option>)}
              </select>
            </div>
            <div className="space-y-2">
              <Label>Da</Label>
              <Input 
                type="time" 
                value={startTime} 
                onChange={(e) => setStartTime(e.target.value)} 
                className="w-32"
              />
            </div>
            <div className="space-y-2">
              <Label>A</Label>
              <Input 
                type="time" 
                value={endTime} 
                onChange={(e) => setEndTime(e.target.value)} 
                className="w-32"
              />
            </div>
            <Button onClick={addSchedule} disabled={loading}>
              <Plus className="h-4 w-4 mr-1" />
              Aggiungi
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>I miei orari</CardTitle>
          <CardDescription>Orari di lavoro attuali</CardDescription>
        </CardHeader>
        <CardContent>
          {schedules.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun orario impostato</p>
          ) : (
            <div className="space-y-2">
              {schedules.map((s) => (
                <div key={s.workScheduleId} className="flex justify-between items-center p-3 border rounded-lg">
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-gray-500" />
                    <span className="font-medium">{getDayLabel(s.dayOfWeek)}</span>
                    <span className="text-gray-500">{s.startTime} - {s.endTime}</span>
                  </div>
                  <Button 
                    onClick={() => deleteSchedule(s.workScheduleId!)} 
                    variant="destructive" 
                    size="sm"
                  >
                    <Trash2 className="h-4 w-4" />
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

export default NutritionistSchedule;
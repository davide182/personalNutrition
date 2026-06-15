import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Activity, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';

interface SystemLog {
  id: string;
  level: string;
  service: string;
  action: string;
  email: string;
  message: string;
  timestamp: string;
}

const AdminLogs: React.FC = () => {
  const [logs, setLogs] = useState<SystemLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    loadLogs();
  }, []);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const url = filter ? `/admin/logs?level=${filter}` : '/admin/logs';
      const res = await api.get(url);
      setLogs(res.data);
    } catch (err: any) {
      toast.error(err.response?.data?.error);
    } finally {
      setLoading(false);
    }
  };

  const getLevelBadge = (level: string) => {
    const styles = {
      ERROR: 'bg-red-100 text-red-800',
      WARN: 'bg-yellow-100 text-yellow-800',
      INFO: 'bg-green-100 text-green-800',
    };
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[level as keyof typeof styles] || 'bg-gray-100'}`}>
        {level}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Log di Sistema</h1>
        <p className="text-gray-500 mt-1">Monitoraggio delle operazioni sulla piattaforma</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="h-5 w-5" />
            Registro Eventi
          </CardTitle>
          <CardDescription>Tutte le operazioni registrate su MongoDB</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4 mb-4">
            <select 
              value={filter} 
              onChange={(e) => setFilter(e.target.value)}
              className="border rounded-md px-3 py-2"
            >
              <option value="">Tutti i livelli</option>
              <option value="INFO">INFO</option>
              <option value="WARN">WARN</option>
              <option value="ERROR">ERROR</option>
            </select>
            <Button onClick={loadLogs} variant="outline" disabled={loading}>
              <RefreshCw className="h-4 w-4 mr-1" />
              Aggiorna
            </Button>
          </div>
          
          {logs.length === 0 ? (
            <p className="text-gray-500 text-center py-8">Nessun log trovato</p>
          ) : (
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {logs.map(log => (
                <div key={log.id} className="border-b pb-2 text-sm">
                  <div className="flex items-center gap-2 mb-1">
                    {getLevelBadge(log.level)}
                    <span className="font-semibold">{log.service}</span>
                    <span className="text-gray-500">- {log.action}</span>
                  </div>
                  <p className="text-gray-600">{log.message}</p>
                  <p className="text-xs text-gray-400 mt-1">{log.email} - {new Date(log.timestamp).toLocaleString()}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default AdminLogs;
import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CalendarDays, MapPin, Euro, Clock, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface AppointmentDetail {
  appointmentId: number;
  startTime: string;
  endTime: string;
  status: string;
  nutritionistName: string;
  nutritionistLatitude: number;
  nutritionistLongitude: number;
  patientLatitude: number;
  patientLongitude: number;
  distance: number;
  price?: number;
  location?: string;
}

export default function AppointmentDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState<AppointmentDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAppointment();
  }, [id]);

  const loadAppointment = async () => {
    try {
      const res = await api.get(`/patient/appointments/${id}`);
      console.log('📅 Dettagli appuntamento:', res.data);
      setAppointment(res.data);
    } catch (err: any) {
      console.error('❌ Errore caricamento dettagli:', err);
      toast.error(err.response?.data?.error);
      navigate('/appointments');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600"></div>
      </div>
    );
  }

  if (!appointment) return null;


  const patientPosition: [number, number] = [appointment.patientLatitude, appointment.patientLongitude];
  const nutritionistPosition: [number, number] = [appointment.nutritionistLatitude, appointment.nutritionistLongitude];
  
  const mapCenter: [number, number] = [
    (patientPosition[0] + nutritionistPosition[0]) / 2,
    (patientPosition[1] + nutritionistPosition[1]) / 2
  ];

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={() => navigate('/appointments')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />
        Torna agli appuntamenti
      </Button>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Dettagli */}
        <Card>
          <CardHeader>
            <CardTitle>Dettagli Appuntamento</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-2">
              <CalendarDays className="h-5 w-5 text-gray-500" />
              <div>
                <p className="font-medium">Data e ora</p>
                <p className="text-sm text-gray-600">{new Date(appointment.startTime).toLocaleString()}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="h-5 w-5 text-gray-500" />
              <div>
                <p className="font-medium">Luogo</p>
                <p className="text-sm text-gray-600">{appointment.location || 'Da definire'}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Euro className="h-5 w-5 text-gray-500" />
              <div>
                <p className="font-medium">Prezzo</p>
                <p className="text-sm text-gray-600">{appointment.price ? `€${appointment.price}` : 'Da definire'}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-gray-500" />
              <div>
                <p className="font-medium">Distanza dal nutrizionista</p>
                <p className="text-sm text-gray-600">{appointment.distance?.toFixed(1) || 'N/A'} km</p>
              </div>
            </div>
            <div className="pt-4 border-t">
              <p className="font-medium">Nutrizionista</p>
              <p className="text-sm text-gray-600">{appointment.nutritionistName}</p>
            </div>
          </CardContent>
        </Card>

        {/* Mappa */}
        <Card>
          <CardHeader>
            <CardTitle>Posizione</CardTitle>
          </CardHeader>
          <CardContent className="p-0 overflow-hidden rounded-lg">
            <MapContainer
              center={mapCenter}
              zoom={12}
              style={{ height: '400px', width: '100%' }}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              />
              <Marker position={patientPosition}>
                <Popup>La tua posizione (salvata nel profilo)</Popup>
              </Marker>
              <Marker position={nutritionistPosition}>
                <Popup>Studio del nutrizionista</Popup>
              </Marker>
            </MapContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
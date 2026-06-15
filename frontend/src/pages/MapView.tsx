import { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';
import api from '@/api/axiosConfig';
import { toast } from 'sonner';
import { Award, MapPin, Stethoscope } from 'lucide-react';

// Fix per icone Leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface NutritionistMap {
  id: number;
  name: string;
  firstName: string;
  lastName: string;
  latitude: number;
  longitude: number;
  distance: number;
  bio?: string;
  specializations?: string[];
}

interface UserProfile {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  latitude: number;
  longitude: number;
  role: string;
}

export default function MapView() {
  const navigate = useNavigate();
  const [nutritionists, setNutritionists] = useState<NutritionistMap[]>([]);
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const res = await api.get('/user/profile');
        console.log('Profilo utente:', res.data);
        
        if (res.data && res.data.latitude && res.data.longitude) {
          const lat = res.data.latitude;
          const lon = res.data.longitude;
          console.log('Posizione dal profilo:', lat, lon);
          setUserLocation([lat, lon]);
          setUserProfile(res.data);
          loadNutritionists(lat, lon);
        } else {
          throw new Error('Nessuna posizione nel profilo');
        }
      } catch (err) {
        console.error('Errore caricamento profilo:', err);
        const defaultLat = 45.4642;
        const defaultLon = 9.1900;
        setUserLocation([defaultLat, defaultLon]);
        loadNutritionists(defaultLat, defaultLon);
        toast.warning('Posizione non trovata nel profilo, usata Milano come default');
      }
    };

    loadUserProfile();
  }, []);

  const loadNutritionists = async (lat: number, lon: number) => {
    setLoading(true);
    try {
      console.log('Caricamento nutrizionisti vicini a:', lat, lon);
      const res = await api.get('/patient/nutritionists/nearby', {
        params: { lat, lon }
      });
      console.log('Nutrizionisti trovati:', res.data.length);
      setNutritionists(res.data);
      if (res.data.length === 0) {
        toast.info('Nessun nutrizionista trovato');
      }
    } catch (err: any) {
      console.error('Errore caricamento nutrizionisti:', err);
      toast.error(err.response?.data?.error || 'Errore nel caricamento');
    } finally {
      setLoading(false);
    }
  };

  const handleBookAppointment = (nutritionist: NutritionistMap) => {
    localStorage.setItem('selectedNutritionistId', nutritionist.id.toString());
    localStorage.setItem('selectedNutritionistName', nutritionist.name);
    navigate('/appointments');
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600"></div>
      </div>
    );
  }

  if (!userLocation) {
    return (
      <Card>
        <CardContent className="py-8 text-center">
          <p>Posizione non disponibile. Aggiorna il tuo profilo.</p>
          <Button onClick={() => window.location.reload()} className="mt-4">
            Riprova
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Nutrizionisti Vicini</h1>
        <Button 
          variant="outline" 
          onClick={() => userLocation && loadNutritionists(userLocation[0], userLocation[1])}
        >
          Aggiorna
        </Button>
      </div>
      
      {userProfile && (
        <p className="text-sm text-gray-500">
          📍 La tua posizione: {userProfile.firstName} {userProfile.lastName}
        </p>
      )}
      
      <Card>
        <CardContent className="p-0 overflow-hidden rounded-lg">
          <MapContainer
            center={userLocation}
            zoom={13}
            style={{ height: '500px', width: '100%' }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            <Marker position={userLocation}>
              <Popup>La tua posizione ({userProfile?.firstName} {userProfile?.lastName})</Popup>
            </Marker>
            {nutritionists.map((n) => (
              <Marker key={n.id} position={[n.latitude, n.longitude]}>
                <Popup>
                  <div className="text-center">
                    <p className="font-bold text-emerald-700">{n.name}</p>
                    <div className="text-sm mt-1">
                      <p><span className="font-semibold">📍 Distanza:</span> {n.distance.toFixed(1)} km</p>
                      {n.bio && <p><span className="font-semibold">📋 Bio:</span> {n.bio}</p>}
                      <p><span className="font-semibold">🏷️ Specializzazioni:</span> {n.specializations && n.specializations.length > 0 ? n.specializations.join(', ') : 'Nessuna'}</p>
                    </div>
                    <Button 
                      size="sm" 
                      className="mt-3 w-full bg-emerald-600 hover:bg-emerald-700"
                      onClick={() => handleBookAppointment(n)}
                    >
                      Prenota Appuntamento
                    </Button>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </CardContent>
      </Card>
      
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {nutritionists.map((n) => (
          <Card key={n.id} className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <CardTitle className="text-lg flex items-center gap-2">
                <Stethoscope className="h-4 w-4 text-emerald-600" />
                {n.name}
              </CardTitle>
              <CardDescription>{n.bio || 'Nutrizionista professionista'}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-1 text-sm text-gray-500 mb-2">
                <Award className="h-3 w-3" />
                <span className="text-xs">
                  Specializzazioni: {n.specializations && n.specializations.length > 0 ? n.specializations.join(', ') : 'Generale'}
                </span>
              </div>
              <div className="flex items-center gap-1 text-sm text-gray-500 mb-4">
                <MapPin className="h-3 w-3" />
                <span className="text-xs">
                  Distanza: {n.distance.toFixed(1)} km
                </span>
              </div>
              <Button 
                className="mt-2 w-full bg-emerald-600 hover:bg-emerald-700"
                onClick={() => handleBookAppointment(n)}
              >
                Prenota Appuntamento
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
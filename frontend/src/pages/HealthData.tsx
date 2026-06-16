// src/pages/HealthDataPage.tsx
import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { toast } from 'sonner';
import { Activity, Scale, Ruler, AlertCircle, Target, Loader2, Edit, Save, X } from 'lucide-react';
import type { HealthData, BMICalculation } from '../types';

const HealthDataPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [healthData, setHealthData] = useState<HealthData>({
    weight: 0,
    height: 0,
    allergies: '',
    goals: '',
  });
  const [originalData, setOriginalData] = useState<HealthData>({
    weight: 0,
    height: 0,
    allergies: '',
    goals: '',
  });
  const [bmiData, setBmiData] = useState<BMICalculation | null>(null);
  const [hasData, setHasData] = useState(false);
  const [calculatingBMI, setCalculatingBMI] = useState(false);

  useEffect(() => {
    loadHealthData();
  }, []);

  const loadHealthData = async () => {
    setLoading(true);
    try {
      const res = await api.get('/patient/health-data');
      const data = res.data;
      
      if (data && (data.weight || data.height || data.allergies || data.goals)) {
        setHasData(true);
        const newData = {
          weight: data.weight || 0,
          height: data.height || 0,
          allergies: data.allergies || '',
          goals: data.goals || '',
        };
        setHealthData(newData);
        setOriginalData(newData);
        
        if (data.weight && data.weight > 0 && data.height && data.height > 0) {
          await fetchBMI(data.weight, data.height);
        }
      } else {
        setHasData(false);
        setHealthData({
          weight: 0,
          height: 0,
          allergies: '',
          goals: '',
        });
        setBmiData(null);
      }
    } catch (err: any) {
      if (err.response?.status === 404) {
        setHasData(false);
        setHealthData({
          weight: 0,
          height: 0,
          allergies: '',
          goals: '',
        });
        setBmiData(null);
      } else {
        console.error(err);
        toast.error(err.response?.data?.error || 'Errore nel caricamento dei dati');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchBMI = async (weight: number, height: number) => {
    if (!weight || !height || weight <= 0 || height <= 0) {
      setBmiData(null);
      return;
    }
    
    setCalculatingBMI(true);
    try {

      const res = await api.get('/patient/bmi');
      setBmiData(res.data);
    } catch (err) {
      console.error('Errore calcolo BMI:', err);
      const heightInMeters = height / 100;
      const bmi = weight / (heightInMeters * heightInMeters);
      let category = '';
      if (bmi < 18.5) category = 'Sottopeso';
      else if (bmi < 25) category = 'Normopeso';
      else if (bmi < 30) category = 'Sovrappeso';
      else category = 'Obesità';
      
      setBmiData({
        bmi: Math.round(bmi * 10) / 10,
        bmr: Math.round(10 * weight + 6.25 * height - 5 * 30 + 5),
        daily_calories: Math.round((10 * weight + 6.25 * height - 5 * 30 + 5) * 1.2),
        bmi_category: category
      });
    } finally {
      setCalculatingBMI(false);
    }
  };

  const handleSave = async () => {
    if (!healthData.weight || healthData.weight <= 0) {
      toast.error('Inserisci un peso valido');
      return;
    }
    if (!healthData.height || healthData.height <= 0) {
      toast.error('Inserisci un\'altezza valida');
      return;
    }

    setSaving(true);
    try {
      let _response;
      
      if (hasData) {
        _response = await api.put('/patient/health-data', {
          weight: healthData.weight,
          height: healthData.height,
          allergies: healthData.allergies || '',
          goals: healthData.goals || '',
        });
        toast.success('Dati aggiornati con successo!', _response.data);
      } else {
        _response = await api.post('/patient/health-data', {
          weight: healthData.weight,
          height: healthData.height,
          allergies: healthData.allergies || '',
          goals: healthData.goals || '',
        });
        toast.success('Dati salvati con successo!', _response.data);
      }
      
      setHasData(true);
      setOriginalData({ ...healthData });
      setEditing(false);
      await fetchBMI(healthData.weight, healthData.height);
    } catch (err: any) {
      console.error(err);
      toast.error(err.response?.data?.error || 'Errore nel salvataggio');
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = () => {
    setEditing(true);
  };

  const handleCancel = () => {
    setHealthData({ ...originalData });
    setEditing(false);
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
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Dati di Salute</h1>
          <p className="text-gray-500 mt-1">Gestisci i tuoi dati antropometrici e obiettivi</p>
        </div>
        {hasData && !editing && (
          <Button onClick={handleEdit} variant="outline" className="flex items-center gap-2">
            <Edit className="w-4 h-4" />
            Modifica Dati
          </Button>
        )}
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        {/* Form Dati */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="w-5 h-5 text-emerald-600" />
              {hasData && !editing ? 'I miei Dati' : 'Inserisci i tuoi Dati'}
            </CardTitle>
            <CardDescription>
              {hasData && !editing 
                ? 'Ecco i tuoi dati attuali. Clicca su Modifica per aggiornarli.'
                : 'Inserisci i tuoi dati per calcolare BMI e ricevere piani personalizzati'}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {!editing && hasData ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-500">Peso</p>
                    <p className="text-xl font-semibold">{healthData.weight} kg</p>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-500">Altezza</p>
                    <p className="text-xl font-semibold">{healthData.height} cm</p>
                  </div>
                </div>
                
                {healthData.allergies && (
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-500 flex items-center gap-2">
                      <AlertCircle className="w-4 h-4" />
                      Allergie / Intolleranze
                    </p>
                    <p className="mt-1 whitespace-pre-wrap">{healthData.allergies}</p>
                  </div>
                )}
                
                {healthData.goals && (
                  <div className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-500 flex items-center gap-2">
                      <Target className="w-4 h-4" />
                      Obiettivi
                    </p>
                    <p className="mt-1 whitespace-pre-wrap">{healthData.goals}</p>
                  </div>
                )}

                {!healthData.allergies && !healthData.goals && (
                  <p className="text-sm text-gray-400 italic text-center">
                    Nessuna informazione aggiuntiva inserita
                  </p>
                )}
              </div>
            ) : (
              <>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="weight" className="flex items-center gap-2">
                      <Scale className="w-4 h-4" />
                      Peso (kg) *
                    </Label>
                    <Input
                      id="weight"
                      type="number"
                      step="0.1"
                      value={healthData.weight || ''}
                      onChange={(e) => setHealthData({ ...healthData, weight: parseFloat(e.target.value) || 0 })}
                      placeholder="es. 70.5"
                      className="mt-1"
                    />
                  </div>
                  <div>
                    <Label htmlFor="height" className="flex items-center gap-2">
                      <Ruler className="w-4 h-4" />
                      Altezza (cm) *
                    </Label>
                    <Input
                      id="height"
                      type="number"
                      step="0.1"
                      value={healthData.height || ''}
                      onChange={(e) => setHealthData({ ...healthData, height: parseFloat(e.target.value) || 0 })}
                      placeholder="es. 170"
                      className="mt-1"
                    />
                  </div>
                </div>

                <div>
                  <Label htmlFor="allergies" className="flex items-center gap-2">
                    <AlertCircle className="w-4 h-4" />
                    Allergie / Intolleranze
                  </Label>
                  <textarea
                    id="allergies"
                    value={healthData.allergies}
                    onChange={(e) => setHealthData({ ...healthData, allergies: e.target.value })}
                    placeholder="Es. lattosio, glutine, frutta secca..."
                    rows={3}
                    className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div>
                  <Label htmlFor="goals" className="flex items-center gap-2">
                    <Target className="w-4 h-4" />
                    Obiettivi
                  </Label>
                  <textarea
                    id="goals"
                    value={healthData.goals}
                    onChange={(e) => setHealthData({ ...healthData, goals: e.target.value })}
                    placeholder="Es. perdere 5 kg, aumentare massa muscolare, migliorare alimentazione..."
                    rows={3}
                    className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div className="flex gap-3">
                  <Button 
                    onClick={handleSave} 
                    disabled={saving}
                    className="flex-1 bg-emerald-600 hover:bg-emerald-700"
                  >
                    {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                    Salva Dati
                  </Button>
                  {hasData && (
                    <Button 
                      variant="outline" 
                      onClick={handleCancel}
                      className="flex-1"
                    >
                      <X className="mr-2 h-4 w-4" />
                      Annulla
                    </Button>
                  )}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Card BMI */}
        {calculatingBMI ? (
          <Card>
            <CardContent className="flex justify-center items-center h-48">
              <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
              <p className="ml-2 text-gray-500">Calcolo BMI in corso...</p>
            </CardContent>
          </Card>
        ) : bmiData ? (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="w-5 h-5 text-emerald-600" />
                Il mio BMI
              </CardTitle>
              <CardDescription>Calcolo basato sui tuoi dati</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-center">
                <p className="text-5xl font-bold text-emerald-600">{bmiData.bmi}</p>
                <p className="text-lg font-semibold mt-2">{bmiData.bmi_category}</p>
              </div>
              <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                <div className="text-center">
                  <p className="text-sm text-gray-500">Metabolismo Basale</p>
                  <p className="text-xl font-semibold">{Math.round(bmiData.bmr)} kcal</p>
                </div>
                <div className="text-center">
                  <p className="text-sm text-gray-500">Calorie giornaliere</p>
                  <p className="text-xl font-semibold">{Math.round(bmiData.daily_calories)} kcal</p>
                </div>
              </div>
            </CardContent>
          </Card>
        ) : hasData && healthData.weight > 0 && healthData.height > 0 ? (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="w-5 h-5 text-yellow-500" />
                Calcolo BMI
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-gray-500 text-center">
                Clicca su "Salva Dati" per calcolare il tuo BMI.
              </p>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="w-5 h-5 text-gray-400" />
                BMI non disponibile
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-gray-500 text-center">
                Inserisci peso e altezza per calcolare il tuo BMI.
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
};

export default HealthDataPage;
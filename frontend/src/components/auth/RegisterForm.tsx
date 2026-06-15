import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, Stethoscope, Mail, Lock, User, MapPin, Briefcase, Award, Eye, EyeOff, CheckCircle } from 'lucide-react';
import type { Role, RegisterData } from '../../types';
import api from '../../api/axiosConfig';

interface Specialization {
  specializationId: number;
  name: string;
  description: string;
}

export default function RegisterForm() {
  const [form, setForm] = useState<RegisterData & { specializationId?: number; confirmPassword: string }>({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    role: 'PATIENT',
    address: '',
    bio: '',
    specializationId: undefined,
  });
  const [specializations, setSpecializations] = useState<Specialization[]>([]);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();


  const [passwordRules, setPasswordRules] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false,
  });

  useEffect(() => {
    const loadSpecializations = async () => {
      try {
        console.log('Caricamento specializzazioni...');
        const res = await api.get('/admin/specializations');
        console.log('Specializzazioni ricevute:', res.data);
        setSpecializations(res.data);
      } catch (err) {
        console.error('Errore caricamento specializzazioni', err);
        setSpecializations([
          { specializationId: 1, name: 'Nutrizione clinica', description: '' },
          { specializationId: 2, name: 'Nutrizione sportiva', description: '' },
          { specializationId: 3, name: 'Disturbi alimentari', description: '' },
        ]);
      }
    };
    loadSpecializations();
  }, []);


  useEffect(() => {
    setPasswordRules({
      length: form.password.length >= 8,
      uppercase: /[A-Z]/.test(form.password),
      lowercase: /[a-z]/.test(form.password),
      number: /[0-9]/.test(form.password),
      special: /[@#$%^&+=]/.test(form.password),
    });
  }, [form.password]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (form.password !== form.confirmPassword) {
      toast.error('Le password non coincidono');
      return;
    }
    
    const allRulesValid = Object.values(passwordRules).every(rule => rule === true);
    if (!allRulesValid) {
      toast.error('La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola, un numero e un carattere speciale (@#$%^&+=)');
      return;
    }
    
    setLoading(true);
    try {
      const payload: any = {
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        role: form.role,
        address: form.address,
      };
      if (form.role === 'NUTRITIONIST') {
        payload.bio = form.bio;
        payload.specializationId = form.specializationId;
      }
      await register(payload);
      toast.success('Registrazione completata! Ora puoi fare login.');
      navigate('/login');
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore registrazione');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 via-teal-50 to-cyan-50 py-8">
      <div className="absolute inset-0 bg-grid-slate-100 [mask-image:radial-gradient(ellipse_at_center,white,transparent)] pointer-events-none" />
      
      <Card className="w-full max-w-md shadow-2xl border-0 relative z-10">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="h-16 w-16 bg-emerald-100 rounded-2xl flex items-center justify-center shadow-lg">
              <Stethoscope className="h-8 w-8 text-emerald-600" />
            </div>
          </div>
          <CardTitle className="text-3xl font-bold bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent">
            Registrazione
          </CardTitle>
          <CardDescription className="text-base">
            Crea il tuo account su Nutritionists
          </CardDescription>
        </CardHeader>
        
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">Nome</Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <Input
                    id="firstName"
                    placeholder="Mario"
                    value={form.firstName}
                    onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                    className="pl-10"
                    required
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Cognome</Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <Input
                    id="lastName"
                    placeholder="Rossi"
                    value={form.lastName}
                    onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                    className="pl-10"
                    required
                  />
                </div>
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="email"
                  type="email"
                  placeholder="nome@esempio.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  className="pl-10"
                  required
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  className="pl-10 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                >
                  {showPassword ? <EyeOff className="h-4 w-4 text-gray-400" /> : <Eye className="h-4 w-4 text-gray-400" />}
                </button>
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Conferma Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="••••••••"
                  value={form.confirmPassword}
                  onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                  className="pl-10 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4 text-gray-400" /> : <Eye className="h-4 w-4 text-gray-400" />}
                </button>
              </div>
            </div>

            {/*Regole password */}
            {form.password && (
              <div className="bg-gray-50 rounded-lg p-3 space-y-1">
                <p className="text-xs font-semibold text-gray-600 mb-2">La password deve contenere:</p>
                <div className="flex items-center gap-2 text-xs">
                  {passwordRules.length ? <CheckCircle className="h-3 w-3 text-green-500" /> : <div className="h-3 w-3 rounded-full border border-gray-300" />}
                  <span className={passwordRules.length ? "text-green-600" : "text-gray-500"}>Almeno 8 caratteri</span>
                </div>
                <div className="flex items-center gap-2 text-xs">
                  {passwordRules.uppercase ? <CheckCircle className="h-3 w-3 text-green-500" /> : <div className="h-3 w-3 rounded-full border border-gray-300" />}
                  <span className={passwordRules.uppercase ? "text-green-600" : "text-gray-500"}>Almeno una lettera maiuscola</span>
                </div>
                <div className="flex items-center gap-2 text-xs">
                  {passwordRules.lowercase ? <CheckCircle className="h-3 w-3 text-green-500" /> : <div className="h-3 w-3 rounded-full border border-gray-300" />}
                  <span className={passwordRules.lowercase ? "text-green-600" : "text-gray-500"}>Almeno una lettera minuscola</span>
                </div>
                <div className="flex items-center gap-2 text-xs">
                  {passwordRules.number ? <CheckCircle className="h-3 w-3 text-green-500" /> : <div className="h-3 w-3 rounded-full border border-gray-300" />}
                  <span className={passwordRules.number ? "text-green-600" : "text-gray-500"}>Almeno un numero</span>
                </div>
                <div className="flex items-center gap-2 text-xs">
                  {passwordRules.special ? <CheckCircle className="h-3 w-3 text-green-500" /> : <div className="h-3 w-3 rounded-full border border-gray-300" />}
                  <span className={passwordRules.special ? "text-green-600" : "text-gray-500"}>Almeno un carattere speciale (@#$%^&+=)</span>
                </div>
              </div>
            )}
            
            <div className="space-y-2">
              <Label htmlFor="role">Ruolo</Label>
              <select
                id="role"
                value={form.role}
                onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              >
                <option value="PATIENT">Paziente</option>
                <option value="NUTRITIONIST">Nutrizionista</option>
              </select>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="address">Indirizzo</Label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="address"
                  placeholder="Via Roma 1, Milano"
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                  className="pl-10"
                  required
                />
              </div>
              <p className="text-xs text-muted-foreground">Inserisci un indirizzo valido in Italia</p>
            </div>
            
            {form.role === 'NUTRITIONIST' && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="specialization" className="flex items-center gap-2">
                    <Award className="h-4 w-4" />
                    Specializzazione *
                  </Label>
                  <select
                    id="specialization"
                    value={form.specializationId || ''}
                    onChange={(e) => setForm({ ...form, specializationId: parseInt(e.target.value) })}
                    className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    required
                  >
                    <option value="">Seleziona una specializzazione</option>
                    {specializations.map(spec => (
                      <option key={spec.specializationId} value={spec.specializationId}>
                        {spec.name}
                      </option>
                    ))}
                  </select>
                  {specializations.length === 0 && (
                    <p className="text-xs text-amber-600">Caricamento specializzazioni...</p>
                  )}
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="bio">Bio (opzionale)</Label>
                  <div className="relative">
                    <Briefcase className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                    <textarea
                      id="bio"
                      className="w-full rounded-md border border-input bg-background px-3 py-2 pl-10 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                      rows={3}
                      placeholder="Parla della tua esperienza e specializzazioni..."
                      value={form.bio}
                      onChange={(e) => setForm({ ...form, bio: e.target.value })}
                    />
                  </div>
                </div>
              </>
            )}
          </CardContent>
          
          <CardFooter className="flex flex-col gap-4">
            <Button 
              type="submit" 
              className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-semibold py-2"
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Registrazione in corso...
                </>
              ) : (
                'Registrati'
              )}
            </Button>
            
            <div className="text-center text-sm text-muted-foreground">
              Hai già un account?{' '}
              <Link 
                to="/login" 
                className="text-emerald-600 hover:text-emerald-700 font-semibold hover:underline transition-all"
              >
                Accedi
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
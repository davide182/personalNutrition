import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, Lock, Eye, EyeOff, CheckCircle } from 'lucide-react';

const ResetPasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [resetted, setResetted] = useState(false);

  const [passwordRules, setPasswordRules] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false,
  });

  useEffect(() => {
    validateToken();
  }, [token]);

  useEffect(() => {
    setPasswordRules({
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[@#$%^&+=]/.test(password),
    });
  }, [password]);

  const validateToken = async () => {
    if (!token) {
      setTokenValid(false);
      setValidating(false);
      return;
    }

    try {
      const res = await api.get(`/auth/verify-reset-token?token=${token}`);
      setTokenValid(res.data.valid);
    } catch (err) {
      setTokenValid(false);
    } finally {
      setValidating(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      toast.error('Le password non coincidono');
      return;
    }
    
    const allRulesValid = Object.values(passwordRules).every(rule => rule === true);
    if (!allRulesValid) {
      toast.error('La password non rispetta tutti i requisiti');
      return;
    }

    setLoading(true);
    try {
      const res = await api.post('/auth/reset-password', { token, newPassword: password });
      if (res.data.success) {
        setResetted(true);
        toast.success('Password resettata con successo!');
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        toast.error(res.data.message);
      }
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore durante il reset della password');
    } finally {
      setLoading(false);
    }
  };

  if (validating) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  if (!tokenValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <CardTitle>Link Non Valido</CardTitle>
            <CardDescription>
              Il link per il reset della password non è valido o è scaduto.
            </CardDescription>
          </CardHeader>
          <CardFooter>
            <Link to="/forgot-password" className="w-full">
              <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                Richiedi nuovo link
              </Button>
            </Link>
          </CardFooter>
        </Card>
      </div>
    );
  }

  if (resetted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <div className="mx-auto w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mb-4">
              <CheckCircle className="h-6 w-6 text-green-600" />
            </div>
            <CardTitle>Password Resettata!</CardTitle>
            <CardDescription>
              La tua password è stata resettata con successo.
            </CardDescription>
          </CardHeader>
          <CardFooter>
            <Link to="/login" className="w-full">
              <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                Vai al Login
              </Button>
            </Link>
          </CardFooter>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50 py-8">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle>Nuova Password</CardTitle>
          <CardDescription>
            Inserisci la tua nuova password
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="password">Nuova Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
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
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
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
          </CardContent>
          <CardFooter className="flex flex-col gap-4">
            <Button 
              type="submit" 
              className="w-full bg-emerald-600 hover:bg-emerald-700" 
              disabled={loading || !tokenValid}
            >
              {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Resetta Password
            </Button>
            <Link to="/login" className="text-sm text-emerald-600 hover:underline text-center">
              Torna al Login
            </Link>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
};

export default ResetPasswordPage;
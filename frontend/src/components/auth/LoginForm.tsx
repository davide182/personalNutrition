import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, Stethoscope, Mail, Lock } from 'lucide-react';

export default function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      toast.success('Login effettuato con successo!');
      navigate('/dashboard');
    } catch (err: any) {
      const errorMessage = err.response?.data?.error || err.message || 'Login fallito';
      
      if (errorMessage.includes('disabilitato') && errorMessage.includes('Contatta')) {
        toast.error('Il tuo account è stato disabilitato. Contatta l\'amministratore per riattivarlo.');
      } else if (errorMessage.includes('attesa di disabilitazione')) {
        toast.error('Il tuo account è in attesa di disabilitazione. Riceverai una email di conferma.');
      } else if (errorMessage.includes('attesa di revisione') || errorMessage.includes('SUSPENDED')) {
        toast.error('Il tuo account è in attesa di revisione. Riceverai una email quando sarà riattivato.');
      } else if (errorMessage.includes('attesa di approvazione') || errorMessage.includes('PENDING')) {
        toast.error('Il tuo account è in attesa di approvazione. Riceverai una email quando sarà attivato.');
      } else if (errorMessage.includes('disabilitato permanentemente') || errorMessage.includes('DISABLED')) {
        toast.error('Il tuo account è stato disabilitato permanentemente. Contatta l\'amministratore.');
      } else if (errorMessage.includes('Email o password non validi') || 
                 errorMessage.includes('credenziali non valide')) {
        toast.error('Email o password errati');
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 via-teal-50 to-cyan-50">
      <div className="absolute inset-0 bg-grid-slate-100 [mask-image:radial-gradient(ellipse_at_center,white,transparent)] pointer-events-none" />
      
      <Card className="w-full max-w-md shadow-2xl border-0 relative z-10">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="h-16 w-16 bg-emerald-100 rounded-2xl flex items-center justify-center shadow-lg">
              <Stethoscope className="h-8 w-8 text-emerald-600" />
            </div>
          </div>
          <CardTitle className="text-3xl font-bold bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent">
            Nutritionists
          </CardTitle>
          <CardDescription className="text-base">
            Accedi al tuo account per gestire i tuoi appuntamenti
          </CardDescription>
        </CardHeader>
        
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium">
                Email
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="email"
                  type="email"
                  placeholder="nome@esempio.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium">
                Password
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>
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
                  Accesso in corso...
                </>
              ) : (
                'Accedi'
              )}
            </Button>
            
            <div className="text-center">
              <Link 
                to="/forgot-password" 
                className="text-sm text-emerald-600 hover:text-emerald-700 hover:underline"
              >
                Password dimenticata?
              </Link>
            </div>
            
            <div className="text-center text-sm text-muted-foreground">
              Non hai un account?{' '}
              <Link 
                to="/register" 
                className="text-emerald-600 hover:text-emerald-700 font-semibold hover:underline transition-all"
              >
                Registrati
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
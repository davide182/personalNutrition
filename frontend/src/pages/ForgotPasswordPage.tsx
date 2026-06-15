// src/pages/ForgotPasswordPage.tsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2, Mail, ArrowLeft } from 'lucide-react';

const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      toast.error('Inserisci la tua email');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      setSubmitted(true);
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Errore durante l\'invio');
    } finally {
      setLoading(false);
    }
  };

  if (submitted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <div className="mx-auto w-12 h-12 bg-emerald-100 rounded-full flex items-center justify-center mb-4">
              <Mail className="h-6 w-6 text-emerald-600" />
            </div>
            <CardTitle>Email Inviata!</CardTitle>
            <CardDescription>
              Abbiamo inviato un link per il reset della password all'indirizzo {email}
            </CardDescription>
          </CardHeader>
          <CardContent className="text-center text-sm text-gray-500">
            <p>Controlla la tua casella di posta e clicca sul link che ti abbiamo inviato.</p>
            <p className="mt-2">Il link è valido per 24 ore.</p>
          </CardContent>
          <CardFooter>
            <Link to="/login" className="w-full">
              <Button variant="outline" className="w-full">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Torna al Login
              </Button>
            </Link>
          </CardFooter>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-50">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle>Recupera Password</CardTitle>
          <CardDescription>
            Inserisci la tua email e ti invieremo un link per resettare la password
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
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
          </CardContent>
          <CardFooter className="flex flex-col gap-4">
            <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700" disabled={loading}>
              {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Invia Link di Reset
            </Button>
            <Link to="/login" className="text-sm text-emerald-600 hover:underline">
              Torna al Login
            </Link>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
};

export default ForgotPasswordPage;
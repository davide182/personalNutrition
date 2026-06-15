export type Role = 'PATIENT' | 'NUTRITIONIST' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'DISABLED' | 'SELF_DISABLED'; // 🔥 AGGIUNTO SELF_DISABLED

export interface User {
  userId: number;
  email: string;
  role: Role;
  status: UserStatus;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token?: string;
  userId?: number;
  email: string;
  role: Role;
  status: UserStatus;
  firstName: string;
  lastName: string;
  message: string;
}

export interface Appointment {
  appointmentId: number;
  startTime: string;
  endTime: string;
  status: string;
  nutritionistName?: string;
  userId?: number;
  patientFirstName?: string;
  patientLastName?: string;
}

export interface Proposal {
  appointmentId: number;
  patientFirstName: string;
  patientLastName: string;
  patientEmail: string;
  startTime: string;
  endTime: string;
  distanceKm: number;
  positionInQueue: number;
}

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: Role;
  address: string;
  bio?: string;
}

export interface HealthData {
  weight: number;
  height: number;
  allergies?: string;
  goals?: string;
  bmi?: number;
}

export interface NutritionalPlan {
  planId: number;
  appointmentId: number;
  diagnosis: string;
  recommendations: string;
  createdAt: string;
}

export interface WorkSchedule {
  workScheduleId?: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface BMICalculation {
  bmi: number;
  bmr: number;
  daily_calories: number;
  bmi_category: string;
}

export interface SystemLog {
  id: string;
  level: string;
  service: string;
  action: string;
  email: string;
  message: string;
  timestamp: string;
}

export interface PendingUser {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  status: string;
}

export interface NutritionalPlanRequest {
  appointmentId: number;
  diagnosis: string;
  recommendations: string;
}

export interface DisableNutritionistRequest {
  reason: string;
}

export interface PendingDisableRequest {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  status: UserStatus;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
  success: boolean;
}